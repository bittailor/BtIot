package ch.bittailor.iot.san.nrf24;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jdk.dio.Device;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;
import jdk.dio.spibus.SPIDevice;
import jdk.dio.spibus.SPIDeviceConfig;

public class RfPacketSocketFactoryImpl implements RfPacketSocketFactory {

	@Override
	public RfPacketSocket create() {
		try {
			// TODO move to configuration
			int networkAddress = 0;
			int powerPin = 27;
			int chipEnablePin = 25;
			int interruptPin = 24;

			ExecutorService executor = Executors.newSingleThreadExecutor();
			
			GPIOPin power = DeviceManager.open(powerPin);
			power.setDirection(GPIOPin.OUTPUT);
			
			GPIOPin chipEnable = DeviceManager.open(chipEnablePin);
			chipEnable.setDirection(GPIOPin.OUTPUT);
			
			GPIOPin interrupt = DeviceManager.open(interruptPin);
			interrupt.setDirection(GPIOPin.INPUT);
			interrupt.setTrigger(GPIOPinConfig.TRIGGER_FALLING_EDGE);
		
			SPIDeviceConfig spiConfig = new SPIDeviceConfig(0, 0,
					SPIDeviceConfig.CS_ACTIVE_LOW,
					500000,
					3,  
					8,
					Device.BIG_ENDIAN);
			SPIDevice spi = DeviceManager.open(SPIDevice.class, spiConfig);
			
			RfDeviceImpl device = new RfDeviceImpl(spi);
			
			RfDeviceControllerImpl deviceController = new RfDeviceControllerImpl(
					executor, device,  power , chipEnable,  interrupt);

			RfNetworkSocketImpl networkSocket = new RfNetworkSocketImpl(
					new RfSocketAddress(networkAddress), 
					deviceController);

			return new RfPacketSocketImpl(executor, networkSocket);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
