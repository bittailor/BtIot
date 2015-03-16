package ch.bittailor.iot.core.mqttsn.gateway;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import ch.bittailor.iot.core.wsn.PacketSocket;
import ch.bittailor.iot.core.wsn.RfSocketAddress;

public class ConnectionFactoryImpl implements ConnectionFactory {

	private final MqttClientFactory mFactory;
		
	public ConnectionFactoryImpl(MqttClientFactory factory) {
		mFactory = factory;
	}

	@Override
	public GatewayConnection create(final RfSocketAddress address, PacketSocket packetSocket) {
		return new GatewayConnectionImpl(Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "Connection " + address + " executor");
			}
		}) ,address, packetSocket, mFactory);
	}

}
