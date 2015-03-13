package ch.bittailor.iot.mqttsn.messages;

import java.nio.ByteBuffer;

public interface MessageFactory {

	Message createMessage(ByteBuffer packet) throws MessageFactoryException;

}