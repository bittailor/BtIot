package ch.bittailor.iot.mqttsn.messages;

import java.nio.ByteBuffer;

public interface Message {

	void writeToBuffer(ByteBuffer buffer);
  void accept(MessageVisitor vistor); 
}