package ch.bittailor.iot.san.nrf24;

import java.io.Closeable;
import java.nio.ByteBuffer;

public interface RfPacketSocket extends Closeable {
	
	int payloadCapacity();
	int send(RfSocketAddress destination, ByteBuffer payload);
	void setListener(Listener listener);
	void resetListener();
	
	public static interface Listener {
		void received(RfSocketAddress source, ByteBuffer payload);
	}

	 
}
