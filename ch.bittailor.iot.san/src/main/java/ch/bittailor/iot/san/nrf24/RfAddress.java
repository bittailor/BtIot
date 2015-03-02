package ch.bittailor.iot.san.nrf24;

import java.nio.ByteBuffer;

public class RfAddress {
	private final byte[] mAddress = new byte[5];
	
	public RfAddress(ByteBuffer address) {
		address.get(this.mAddress);
	}
	
	public byte[] raw() {
		return mAddress;
	}

}
