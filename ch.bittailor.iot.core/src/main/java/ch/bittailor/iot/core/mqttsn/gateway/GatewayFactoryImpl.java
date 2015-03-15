package ch.bittailor.iot.core.mqttsn.gateway;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import ch.bittailor.iot.core.mqttsn.messages.MessageFactoryImpl;
import ch.bittailor.iot.core.wsn.PacketSocket;

public class GatewayFactoryImpl {
	public Gateway create(PacketSocket socket) {
		
		ExecutorService executor = Executors.newSingleThreadExecutor( new ThreadFactory() {	
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "mqtt-sn gateway executor");
			}
		});
		
		return new Gateway(
				new MessageFactoryImpl(), 
				new ConnectionFactoryImpl(),
				socket, 
				executor);
	}
}
