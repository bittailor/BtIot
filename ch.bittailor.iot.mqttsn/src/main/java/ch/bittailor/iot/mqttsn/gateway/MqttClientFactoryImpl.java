package ch.bittailor.iot.mqttsn.gateway;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudService;

import ch.bittailor.iot.core.mqttsn.gateway.MqttClient;
import ch.bittailor.iot.core.mqttsn.gateway.MqttClientFactory;

public class MqttClientFactoryImpl implements MqttClientFactory {

	private final CloudService mCloudService;
	
	public MqttClientFactoryImpl(CloudService cloudService) {
		mCloudService = cloudService;
	}

	@Override
	public MqttClient create(String clientId) {
		try {
			return new CloudClientWrapper(mCloudService.newCloudClient(clientId));
		} catch (KuraException e) {
			throw new RuntimeException(e);
		}
	}

}
