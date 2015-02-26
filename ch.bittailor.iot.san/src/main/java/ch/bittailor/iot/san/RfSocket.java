package ch.bittailor.iot.san;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import jdk.dio.ClosedDeviceException;
import jdk.dio.Device;
import jdk.dio.DeviceManager;
import jdk.dio.UnavailableDeviceException;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.spibus.SPIDevice;
import jdk.dio.spibus.SPIDeviceConfig;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RfSocket {
	
	private static byte CMD_R_REGISTER = 0x00;
	private static byte CMD_NOP = (byte)0xFF;
	private static byte REGISTER_RX_ADDR_P0 = 0x0A;
	private static byte REGISTER_RX_ADDR_P1 = 0x0B;
	private static byte MASK_REGISTER_CMD = 0x1F;
	
	
	private static final Logger s_logger = LoggerFactory.getLogger(RfSocket.class);
	private static final String APP_ID = "org.eclipse.kura.example.configurable.ConfigurableExample";
	private Map<String, Object> properties;
	private GPIOPin power;

	protected void activate(ComponentContext componentContext) {
		s_logger.info("Bundle " + APP_ID + " has started -- bt");
		ledOn(); 
		//tryI2C();
		readAddress();
	}

	protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
		s_logger.info("Bundle " + APP_ID + " has started with config  -- bt");
		updated(properties);
		ledOn(); 
		//tryI2C();
		readAddress();
	}

	protected void deactivate(ComponentContext componentContext) {
		s_logger.info("Bundle " + APP_ID + " has stopped!");
		ledOff();
	}

	public void updated(Map<String, Object> properties) {
		this.properties = properties;
		if(properties != null && !properties.isEmpty()) {
			Iterator<Entry<String, Object>> it = properties.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Object> entry = it.next();
				s_logger.info("New property - " + entry.getKey() + " = " +
						entry.getValue() + " of type " + entry.getValue().getClass().toString());
			}
		}
	}
	
	
	private void ledOn() {
		try {
			SPIDeviceConfig config = new SPIDeviceConfig(0, 0,
					SPIDeviceConfig.CS_ACTIVE_LOW,
					500000,
					3,  
					8,
					Device.BIG_ENDIAN);
			
			s_logger.info("address is {}",config.getAddress());
			
			
			power = DeviceManager.open(18);
			power.setDirection(GPIOPin.OUTPUT);
			power.setValue(true);
			
		} catch (Exception e) {
			s_logger.error("ledOn failed ", e);
		}
	}
	
	private void ledOff() {
		try {
			power.setValue(false);
			power.close();
			power = null;
		} catch (Exception e) {
			s_logger.error("ledOff failed ", e);
		}
	}
	
	
	
	
	private void readAddress() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					s_logger.info("readAddress");
					SPIDeviceConfig config = new SPIDeviceConfig(0, 0,
							SPIDeviceConfig.CS_ACTIVE_LOW,
							8000000,
							3,  
							8,
							Device.BIG_ENDIAN);
			
					try (
						SPIDevice spi = DeviceManager.open(SPIDevice.class, config);
						GPIOPin power = DeviceManager.open(27);
					){
			
						s_logger.info("devices opened");
			
						power.setDirection(GPIOPin.OUTPUT);
						power.setValue(true);
			
						s_logger.info("power in wait 2000ms");
						Thread.sleep(2000);
			
			
						ByteBuffer addressP0 = readRegister(spi,REGISTER_RX_ADDR_P0);
						
						s_logger.info("address P0 is 0x{}", 
								bytesToHex(addressP0.array()));
						
						ByteBuffer addressP1 = readRegister(spi,REGISTER_RX_ADDR_P1);
						
						s_logger.info("address P1 is 0x{}", 
								bytesToHex(addressP1.array()));
						
						
						s_logger.info("wait 2000ms");
						
						Thread.sleep(2000);
						
						power.setValue(false);
						s_logger.info("power off wait 2000ms");
						Thread.sleep(2000);
											
					} catch (IOException ioe) {
						s_logger.error("Failed", ioe);
					} catch (InterruptedException ex) {
						s_logger.error("Failed", ex);
					}
				} catch (Exception e) {
					s_logger.error("readAddress failed ", e);
				}
			}
		}).start();
	}
	
	private ByteBuffer readRegister(SPIDevice spi, byte iRegister) throws UnavailableDeviceException, ClosedDeviceException, IOException {
		byte cmd = (byte)(CMD_R_REGISTER | (iRegister & MASK_REGISTER_CMD));

		ByteBuffer txBuffer = ByteBuffer.wrap(new byte[]{cmd,CMD_NOP,CMD_NOP,CMD_NOP,CMD_NOP,CMD_NOP});
		ByteBuffer rcBuffer = ByteBuffer.allocate(6);
		spi.writeAndRead(txBuffer, rcBuffer);
		rcBuffer.rewind();
		s_logger.info("read register status is {}", rcBuffer.get());
		return rcBuffer.slice();
	}
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
}
