package ch.bittailor.iot.san;

import java.nio.ByteBuffer;

import ch.bittailor.iot.san.nrf24.RfSocketAddress;

public interface PacketSocket {
	
	 int send(ByteBuffer payload, RfSocketAddress destination);
	 RfSocketAddress receive(ByteBuffer payload);
	 
}
