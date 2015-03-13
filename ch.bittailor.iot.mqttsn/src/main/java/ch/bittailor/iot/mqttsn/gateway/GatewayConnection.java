package ch.bittailor.iot.mqttsn.gateway;

import java.io.Closeable;

import ch.bittailor.iot.mqttsn.messages.Message;

public interface GatewayConnection extends Closeable {

	void handle(Message message);

}
