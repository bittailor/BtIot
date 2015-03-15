package ch.bittailor.iot.core.mqttsn.messages;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

public class ConnectTest {

	@Test
	public void Connect_WithCleanSession() {
		
		ByteBuffer buffer = ByteBuffer.wrap(new byte[]{0x04,0x01,0x23,0x45,'J','u','s','t','A','n','I','D'});
		Connect connect = new Connect(buffer);		
		
		assertTrue(connect.isCleanSession());
		assertFalse(connect.isWill());
		assertEquals(0x2345,connect.getDuration());
		assertEquals("JustAnID", connect.getClientId());	
	}
	
	@Test
	public void Connect_WithWill() {
		
		ByteBuffer buffer = ByteBuffer.wrap(new byte[]{0x08,0x01,0x01,0x10,'A','n','o','t','h','e','r','I','D'});
		Connect connect = new Connect(buffer);		
		
		assertFalse(connect.isCleanSession());
		assertTrue(connect.isWill());
		assertEquals(0x0110,connect.getDuration());
		assertEquals("AnotherID", connect.getClientId());
	}
	
	@Test
	public void Connect_() {
		ByteBuffer buffer = ByteBuffer.allocate(20);
		Connect connect = new Connect("YetAnother");
		connect.writeToBuffer(buffer);
		buffer.flip();
		
		
		assertEquals(buffer.remaining(),buffer.get(0));
		byte[] act = new byte[buffer.remaining()];
		byte[] exp = new byte[]{(byte)buffer.remaining(),0x04,0x04,0x01,(byte)0xff,(byte)0xff,'Y','e','t','A','n','o','t','h','e','r'};
		buffer.get(act);
		assertArrayEquals(exp,act);
		
		
		
		
		
		//assertFalse(connect.isCleanSession());
		//assertTrue(connect.isWill());
		//assertEquals(0x0110,connect.getDuration());
		//assertEquals("AnotherID", connect.getClientId());
	}
	
	
	

}
