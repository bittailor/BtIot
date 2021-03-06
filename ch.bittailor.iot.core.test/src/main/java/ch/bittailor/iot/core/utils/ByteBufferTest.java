package ch.bittailor.iot.core.utils;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

public class ByteBufferTest {

	@Test
	public void emptyBuffer_remainingAfterFlip() {
		ByteBuffer buffer = ByteBuffer.allocate(20);
		buffer.flip();
		assertEquals(0, buffer.remaining());
	}
	
	@Test
	public void emptyBuffer_limitAfterFlip() {
		ByteBuffer buffer = ByteBuffer.allocate(20);
		buffer.flip();
		assertEquals(0, buffer.limit());
	}
	
	@Test
	public void buffer_limitAfterFlip() {
		ByteBuffer buffer = ByteBuffer.allocate(20);
		buffer.putInt(0x11223344);
		buffer.flip();
		assertEquals(4, buffer.limit());
	}
	
	@Test
	public void buffer_remainingAfterFlipAndGet() {
		ByteBuffer buffer = ByteBuffer.allocate(20);
		buffer.putInt(0x11223344);
		buffer.flip();
		byte value = buffer.get();
		assertEquals(3, buffer.remaining());
		assertEquals(4, buffer.limit());
		assertEquals(0x11, value);
	}
	
	@Test
	public void buffer_remainingAfterFlipAndGetAtIndex() {
		ByteBuffer buffer = ByteBuffer.allocate(20);
		buffer.putInt(0x11223344);
		buffer.flip();
		byte value = buffer.get(0);
		assertEquals(4, buffer.remaining());
		assertEquals(0x11, value);
	}
	
	@Test
	public void bufferFromArray_remaining() {
		ByteBuffer buffer = ByteBuffer.wrap(new byte[]{1,2,3,4,5});
		assertEquals(5, buffer.remaining());
	}


}
