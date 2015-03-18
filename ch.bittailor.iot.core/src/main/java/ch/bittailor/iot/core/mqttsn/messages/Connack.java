package ch.bittailor.iot.core.mqttsn.messages;

import java.io.ObjectOutputStream.PutField;
import java.nio.ByteBuffer;

public class Connack extends MessageBase {
	private ReturnCode  mReturnCode;
	
	public Connack(ByteBuffer buffer) {
		mReturnCode = ReturnCode.parse(buffer.get());
	}
	
	public Connack(ReturnCode  returnCode) {
		mReturnCode = returnCode;
	}
	
	
	
	@Override
	protected int calculateLength() {
		return 3;
	}

	@Override
	public ByteBuffer writeToByteBuffer(ByteBuffer buffer) {
		putLength(buffer);
		buffer.put(MsgType.CONNACK.octet);
		buffer.put(mReturnCode.octet);
		return buffer;
	}

	@Override
	public void accept(MessageVisitor vistor) {
		vistor.visit(this);
	}

	public ReturnCode getReturnCode() {
		return mReturnCode;
	}

	public void setReturnCode(ReturnCode returnCode) {
		mReturnCode = returnCode;
	}

}
