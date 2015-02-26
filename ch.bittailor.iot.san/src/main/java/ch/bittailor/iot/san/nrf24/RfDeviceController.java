package ch.bittailor.iot.san.nrf24;

import java.nio.ByteBuffer;

public interface RfDeviceController {
	
	boolean write(RfPipe iPipe, ByteBuffer iPacket);
}
