package ch.bittailor.iot.san;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bittailor.iot.san.nrf24.RfPacketSocket;
import ch.bittailor.iot.san.nrf24.RfPacketSocketFactoryImpl;
import ch.bittailor.iot.san.nrf24.RfSocketAddress;

public class RfSocket implements PacketSocket {
	private static final Logger LOG = LoggerFactory.getLogger(RfSocket.class);
	private static final String APP_ID = "ch.bittailor.iot.san.RfSocket";
		
	private RfPacketSocket mRfPacketSocket;

	@Override
	public int send(ByteBuffer payload, RfSocketAddress destination) {
		return mRfPacketSocket.send(payload, destination);
	}

	@Override
	public RfSocketAddress receive(ByteBuffer payload) {
		return receive(payload);
	}
	
	protected void activate(ComponentContext componentContext) {
		LOG.info("Bundle " + APP_ID + " has started");
		try {
			mRfPacketSocket = new RfPacketSocketFactoryImpl().create();
		} catch (Exception e) {
			LOG.error("creating the RF packet socket failed", e);
		}
	}

	protected void deactivate(ComponentContext componentContext) {
		LOG.info("Bundle " + APP_ID + " has stopped");
		try {
			mRfPacketSocket.close();
		} catch (IOException e) {
			LOG.error("closing the RF packet socket failed", e);
		}
		mRfPacketSocket = null;
	}
	
}
