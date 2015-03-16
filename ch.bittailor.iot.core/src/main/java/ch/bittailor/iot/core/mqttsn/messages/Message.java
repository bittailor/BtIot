package ch.bittailor.iot.core.mqttsn.messages;

import java.nio.ByteBuffer;

public interface Message {

	ByteBuffer writeToByteBuffer(ByteBuffer buffer);
	ByteBuffer toByteBuffer();
	
  void accept(MessageVisitor vistor); 
}