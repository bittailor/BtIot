package ch.bittailor.iot.san;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bittailor.iot.core.wsn.PacketSocket;
import ch.bittailor.iot.core.wsn.RfPacketSocket;
import ch.bittailor.iot.core.wsn.RfPacketSocketFactoryImpl;
import ch.bittailor.iot.core.wsn.RfSocketAddress;

public class RfSocket implements PacketSocket, ConfigurableComponent {
	private static final Logger LOG = LoggerFactory.getLogger(RfSocket.class);
	private static final String APP_ID = "ch.bittailor.iot.san.RfSocket";
		
	private RfPacketSocket mRfPacketSocket;

	@Override
	public int payloadCapacity() {
		return mRfPacketSocket.payloadCapacity();
	}

	@Override
	public int send(RfSocketAddress destination, ByteBuffer payload) {
		return mRfPacketSocket.send(destination, payload);
	}

	@Override
	public void setListener(final Listener listener) {
		mRfPacketSocket.setListener(new RfPacketSocket.Listener() {
			@Override
			public void received(RfSocketAddress source, ByteBuffer payload) {
				listener.received(source, payload);
			}
		});
	}

	@Override
	public void resetListener() {
		mRfPacketSocket.resetListener();
	}

	protected void activate(ComponentContext componentContext) {
		LOG.info("Bundle " + APP_ID + " has started");
		try {
			mRfPacketSocket = new RfPacketSocketFactoryImpl().create(0);
		} catch (Exception e) {
			LOG.error("creating the RF packet socket failed", e);
		}
	}
	
	protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
		LOG.info("Bundle " + APP_ID + " has started with config!");
		updated(properties);
		activate(componentContext);	
	}
	
	public void updated(Map<String, Object> properties) {
		LOG.info("Bundle " + APP_ID + " updated properties!");
    if(properties != null && !properties.isEmpty()) {
    	for (Entry<String, Object> property : properties.entrySet()) {
    		LOG.info("New property - " + property.getKey() + " = " + property.getValue() + " of type " + property.getValue().getClass().toString());
			}
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
