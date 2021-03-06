package ch.bittailor.iot.core.wsn;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import ch.bittailor.iot.core.devices.nrf24.RfDeviceControllerImpl;
import ch.bittailor.iot.core.devices.nrf24.RfDeviceImpl;
import jdk.dio.Device;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;
import jdk.dio.spibus.SPIDevice;
import jdk.dio.spibus.SPIDeviceConfig;

public class RfPacketSocketFactoryImpl implements RfPacketSocketFactory {

	@Override
	public RfPacketSocket create(int networkAddress) {
		try {
			// TODO move to configuration
			int powerPin = 18;
			int chipEnablePin = 25;
			int interruptPin = 24;

			ExecutorService executor = Executors.newSingleThreadExecutor( new ThreadFactory() {
				
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r, "RF socket executor");
					thread.setPriority(Thread.MAX_PRIORITY);
					return thread;
				}
			});
			
			GPIOPin power = DeviceManager.open(powerPin);
			power.setDirection(GPIOPin.OUTPUT);
			
			GPIOPin chipEnable = DeviceManager.open(chipEnablePin);
			chipEnable.setDirection(GPIOPin.OUTPUT);
			
			GPIOPin interrupt = DeviceManager.open(interruptPin);
			interrupt.setDirection(GPIOPin.INPUT);
			interrupt.setTrigger(GPIOPinConfig.TRIGGER_FALLING_EDGE);
		
			SPIDeviceConfig spiConfig = new SPIDeviceConfig(0, 0,
					SPIDeviceConfig.CS_ACTIVE_LOW,
					8000000,            // clockFrequency
					0,                  // mode 
					8,                  // wordLength	
					Device.BIG_ENDIAN); // bitOrdering
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
