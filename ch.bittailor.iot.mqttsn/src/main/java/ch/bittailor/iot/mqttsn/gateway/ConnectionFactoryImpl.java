package ch.bittailor.iot.mqttsn.gateway;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import ch.bittailor.iot.san.nrf24.RfSocketAddress;

public class ConnectionFactoryImpl implements ConnectionFactory {

	@Override
	public GatewayConnection create(final RfSocketAddress address) {
		return new GatewayConnectionImpl(Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "Connection " + address + " executor");
			}
		}) ,address);
	}

}
