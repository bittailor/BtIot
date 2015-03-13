package ch.bittailor.iot.san.nrf24;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.bittailor.iot.devices.nrf24.RfPipe;

public class RfNetworkRoutingAlgorithmTest {

	private final RfNetworkRoutingAlgorithm mRouting = new RfNetworkRoutingAlgorithm();
	
	@Test
	public void routingFromTopNode() {
		assertEquals(RfPipe.PIPE_1, mRouting.calculateRoutingPipe(0, 1));
		assertEquals(RfPipe.PIPE_2, mRouting.calculateRoutingPipe(0, 2));
		assertEquals(RfPipe.PIPE_3, mRouting.calculateRoutingPipe(0, 3));
		assertEquals(RfPipe.PIPE_4, mRouting.calculateRoutingPipe(0, 4));
		assertEquals(RfPipe.PIPE_5, mRouting.calculateRoutingPipe(0, 5));
	}

	@Test
	public void routingToParentWhenDestinationAtLowerLevel() {
		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 0));
		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 1));
		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 2));
		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 3));
	}

	@Test
	public void routingToParentWhenDestinationAtSameLevel() {
		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 6));
		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 7));
		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 10));

		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 11));
		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 13));

		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 16));
		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 20));
		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 30));
	}

	@Test
	public void routingToParentWhenHigherLevelButNotChild() {
		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 31));
		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 32));
		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 35));

		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 66));
		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 70));

		assertEquals(RfPipe.PIPE_0, mRouting.calculateRoutingPipe(12, 176));
	}

	@Test
	public void routingToChildOneLevel() {
		assertEquals(RfPipe.PIPE_1, mRouting.calculateRoutingPipe(12, 61));
		assertEquals(RfPipe.PIPE_2, mRouting.calculateRoutingPipe(12, 62));
		assertEquals(RfPipe.PIPE_3, mRouting.calculateRoutingPipe(12, 63));
		assertEquals(RfPipe.PIPE_4, mRouting.calculateRoutingPipe(12, 64));
		assertEquals(RfPipe.PIPE_5, mRouting.calculateRoutingPipe(12, 65));
	}

	@Test
	public void routingToChildMultipleLevel() {
		assertEquals(RfPipe.PIPE_1, mRouting.calculateRoutingPipe(7, 181));
		assertEquals(RfPipe.PIPE_1, mRouting.calculateRoutingPipe(7, 182));
		assertEquals(RfPipe.PIPE_1, mRouting.calculateRoutingPipe(7, 183));
		assertEquals(RfPipe.PIPE_1, mRouting.calculateRoutingPipe(7, 184));
		assertEquals(RfPipe.PIPE_1, mRouting.calculateRoutingPipe(7, 185));

		assertEquals(RfPipe.PIPE_2, mRouting.calculateRoutingPipe(7, 186));
		assertEquals(RfPipe.PIPE_2, mRouting.calculateRoutingPipe(7, 187));
		assertEquals(RfPipe.PIPE_2, mRouting.calculateRoutingPipe(7, 188));
		assertEquals(RfPipe.PIPE_2, mRouting.calculateRoutingPipe(7, 189));
		assertEquals(RfPipe.PIPE_2, mRouting.calculateRoutingPipe(7, 190));

		assertEquals(RfPipe.PIPE_3, mRouting.calculateRoutingPipe(7, 191));
		assertEquals(RfPipe.PIPE_3, mRouting.calculateRoutingPipe(7, 192));
		assertEquals(RfPipe.PIPE_3, mRouting.calculateRoutingPipe(7, 193));
		assertEquals(RfPipe.PIPE_3, mRouting.calculateRoutingPipe(7, 194));
		assertEquals(RfPipe.PIPE_3, mRouting.calculateRoutingPipe(7, 195));

		assertEquals(RfPipe.PIPE_4, mRouting.calculateRoutingPipe(7, 196));
		assertEquals(RfPipe.PIPE_4, mRouting.calculateRoutingPipe(7, 197));
		assertEquals(RfPipe.PIPE_4, mRouting.calculateRoutingPipe(7, 198));
		assertEquals(RfPipe.PIPE_4, mRouting.calculateRoutingPipe(7, 199));
		assertEquals(RfPipe.PIPE_4, mRouting.calculateRoutingPipe(7, 200));

		assertEquals(RfPipe.PIPE_5, mRouting.calculateRoutingPipe(7, 201));
		assertEquals(RfPipe.PIPE_5, mRouting.calculateRoutingPipe(7, 202));
		assertEquals(RfPipe.PIPE_5, mRouting.calculateRoutingPipe(7, 203));
		assertEquals(RfPipe.PIPE_5, mRouting.calculateRoutingPipe(7, 204));
		assertEquals(RfPipe.PIPE_5, mRouting.calculateRoutingPipe(7, 205));
	}

}
