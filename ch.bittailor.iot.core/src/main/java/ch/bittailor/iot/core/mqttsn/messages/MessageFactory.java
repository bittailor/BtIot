package ch.bittailor.iot.core.mqttsn.messages;

import java.nio.ByteBuffer;

public interface MessageFactory {

	Message createMessage(ByteBuffer packet) throws MessageFactoryException;

}