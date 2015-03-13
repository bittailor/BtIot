package ch.bittailor.iot.mqttsn.gateway;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bittailor.iot.mqttsn.messages.Message;
import ch.bittailor.iot.mqttsn.messages.MessageFactory;
import ch.bittailor.iot.mqttsn.messages.MessageFactoryException;
import ch.bittailor.iot.san.PacketSocket;
import ch.bittailor.iot.san.nrf24.RfSocketAddress;

public class Gateway implements Closeable {
	private static final Logger LOG = LoggerFactory.getLogger(Gateway.class);
	
	private final MessageFactory mMessageFactory;
	private final ConnectionFactory mConnectionFactory;
	private final PacketSocket mPacketSocket;
	private final ExecutorService mExecutorService;
	private ConcurrentHashMap<RfSocketAddress,GatewayConnection> mConnections; 
	
	public Gateway(MessageFactory messageFactory, ConnectionFactory connectionFactory, PacketSocket packetSocket, ExecutorService executorService) {
		mMessageFactory = messageFactory;
		mConnectionFactory = connectionFactory;
		mPacketSocket = packetSocket;
		mExecutorService = executorService; 
		mConnections = new ConcurrentHashMap<RfSocketAddress, GatewayConnection>();
		mPacketSocket.setListener(new PacketSocket.Listener() {
			@Override
			public void received(final RfSocketAddress source, final ByteBuffer payload) {
				mExecutorService.execute(new Runnable() {				
					@Override
					public void run() {
						onRfPacketReceived(source,payload);
					}
				});
			}
		});
	}
	
	@Override
	public void close() throws IOException {
		mPacketSocket.resetListener();
	}

	private void onRfPacketReceived(RfSocketAddress source, ByteBuffer payload) {	
		if(payload.remaining() <= 0) {
			return;
		}
		try {
			Message message = mMessageFactory.createMessage(payload);		
			GatewayConnection connection = mConnections.get(source);
			if(connection == null) {
				LOG.debug("Create new gateway connection for address " + source) ;
				connection = mConnectionFactory.create(source);
				mConnections.put(source, connection);
			}
			connection.handle(message);	
		} catch(MessageFactoryException exception) {
			LOG.warn("Failed to parse incomming packet", exception);
		}
	}
}
