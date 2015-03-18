package ch.bittailor.iot.core.mqttsn.gateway;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bittailor.iot.core.mqttsn.messages.Connack;
import ch.bittailor.iot.core.mqttsn.messages.Connect;
import ch.bittailor.iot.core.mqttsn.messages.Disconnect;
import ch.bittailor.iot.core.mqttsn.messages.Message;
import ch.bittailor.iot.core.mqttsn.messages.MessageVisitor;
import ch.bittailor.iot.core.mqttsn.messages.PingReq;
import ch.bittailor.iot.core.mqttsn.messages.PingResp;
import ch.bittailor.iot.core.mqttsn.messages.Publish;
import ch.bittailor.iot.core.mqttsn.messages.Regack;
import ch.bittailor.iot.core.mqttsn.messages.Register;
import ch.bittailor.iot.core.mqttsn.messages.ReturnCode;
import ch.bittailor.iot.core.mqttsn.messages.Suback;
import ch.bittailor.iot.core.mqttsn.messages.Subscribe;
import ch.bittailor.iot.core.utils.Utilities;
import ch.bittailor.iot.core.wsn.PacketSocket;
import ch.bittailor.iot.core.wsn.RfSocketAddress;

public class GatewayConnectionImpl implements GatewayConnection, MessageVisitor, MqttClient.Callback {
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
	private int mMsgIdCounter;

