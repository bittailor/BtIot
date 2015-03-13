package ch.bittailor.iot.san.nrf24;

import java.io.Closeable;
import java.nio.ByteBuffer;

public interface RfNetworkSocket extends Closeable {

	int payloadCapacity();

	boolean send(RfSocketAddress destination, ByteBuffer packet);
	void startListening(Listener listener);
	void stopListening();

	public static interface Listener {
		void packetReceived(RfSocketAddress source, ByteBuffer packet);
	}


}
