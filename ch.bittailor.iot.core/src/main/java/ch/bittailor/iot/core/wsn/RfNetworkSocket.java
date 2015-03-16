package ch.bittailor.iot.core.wsn;

import java.nio.ByteBuffer;

public interface RfNetworkSocket extends AutoCloseable {

	int payloadCapacity();

	boolean send(RfSocketAddress destination, ByteBuffer packet);
	void startListening(Listener listener);
	void stopListening();

	public static interface Listener {
		void packetReceived(RfSocketAddress source, ByteBuffer packet);
	}


}
