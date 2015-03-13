package ch.bittailor.iot.devices.integrationtest.nrf24;

import static org.junit.Assert.*;

import java.io.IOException;

import jdk.dio.DeviceManager;
import jdk.dio.DeviceNotFoundException;
import jdk.dio.UnavailableDeviceException;
import jdk.dio.gpio.GPIOPin;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.bittailor.iot.devices.nrf24.RfAddress;
import ch.bittailor.iot.devices.nrf24.RfDevice;
import ch.bittailor.iot.devices.nrf24.RfDeviceFactory;
import ch.bittailor.iot.devices.nrf24.RfPipe;

public class RfDeviceTest {

	private GPIOPin mPower;
	private RfDevice mRfDevice;
	
	@Before
	public void Before() throws DeviceNotFoundException, UnavailableDeviceException, IOException {
		mPower = DeviceManager.open(27);
		mRfDevice = new RfDeviceFactory().create();
		mPower.setValue(true);
	}
	
	@After
	public void After() throws IOException, InterruptedException {
		mPower.setValue(false);
		mRfDevice.close();
		mPower.close();
		Thread.sleep(100);
	}
	
	@Test
	public void powerUp_default() {
		assertEquals(false, mRfDevice.powerUp());
	}
	
	@Test
	public void powerUp_writeAndReadBack() {
		mRfDevice.powerUp(true);
		assertEquals(true, mRfDevice.powerUp());
	}
	
	@Test
	public void transceiverMode_default() {
		assertEquals(RfDevice.TransceiverMode.TX_MODE, mRfDevice.transceiverMode());
	}
	
	@Test
	public void transceiverMode_writeAndReadBack() {
		mRfDevice.transceiverMode(RfDevice.TransceiverMode.RX_MODE);
		assertEquals(RfDevice.TransceiverMode.RX_MODE, mRfDevice.transceiverMode());
	}
	
	@Test
	public void channel_default() {
		assertEquals(0x2, mRfDevice.channel());
	}
	
	@Test
	public void channel_writeAndReadBack() {
		int channel = 0x3a;
		mRfDevice.channel(channel);
		assertEquals(channel, mRfDevice.channel());
	}
	
	@Test
	public void dataRate_default() {
		assertEquals(RfDevice.DataRate.DR_2_MBPS, mRfDevice.dataRate());
	}
	
	@Test
	public void dataRate_writeAndReadBack_1_MBPS() {
		mRfDevice.dataRate(RfDevice.DataRate.DR_1_MBPS);
		assertEquals(RfDevice.DataRate.DR_1_MBPS, mRfDevice.dataRate());
	}
	
	@Test
	public void dataRate_writeAndReadBack_250_KBPS() {
		mRfDevice.dataRate(RfDevice.DataRate.DR_250_KBPS);
		assertEquals(RfDevice.DataRate.DR_250_KBPS, mRfDevice.dataRate());
	}
	
	@Test
	public void autoRetransmitCount_default() {
		assertEquals(0x03, mRfDevice.autoRetransmitCount());
	}
	
	@Test
	public void autoRetransmitCount_writeAndReadBack() {
		int count = 0x0a;
		mRfDevice.autoRetransmitCount(count);
		assertEquals(count, mRfDevice.autoRetransmitCount());
	}
	
	@Test
	public void autoRetransmitDelay_default() {
		assertEquals(0x00, mRfDevice.autoRetransmitDelay());
	}
	
	@Test
	public void autoRetransmitDelay_writeAndReadBack() {
		int delay = 0x04;
		mRfDevice.autoRetransmitDelay(delay);
		assertEquals(delay, mRfDevice.autoRetransmitDelay());
	}
	
	@Test
	public void writeToSameRegister() {	
	   int delay = 0x04;
	   int count = 0x0a;
	   mRfDevice.autoRetransmitDelay(delay);
	   assertEquals(delay,mRfDevice.autoRetransmitDelay());
	   assertEquals(0x03,mRfDevice.autoRetransmitCount());
	   mRfDevice.autoRetransmitCount(count);
	   assertEquals(delay,mRfDevice.autoRetransmitDelay());
	   assertEquals(count,mRfDevice.autoRetransmitCount());
	}

	
	@Test
	public void receiveAddress_default() {
		RfAddress[] defaultAddresses = new RfAddress[]  {
				new RfAddress(0xE7,0xE7, 0xE7, 0xE7, 0xE7),
				new RfAddress(0xC2,0xC2, 0xC2, 0xC2, 0xC2),
				new RfAddress(0xC2,0xC2, 0xC2, 0xC2, 0xC3),
				new RfAddress(0xC2,0xC2, 0xC2, 0xC2, 0xC4),
				new RfAddress(0xC2,0xC2, 0xC2, 0xC2, 0xC5),
				new RfAddress(0xC2,0xC2, 0xC2, 0xC2, 0xC6)
		};
		
		RfPipe[] pipes = RfPipe.values();
		
		for (int i = 0; i < pipes.length; i++) {
			RfAddress address = mRfDevice.receiveAddress(pipes[i]);
			assertArrayEquals("for pipe " + pipes[i].name(), defaultAddresses[i].raw(), address.raw());
		}		
	}
	
	@Test
	public void receiveAddress_writeAndReadBack() {
		RfAddress[] addresses = new RfAddress[]  {
				new RfAddress(0x22, 0x23, 0x24, 0x25, 0x26),
				new RfAddress(0xA1, 0xB1, 0xC1, 0xD1, 0xE1),
				new RfAddress(0xA1, 0xB1, 0xC1, 0xD1, 0xE2),
				new RfAddress(0xA1, 0xB1, 0xC1, 0xD1, 0xE3),
				new RfAddress(0xA1, 0xB1, 0xC1, 0xD1, 0xE4),
				new RfAddress(0xA1, 0xB1, 0xC1, 0xD1, 0xE5)
		};
		
		RfPipe[] pipes = RfPipe.values();
		
		for (int i = 0; i < pipes.length; i++) {
			mRfDevice.receiveAddress(pipes[i], addresses[i]);
		}	
		
		for (int i = 0; i < pipes.length; i++) {
			RfAddress address = mRfDevice.receiveAddress(pipes[i]);
			assertArrayEquals("for pipe " + pipes[i].name(), addresses[i].raw(), address.raw());
		}		
	}
	
	@Test
	public void receivePipeEnabled_default() {
		boolean[] defaults = new boolean[]  {
				true,
        true,
        false,
        false,
        false,
        false
		};
		
		RfPipe[] pipes = RfPipe.values();
		
		for (int i = 0; i < pipes.length; i++) {
			assertEquals("for pipe " + pipes[i].name(), defaults[i], mRfDevice.receivePipeEnabled(pipes[i]));
		}		
	}
	
	@Test
	public void receivePipeEnabled_writeAndReadBack() {
		boolean[] values = new boolean[]  {
				false,
        false,
        true,
        true,
        true,
        true
		};
		
		RfPipe[] pipes = RfPipe.values();
		
		for (int i = 0; i < pipes.length; i++) {
			mRfDevice.receivePipeEnabled(pipes[i],values[i]);
		}	
		
		for (int i = 0; i < pipes.length; i++) {
			assertEquals("for pipe " + pipes[i].name(), values[i], mRfDevice.receivePipeEnabled(pipes[i]));
		}	
		
		
		
	}
	

	
	
}
