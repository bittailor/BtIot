package ch.bittailor.iot.core.mqttsn.gateway;

import java.io.Closeable;

import ch.bittailor.iot.core.mqttsn.messages.Message;

public interface GatewayConnection extends Closeable {

	void handle(Message message);

}
