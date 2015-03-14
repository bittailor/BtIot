package ch.bittailor.iot.core.wsn;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.bittailor.iot.core.wsn.RfSocketAddress;

public class RfSocketAddressTest {

	@Test
	public void equal() {
		assertTrue(new RfSocketAddress(1).equals(new RfSocketAddress(1)));
	}
	
	@Test
	public void verifyLevel0() {
		assertEquals(0, new RfSocketAddress(0).getLevel());
	}
	
	@Test
	public void verifyLevel1() {
		for(int i = 1 ; i <= 5 ; i++ ) {
			assertEquals("for i = " + i, 1, new RfSocketAddress(i).getLevel());
		}
	}
	
	@Test
	public void verifyLevel2() {
		for(int i = 6 ; i <= 30 ; i++ ) {
			assertEquals("for i = " + i, 2, new RfSocketAddress(i).getLevel());
		}
	}
	
	@Test
	public void verifyLevel3() {
		for(int i = 31 ; i <= 155 ; i++ ) {
			assertEquals("for i = " + i, 3, new RfSocketAddress(i).getLevel());
		}
	}
	
	@Test
	public void verifyLevel4() {
		for(int i = 156 ; i <= 255 ; i++ ) {
			assertEquals("for i = " + i, 4, new RfSocketAddress(i).getLevel());
		}
	}
	
	

}
