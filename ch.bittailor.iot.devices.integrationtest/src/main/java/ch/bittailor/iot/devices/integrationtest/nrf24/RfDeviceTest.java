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
	

	
	
}
