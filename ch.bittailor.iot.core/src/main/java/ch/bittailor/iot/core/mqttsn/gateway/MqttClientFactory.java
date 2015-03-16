package ch.bittailor.iot.core.mqttsn.gateway;

public interface MqttClientFactory {
	MqttClient create(String clientId);
}