	private int mKeepAliveTimeout;
	private int mAsleepTimeout;
	private MqttClient mBrokerClient;
	private List<BufferedMessage> mBufferedMessages;
	



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
		mMsgIdCounter = 0;
		mBufferedMessages = new LinkedList<GatewayConnectionImpl.BufferedMessage>();

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
		LOG.info("handle Connect [{}]", mAddress);
		mCurrentState.hanlde(connect);
	}

	@Override
	public void visit(Connack connack) {
		LOG.info("handle Connack [{}]", mAddress);	
		LOG.warn("drop CONNACK message since since this should not be sent by a client to the gateway");
	}

	@Override
	public void visit(Register register) {
		LOG.info("handle Register [{}]", mAddress);
		mCurrentState.handle(register);
	}

	@Override
	public void visit(Regack regack) {
		LOG.info("handle Regack [{}]", mAddress);
		mCurrentState.handle(regack);
	}

	@Override
	public void visit(Publish publish) {
		LOG.info("handle Publish [{}]", mAddress);
		mCurrentState.handle(publish);
	}

	@Override
	public void visit(Disconnect disconnect) {
		LOG.info("handle Disconnect [{}]", mAddress);
		mCurrentState.handle(disconnect);
	}

	@Override
	public void visit(Subscribe subscribe) {
		LOG.info("handle Subscribe [{}]", mAddress);
		mCurrentState.handle(subscribe);
	}

	@Override
	public void visit(Suback suback) {
		LOG.info("handle Suback [{}]", mAddress);	
	}

	@Override
	public void visit(PingReq pingreq) {
		LOG.info("handle PINGREQ [{}]", mAddress);	
		mCurrentState.handle(pingreq);
	}

	@Override
	public void visit(PingResp pingResp) {
		LOG.info("handle PINGRESP [{}]", mAddress);
		mCurrentState.handle(pingResp);
		
	}
	
	// ---
	
	@Override
	public void messageArrived(final String topic, final byte[] payload, final int qos, final boolean retain) {
		mExecutorService.execute(new Runnable() {		
			@Override
			public void run() {
				LOG.info("message from broker {} - {}", topic, Utilities.getString(ByteBuffer.wrap(payload)));
				mCurrentState.handleMessageArrived(topic, payload, qos, retain);				
			}
		});
		
	}

	// ---

	private void send(Message message) {
		ByteBuffer buffer = message.toByteBuffer();
		LOG.info("send {} : 0x{}",message.getClass().getSimpleName(), Utilities.toHexString(buffer));
		if(mSocket.send(mAddress, buffer) == -1){
			LOG.error("send of {} failed", message.getClass().getSimpleName());
		};
	}

	
	private void changeState(State state) {
		LOG.info("state change {} => {}", mCurrentState.getClass().getSimpleName(), state.getClass().getSimpleName());
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
			mBrokerClient.setCallback(this);
		} catch (Exception exception) {
			LOG.warn(" create MQTT Client failed", exception);
			changeState(mDisconnected);
			send(new Connack(ReturnCode.REJECTED_NOT_SUPPORTED));
			return;
		}
		changeState(mActive);
		send(new Connack(ReturnCode.ACCEPTED));
	}

	private void reconnectToBroker(Connect message) {
		LOG.warn("Reconnect without disconnect => flush topic storage");
		mBrokerClient.setCallback(null);
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
	
	private boolean containsWildcardCharacters(String topicName) {
	   return topicName.contains("*") || topicName.contains("+");
	}

	
	private void subscribe(Subscribe message) {

	   int topicId = TopicStorage.NO_TOPIC_ID;
	   if(!containsWildcardCharacters(message.getTopicName())) {
	      topicId = mTopicStorage.getOrCreateTopicId(message.getTopicName());
	   }

	   LOG.info("subscribe to {} [topic id = {}]", message.getTopicName(), topicId);
	   mBrokerClient.subscribe(message.getTopicName(),message.getQos());
	   send(new Suback(message.getQos(), topicId, message.getMsgId(), ReturnCode.ACCEPTED));
	}

	private void sendPingresp() {
	   send(new PingResp());
	}
	
	private void forwardMessageToClient(String topic, byte[] payload, int qos, boolean retain) {
		int topicId = mTopicStorage.getTopicId(topic);
		if(topicId == TopicStorage.NO_TOPIC_ID) {
			topicId = mTopicStorage.getOrCreateTopicId(topic);
			int msgId = mMsgIdCounter++;
			send(new Register(topicId, msgId, topic));
		}
		int msgId = mMsgIdCounter++;
		send(new Publish(qos,retain,topicId, msgId, payload));		
	}
	
	private void handleSleep(int duration) {
	   mAsleepTimeout = duration;
	   changeState(mAsleep);
	   send(new Disconnect());
	}
	
	void disconnect(boolean sendDisconnectToClient) {
	   if(mBrokerClient != null) {
	  	 try {
				mBrokerClient.close();
			} catch (Exception e) {
				LOG.error("closing the broker client failed", e);
			}
	  	 mBrokerClient = null;
	   }
	   changeState(mDisconnected);
	   if(sendDisconnectToClient) {
	  	 send(new Disconnect());
	   }
	}
	
	private void connectFromSleep(Connect message) {
	   changeState(mActive);
	   sendBufferedMessages();
	   send(new Connack(ReturnCode.ACCEPTED));
	}
	
	private void sendBufferedMessages() {
		List<BufferedMessage> messages = mBufferedMessages;
		mBufferedMessages = new LinkedList<GatewayConnectionImpl.BufferedMessage>();
		for (BufferedMessage message : messages) {
			forwardMessageToClient(message.getTopic(), message.getPayload(), message.getQos(), message.isRetain());
		} 
	}
	
	private void awake() {
	   sendBufferedMessages();
	   send(new PingResp());
	}
	
	private void storeMessage(String topic, byte[] payload, int qos, boolean retain) {
		mBufferedMessages.add(new BufferedMessage(topic, payload, qos, retain));	
	}

	// -- state machine

	private interface State {
		void hanlde(Connect connect);
		void handle(Disconnect disconnect);
		void handle(PingReq pingreq);
		void handle(Regack regack);
		void handle(Register register);
		void handle(Publish publish);
		void handle(Subscribe subscribe);
		void handle(PingResp pingResp);
		void handleMessageArrived(String topic, byte[] payload, int qos, boolean retain);
	}

	private class Disconnected implements State {

		@Override
		public void hanlde(Connect connect) {
			connectToBroker(connect);
		}
		
		@Override
		public void handle(Disconnect disconnect) {
			LOG.warn("drop DISCONNECT message from client since gateway is already Disconnected state");
			// do nothing here
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

		@Override
		public void handle(Subscribe subscribe) {
			LOG.warn("drop SUBSCRIBE message from client since gateway is in Disconnected state");
		}

		@Override
		public void handle(PingReq pingreq) {
			LOG.warn("drop PINGREQ message from client since gateway is in Disconnected state");
		  // do not send a PINGRESP since we are Disconnected			
		}

		@Override
		public void handle(PingResp pingResp) {
			LOG.warn("drop PINGRESP message from client since gateway is in Disconnected state");
		}

		@Override
		public void handleMessageArrived(String topic, byte[] payload, int qos, boolean retain) {
			LOG.warn("drop message to topic {} from broker since in Disconnected state", topic);			
		}
		
		
			
	}

	private class Active implements State {

		@Override
		public void hanlde(Connect connect) {
			reconnectToBroker(connect);
		}

		@Override
		public void handle(Disconnect disconnect) {
			if (disconnect.isWithDuration()) {
				handleSleep(disconnect.getDuration());
			} else {
				disconnect(true);
			}

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
		
		@Override
		public void handle(Subscribe subscribe) {
			subscribe(subscribe);
		}

		@Override
		public void handle(PingReq pingreq) {
			sendPingresp();
		}

		@Override
		public void handle(PingResp pingResp) {
		   // currently nothing to do
		}

		@Override
		public void handleMessageArrived(String topic, byte[] payload, int qos, boolean retain) {
			forwardMessageToClient(topic, payload, qos, retain);			
		}

	}

	private class Asleep implements State {

		@Override
		public void hanlde(Connect connect) {
			if(connect.isCleanSession()) {
	      LOG.warn("connect from Asleep with clean session set => reconnect");
	      reconnectToBroker(connect);
	   } else {
	      connectFromSleep(connect);
	   }	
		}
		
		@Override
		public void handle(Disconnect disconnect) {
			// we are in asleep, but lets do it anyway
		  disconnect(true);
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
			// we are in asleep, but lets do it anyway
		   publishToBroker(publish);	
		}
		
		@Override
		public void handle(Subscribe subscribe) {
			// we are in asleep, but lets do it anyway
			subscribe(subscribe);
		}
		
		@Override
		public void handle(PingReq pingreq) {
			if(pingreq.isWithClientId()) {
				awake();
			} else {
				sendPingresp();
			}
		}

		@Override
		public void handle(PingResp pingResp) {
			// currently nothing to do		
		}
		
		@Override
		public void handleMessageArrived(String topic, byte[] payload, int qos, boolean retain) {
			storeMessage(topic, payload, qos, retain);
		}

	}
	
	private static class BufferedMessage {
		private final String mTopic; 
		private final byte[] mPayload;
		private final int mQos; 
		private final boolean mRetain;
		
		public BufferedMessage(String topic, byte[] payload, int qos, boolean retain) {
			super();
			mTopic = topic;
			mPayload = payload;
			mQos = qos;
			mRetain = retain;
		}

		public String getTopic() {
			return mTopic;
		}

		public byte[] getPayload() {
			return mPayload;
		}

		public int getQos() {
			return mQos;
		}

		public boolean isRetain() {
			return mRetain;
		}
		
	}
	
}
