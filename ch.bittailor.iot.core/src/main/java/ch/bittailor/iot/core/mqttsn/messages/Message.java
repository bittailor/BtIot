package ch.bittailor.iot.core.mqttsn.messages;

import java.nio.ByteBuffer;

public interface Message {

	void writeToBuffer(ByteBuffer buffer);
  void accept(MessageVisitor vistor); 
}