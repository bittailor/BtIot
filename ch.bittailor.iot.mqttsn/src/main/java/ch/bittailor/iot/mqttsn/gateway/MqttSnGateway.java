package ch.bittailor.iot.mqttsn.gateway;

import java.io.IOException;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bittailor.iot.core.mqttsn.gateway.Gateway;
import ch.bittailor.iot.core.mqttsn.gateway.GatewayFactoryImpl;
import ch.bittailor.iot.core.wsn.PacketSocket;


public class MqttSnGateway {
	private static final Logger LOG = LoggerFactory.getLogger(MqttSnGateway.class);
	private static final String APP_ID = "ch.bittailor.iot.mqttsn.gateway.MqttSnGateway";
	
	private PacketSocket mRfSocket;
	private Gateway mGateway;
	
	protected void activate(ComponentContext componentContext) {
		LOG.info("Bundle " + APP_ID + " has started");
		mGateway = new GatewayFactoryImpl().create(mRfSocket);
	}

	protected void deactivate(ComponentContext componentContext) {
		LOG.info("Bundle " + APP_ID + " has stopped");
		try {
			mGateway.close();
		} catch (IOException e) {
			LOG.error("closing the mqtt-sn gateway failed", e);
		}
		mGateway = null;
	}
	
	public synchronized void setPacketSocket(PacketSocket rfSocket) {
		LOG.info("Bundle " + APP_ID + " RfSocket set");
		mRfSocket = rfSocket;
	}

	public synchronized void unsetPacketSocket(PacketSocket rfSocket) {
		LOG.info("Bundle " + APP_ID + " RfSocket unset");
		if (mRfSocket == rfSocket) {
			mRfSocket = null;
    }
	}

}
