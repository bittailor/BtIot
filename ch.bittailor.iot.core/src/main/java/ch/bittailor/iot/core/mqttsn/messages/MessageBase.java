package ch.bittailor.iot.core.mqttsn.messages;

import java.nio.ByteBuffer;

public abstract class MessageBase implements Message {

	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(calculateLength());
		writeToByteBuffer(buffer);
		buffer.flip();
		return buffer;
	}
	
	protected abstract int calculateLength();
	
}
