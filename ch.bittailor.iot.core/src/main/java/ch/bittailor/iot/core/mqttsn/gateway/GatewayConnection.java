package ch.bittailor.iot.core.mqttsn.gateway;

import ch.bittailor.iot.core.mqttsn.messages.Message;

public interface GatewayConnection extends AutoCloseable {

	void handle(Message message);

}
