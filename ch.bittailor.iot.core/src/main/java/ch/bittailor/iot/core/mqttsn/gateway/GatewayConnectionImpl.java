package ch.bittailor.iot.core.mqttsn.gateway;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bittailor.iot.core.mqttsn.messages.Connack;
import ch.bittailor.iot.core.mqttsn.messages.Connect;
import ch.bittailor.iot.core.mqttsn.messages.Disconnect;
import ch.bittailor.iot.core.mqttsn.messages.Message;
import ch.bittailor.iot.core.mqttsn.messages.MessageVisitor;
import ch.bittailor.iot.core.mqttsn.messages.PingReq;
import ch.bittailor.iot.core.mqttsn.messages.Publish;
import ch.bittailor.iot.core.mqttsn.messages.Regack;
import ch.bittailor.iot.core.mqttsn.messages.Register;
import ch.bittailor.iot.core.mqttsn.messages.ReturnCode;
import ch.bittailor.iot.core.mqttsn.messages.Suback;
import ch.bittailor.iot.core.mqttsn.messages.Subscribe;
import ch.bittailor.iot.core.utils.Utilities;
import ch.bittailor.iot.core.wsn.PacketSocket;
import ch.bittailor.iot.core.wsn.RfSocketAddress;

public class GatewayConnectionImpl implements GatewayConnection, MessageVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(GatewayConnectionImpl.class);

	private final ExecutorService mExecutorService;
	private final RfSocketAddress mAddress;
	private final PacketSocket mSocket;
	private final MqttClientFactory mFactory;

	private final TopicStorage mTopicStorage;

	private final Disconnected mDisconnected;
	private final Active mActive;
	private final Asleep mAsleep;
	private State mCurrentState;

	private int mKeepAliveTimeout;
	private MqttClient mBrokerClient;



	public GatewayConnectionImpl(ExecutorService executorService, 
			RfSocketAddress address,
			PacketSocket socket,
			MqttClientFactory factory) {
		
		mExecutorService = executorService;
		mAddress = address;
		mSocket = socket;
		mFactory = factory;

		mTopicStorage = new TopicStorage();

		mDisconnected = new Disconnected();
		mActive = new Active();
		mAsleep = new Asleep();

		mCurrentState = mDisconnected;

	}

	@Override
	public void close() throws IOException {
		mExecutorService.shutdown();
	}

	@Override
	public void handle(final Message message) {
		mExecutorService.execute(new Runnable() {		
			@Override
			public void run() {
				message.accept(GatewayConnectionImpl.this);
			}
		});
	}

	@Override
	public void visit(Connect connect) {
		LOG.info("handle Connect " + mAddress);	
		mCurrentState.hanlde(connect);
	}

	@Override
	public void visit(Connack connack) {
		LOG.info("handle Connack " + mAddress);	
		LOG.warn("drop CONNACK message since since this should not be sent by a client to the gateway");
	}

	@Override
	public void visit(Register register) {
		LOG.info("handle Register " + mAddress);
		mCurrentState.handle(register);
	}

	@Override
	public void visit(Regack regack) {
		LOG.info("handle Regack " + mAddress);
		mCurrentState.handle(regack);
	}

	@Override
	public void visit(Publish publish) {
		LOG.info("handle Publish " + mAddress);
		mCurrentState.handle(publish);
	}

	@Override
	public void visit(Disconnect disconnect) {
		LOG.info("handle Disconnect " + mAddress);

	}

	@Override
	public void visit(Subscribe subscribe) {
		LOG.info("handle Subscribe " + mAddress);

	}

	@Override
	public void visit(Suback suback) {
		LOG.info("handle Suback " + mAddress);	
	}



	@Override
	public void visit(PingReq pingreq) {
	}

	private void send(Message message) {
		ByteBuffer buffer = message.toByteBuffer();
		LOG.info("send {} : 0x{} ",message.getClass().getSimpleName(), Utilities.toHexString(buffer));
		if(mSocket.send(mAddress, buffer) == -1){
			LOG.error("send of {} failed", message.getClass().getSimpleName());
		};
	}

	private void changeState(State state) {
		LOG.info("state change {} => {} ", mCurrentState.getClass().getSimpleName(), state.getClass().getSimpleName());
		mCurrentState = state;
	}

	private void connectToBroker(Connect message) {
		if(!message.isCleanSession()) {
			LOG.warn("CONNECT with CleanSession=false not supported!");
			changeState(mDisconnected);
			send(new Connack(ReturnCode.REJECTED_NOT_SUPPORTED));
			return;
		}
		mKeepAliveTimeout = message.getDuration() + (message.getDuration() / 2);

		try {
			mBrokerClient = mFactory.create(message.getClientId());
		} catch (Exception exception) {
			LOG.warn(" create MQTT Client failed", exception);
			changeState(mDisconnected);
			send(new Connack(ReturnCode.REJECTED_NOT_SUPPORTED));
			return;
		}
		changeState(mActive);
		mBrokerClient.publish("GW/Info", ("New Connection for " + mAddress ).getBytes(), 1, false);
		send(new Connack(ReturnCode.ACCEPTED));
	}

	private void reconnectToBroker(Connect message) {
		LOG.warn("Reconnect without disconnect => flush topic storage");
		// TODO unsubscribe to all !
		try {
			mBrokerClient.close();
		} catch (Exception e) {
			LOG.error("failed to close brocker client", e);
			e.printStackTrace();
		}
		mBrokerClient = null;
		mTopicStorage.clear();
		connectToBroker(message);
	}

	private void registerTopic(Register message) {
		int topicId = mTopicStorage.getOrCreateTopicId(message.getTopicName());
		send(new Regack(topicId, message.getMsgId(), ReturnCode.ACCEPTED));
	}
	
	private void publishToBroker(Publish message) {
	   String topicName = mTopicStorage.getTopicName(message.getTopicId());
	   if (topicName == null) {
	  	 LOG.warn("could not find topic name for id {}", message.getTopicId());
	      return;
	   }
	   mBrokerClient.publish(topicName, message.getData(), message.getQos(), message.isRetain());
	}

	// -- state machine


	private interface State {
		void hanlde(Connect connect);
		void handle(Regack regack);
		void handle(Register register);
		void handle(Publish publish);

	}

	private class Disconnected implements State {

		@Override
		public void hanlde(Connect connect) {
			connectToBroker(connect);
		}

		@Override
		public void handle(Register register) {
			LOG.warn("drop REGISTER message from client since gateway is in Disconnected state");
		}

		@Override
		public void handle(Regack regack) {
			LOG.warn("drop REGACK message from client since gateway is in Disconnected state");
		}

		@Override
		public void handle(Publish publish) {
			if (publish.getQos() == 3 ) {
				LOG.warn("PUBLISH with QoS -1 not supported yet => drop PUBLISH message");
				return;
			}
			LOG.warn("drop PUBLISH message from client since in Disconnected state");
		}

	}

	private class Active implements State {

		@Override
		public void hanlde(Connect connect) {
			reconnectToBroker(connect);
		}

		@Override
		public void handle(Register register) {
			registerTopic(register);
		}

		@Override
		public void handle(Regack regack) {
			// currently nothing to do
		}
		
		@Override
		public void handle(Publish publish) {
			publishToBroker(publish);
		}

	}

	private class Asleep implements State {

		@Override
		public void hanlde(Connect connect) {
			// TODO Auto-generated method stub	
		}

		@Override
		public void handle(Register register) {
			// TODO Auto-generated method stub		
		}

		@Override
		public void handle(Regack regack) {
			// TODO Auto-generated method stub	
		}
		
		@Override
		public void handle(Publish publish) {
			// TODO Auto-generated method stub	
		}

	}







}
