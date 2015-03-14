package ch.bittailor.iot.core.devices.nrf24;

import java.nio.ByteBuffer;

public class RfAddress {
	private final byte[] mRaw = new byte[5];
	
	public RfAddress(ByteBuffer address) {
		address.get(this.mRaw);
	}

	public RfAddress(int byte4, int byte3, int byte2, int byte1, int byte0) {
		mRaw[0] = (byte)byte0;
		mRaw[1] = (byte)byte1;
		mRaw[2] = (byte)byte2;
		mRaw[3] = (byte)byte3;
		mRaw[4] = (byte)byte4;
	}

	public byte[] raw() {
		return mRaw;
	}

}
