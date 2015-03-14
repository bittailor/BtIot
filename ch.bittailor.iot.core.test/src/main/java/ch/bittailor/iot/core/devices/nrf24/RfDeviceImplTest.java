package ch.bittailor.iot.core.devices.nrf24;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.ByteBuffer;

import jdk.dio.ClosedDeviceException;
import jdk.dio.UnavailableDeviceException;
import jdk.dio.spibus.SPIDevice;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import ch.bittailor.iot.core.devices.nrf24.RfDeviceImpl;



public class RfDeviceImplTest {

	private SPIDevice mSpi; 
	private RfDeviceImpl mRfDevice; 
	
	@Before
	public void Before() {
		mSpi = mock(SPIDevice.class);
		mRfDevice = new RfDeviceImpl(mSpi);
		
	}
	
	@Test
	public void writeTransmitPayload_checkWriteBuffer() throws UnavailableDeviceException, ClosedDeviceException, IOException {
		byte[] array = new byte[]{1,2,3,4,5,6,7,8};
		ByteBuffer payload = ByteBuffer.wrap(array);
			
		mRfDevice.writeTransmitPayload(payload);
		ArgumentCaptor<ByteBuffer> bufferArgument = ArgumentCaptor.forClass(ByteBuffer.class);
		
		verify(mSpi).writeAndRead(bufferArgument.capture(), any(ByteBuffer.class));
		ByteBuffer sendBuffer = bufferArgument.getValue();
		assertEquals(9, sendBuffer.remaining());
		assertEquals((byte)0xA0, sendBuffer.get());
		for (int i = 0; i < array.length; i++) {
			assertEquals(array[i], sendBuffer.get());
		}
		assertEquals(0, sendBuffer.remaining());
	}

}
