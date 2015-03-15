package ch.bittailor.iot.core.devices.nrf24;

import jdk.dio.Device;
import jdk.dio.DeviceManager;
import jdk.dio.spibus.SPIDevice;
import jdk.dio.spibus.SPIDeviceConfig;

public class RfDeviceFactory {

	public RfDevice create() {
		try {
			SPIDeviceConfig spiConfig = new SPIDeviceConfig(
					0, 
					0,
					SPIDeviceConfig.CS_ACTIVE_LOW,  //
					8000000,            						// clockFrequency
					0,                 						 	// mode 
					8,                  						// wordLength	
					Device.BIG_ENDIAN); 						// bitOrdering
			SPIDevice spi = DeviceManager.open(SPIDevice.class, spiConfig);
			return new RfDeviceImpl(spi);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
