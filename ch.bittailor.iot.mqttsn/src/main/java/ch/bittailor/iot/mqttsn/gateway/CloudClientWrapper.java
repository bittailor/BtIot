package ch.bittailor.iot.mqttsn.gateway;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudClient;

import ch.bittailor.iot.core.mqttsn.gateway.MqttClient;

public class CloudClientWrapper implements MqttClient {
	private static final int PRIO = 5;
	
	private final CloudClient mCloudClient;

	public CloudClientWrapper(CloudClient cloudClient) {
		mCloudClient = cloudClient;
	}

	@Override
	public void close() throws Exception {
		mCloudClient.release();
	}

	@Override
	public void publish(String appTopic, byte[] payload, int qos, boolean retain) {
		try {
			mCloudClient.publish(appTopic, payload, qos, retain, PRIO);
		} catch (KuraException e) {
			throw new RuntimeException(e);
		}
	}

}
