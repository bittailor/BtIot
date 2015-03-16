package ch.bittailor.iot.core.wsn;

import java.nio.ByteBuffer;

public interface RfPacketSocket extends AutoCloseable {
	
	int payloadCapacity();
	int send(RfSocketAddress destination, ByteBuffer payload);
	void setListener(Listener listener);
	void resetListener();
	
	public static interface Listener {
		void received(RfSocketAddress source, ByteBuffer payload);
	}

	 
}
