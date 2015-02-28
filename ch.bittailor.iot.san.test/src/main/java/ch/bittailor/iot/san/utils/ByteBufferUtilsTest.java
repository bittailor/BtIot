package ch.bittailor.iot.san.utils;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

public class ByteBufferUtilsTest {

	@Test
	public void signleIntegerToString() {
		ByteBuffer buffer = ByteBuffer.allocate(20);
		buffer.putInt(0x12345678);
		buffer.flip();
		assertEquals("12345678", Utilities.toHexString(buffer));
	}

}
