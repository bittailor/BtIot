package ch.bittailor.iot.mqttsn.messages;

import java.nio.ByteBuffer;

public class Connack implements Message {
	private ReturnCode  mReturnCode;
	
	public Connack(ByteBuffer buffer) {
		mReturnCode = ReturnCode.parse(buffer.get());
	}
	
	public Connack(ReturnCode  returnCode) {
		mReturnCode = returnCode;
	}
	
	@Override
	public void writeToBuffer(ByteBuffer buffer) {
		buffer.put((byte)3);
		buffer.put(MsgType.CONNACK.octet);
		buffer.put(mReturnCode.octet);
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
