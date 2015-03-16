package ch.bittailor.iot.core.mqttsn.gateway;

public interface MqttClient extends AutoCloseable {
	void publish(String appTopic, byte[] payload, int qos, boolean retain);
}
