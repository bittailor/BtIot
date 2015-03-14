package ch.bittailor.iot.san;

import java.nio.ByteBuffer;

import ch.bittailor.iot.core.wsn.RfSocketAddress;

public interface PacketSocket {

	int payloadCapacity();
	int send(RfSocketAddress destination, ByteBuffer payload);
	void setListener(Listener listener);
	void resetListener();
	
	public static interface Listener {
		void received(RfSocketAddress source, ByteBuffer payload);
	}

}
