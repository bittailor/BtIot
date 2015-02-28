package ch.bittailor.iot.san.nrf24;

import java.io.Closeable;
import java.nio.ByteBuffer;

public interface RfPacketSocket extends Closeable {
	
	 int payloadCapacity();
	 
	 int send(ByteBuffer payload, RfSocketAddress destination);
	 RfSocketAddress receive(ByteBuffer payload);
	 
}
