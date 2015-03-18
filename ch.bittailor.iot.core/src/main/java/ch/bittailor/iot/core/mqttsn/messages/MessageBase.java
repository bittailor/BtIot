package ch.bittailor.iot.core.mqttsn.messages;

import java.nio.ByteBuffer;

import ch.bittailor.iot.core.utils.Utilities;

public abstract class MessageBase implements Message {

	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(calculateLength());
		writeToByteBuffer(buffer);
		buffer.flip();
		return buffer;
	}
	
	protected abstract int calculateLength();
	
	protected ByteBuffer putLength(ByteBuffer buffer) {
		int length = calculateLength();
		if (length < 256) {
			buffer.put((byte)length);
			return buffer;
		}
		
		buffer.put((byte)0x01);
		Utilities.putUnsignedShort(buffer, length);
		return buffer;
	}
	
}
