package ch.bittailor.iot.mqttsn.messages;

import java.nio.ByteBuffer;

import ch.bittailor.iot.mqttsn.utils.Utilities;

public class Regack implements Message {
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
	public void writeToBuffer(ByteBuffer buffer) {
		int length = 7 ;
		buffer.put((byte)length);
		buffer.put(MsgType.REGACK.octet);
		Utilities.putUnsignedShort(buffer, mTopicId);
		Utilities.putUnsignedShort(buffer, mMsgId);
		buffer.put(mReturnCode.octet);
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
