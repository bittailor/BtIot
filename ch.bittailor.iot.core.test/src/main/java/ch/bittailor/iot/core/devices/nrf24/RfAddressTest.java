package ch.bittailor.iot.core.devices.nrf24;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.bittailor.iot.core.devices.nrf24.RfAddress;

public class RfAddressTest {

	@Test
	public void RfAddress() {
		byte[] exp = new byte[]{(byte)0xE1, (byte)0xE2, (byte)0xE3, (byte)0xE4, (byte)0xE5};
		RfAddress address = new RfAddress(0xE5, 0xE4, 0xE3, 0xE2, 0xE1);
		assertArrayEquals(exp,address.raw());
	}

}
