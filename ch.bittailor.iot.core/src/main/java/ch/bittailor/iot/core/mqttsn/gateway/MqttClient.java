package ch.bittailor.iot.core.mqttsn.gateway;

public interface MqttClient extends AutoCloseable {
	
	void setCallback(Callback callback);
	
	void publish(String topic, byte[] payload, int qos, boolean retain);
	void subscribe(String topic, int qos);
	
	public interface Callback {
		void messageArrived(String topic, byte[] payload, int qos, boolean retain);
	}
}
