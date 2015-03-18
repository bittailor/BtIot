package ch.bittailor.iot.core.mqttsn.messages;

import java.nio.ByteBuffer;

import ch.bittailor.iot.core.utils.Utilities;

public class Regack extends MessageBase {
  int mTopicId;
  int mMsgId;
  ReturnCode mReturnCode;
	
	public Regack(ByteBuffer buffer) {
		mTopicId = Utilities.getUnsignedShort(buffer);
  	mMsgId = Utilities.getUnsignedShort(buffer);
  	mReturnCode = ReturnCode.parse(buffer.get());
	}
	
	public Regack(int topicId, int msgId, ReturnCode returnCode) {
		mTopicId = topicId;
		mMsgId = msgId;
		mReturnCode = returnCode;
	}
	
	@Override
	protected int calculateLength() {
		return 7;
	}

	@Override
	public ByteBuffer writeToByteBuffer(ByteBuffer buffer) {
		putLength(buffer);
		buffer.put(MsgType.REGACK.octet);
		Utilities.putUnsignedShort(buffer, mTopicId);
		Utilities.putUnsignedShort(buffer, mMsgId);
		buffer.put(mReturnCode.octet);
		return buffer;
	}

	@Override
	public void accept(MessageVisitor vistor) {
		vistor.visit(this);
	}

	public int getTopicId() {
		return mTopicId;
	}

	public void setTopicId(int topicId) {
		mTopicId = topicId;
	}

	public int getMsgId() {
		return mMsgId;
	}

	public void setMsgId(int msgId) {
		mMsgId = msgId;
	}

	public ReturnCode getReturnCode() {
		return mReturnCode;
	}

	public void setReturnCode(ReturnCode returnCode) {
		mReturnCode = returnCode;
	}

}
