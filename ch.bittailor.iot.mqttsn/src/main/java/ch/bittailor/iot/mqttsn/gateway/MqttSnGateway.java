package ch.bittailor.iot.mqttsn.gateway;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bittailor.iot.san.RfSocket;


public class MqttSnGateway {
	private static final Logger LOG = LoggerFactory.getLogger(MqttSnGateway.class);
	private static final String APP_ID = "ch.bittailor.iot.mqttsn.gateway.MqttSnGateway";
	
	private RfSocket mRfSocket;
	
	protected void activate(ComponentContext componentContext) {
		LOG.info("Bundle " + APP_ID + " has started");
	}

	protected void deactivate(ComponentContext componentContext) {
		LOG.info("Bundle " + APP_ID + " has stopped");
	}
	
	public synchronized void setRfSocket(RfSocket rfSocket) {
		LOG.info("Bundle " + APP_ID + " RfSocket set");
		mRfSocket = rfSocket;
	}

	public synchronized void unsetRfSocket(RfSocket rfSocket) {
		LOG.info("Bundle " + APP_ID + " RfSocket unset");
		if (mRfSocket == rfSocket) {
			mRfSocket = null;
    }
	}


}
