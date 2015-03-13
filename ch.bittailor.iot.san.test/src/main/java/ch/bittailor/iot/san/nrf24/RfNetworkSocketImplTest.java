package ch.bittailor.iot.san.nrf24;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import ch.bittailor.iot.devices.nrf24.RfDeviceController;
import ch.bittailor.iot.devices.nrf24.RfPipe;

public class RfNetworkSocketImplTest {

	private static final int OWN_ADDRESS = 0;
	
	private RfDeviceController mController;
	private RfNetworkSocketImpl mSocket;
	
	@Before
	public void Setup() {
		mController = mock(RfDeviceController.class);
		mSocket = new RfNetworkSocketImpl(new RfSocketAddress(OWN_ADDRESS), mController);
	}
	
	
	@Test
	public void testSend() {
		int destinationAddress = 1;
		RfSocketAddress destination = new RfSocketAddress(destinationAddress);
		byte[] payload = new byte[]{9,8,7,6};
		ByteBuffer packet = ByteBuffer.wrap(payload);
		
		when(mController.write(any(RfPipe.class), any(ByteBuffer.class))).thenReturn(true);
		
		mSocket.send(destination, packet);
		
		ArgumentCaptor<ByteBuffer> bufferArgument = ArgumentCaptor.forClass(ByteBuffer.class);
		verify(mController).write(any(RfPipe.class), bufferArgument.capture());
		ByteBuffer buffer = bufferArgument.getValue();
		assertEquals(7, buffer.remaining());
		assertEquals(OWN_ADDRESS, buffer.get());
		assertEquals(destinationAddress, buffer.get());
		buffer.get();
		byte[] array = new byte[4];
		buffer.get(array);
		assertArrayEquals(payload, array);
	}
}
