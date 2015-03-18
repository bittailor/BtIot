package ch.bittailor.iot.core.mqttsn.messages;

import java.nio.ByteBuffer;

import ch.bittailor.iot.core.utils.Utilities;

public class Register extends MessageBase {
  private int mTopicId;
  private int mMsgId;
  private String mTopicName;
  
  public Register(ByteBuffer buffer){
  	mTopicId = Utilities.getUnsignedShort(buffer);
  	mMsgId = Utilities.getUnsignedShort(buffer);
  	mTopicName = Utilities.getString(buffer);
  }
  
  public Register(int topicId, int msgId, String topicName) {
		mTopicId = topicId;
		mMsgId = msgId;
		mTopicName = topicName;
	}

	@Override
	protected int calculateLength() {
		return 6 + Utilities.getBufferLengthForString(mTopicName);
	}

	@Override
	public ByteBuffer writeToByteBuffer(ByteBuffer buffer) {
		putLength(buffer);
  	buffer.put(MsgType.REGISTER.octet);
  	Utilities.putUnsignedShort(buffer, mTopicId);
  	Utilities.putUnsignedShort(buffer, mMsgId);
  	Utilities.putString(buffer, mTopicName);	
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

	public String getTopicName() {
		return mTopicName;
	}

	public void setTopicName(String topicName) {
		mTopicName = topicName;
	}

}

	
