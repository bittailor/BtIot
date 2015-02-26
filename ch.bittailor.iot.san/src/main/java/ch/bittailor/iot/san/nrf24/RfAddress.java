package ch.bittailor.iot.san.nrf24;

import java.nio.ByteBuffer;

public class RfAddress {
	private final byte[] address = new byte[5];
	
	public RfAddress(ByteBuffer address) {
		address.get(this.address);
	}
	
	public byte[] raw() {
		return address;
	}

}
