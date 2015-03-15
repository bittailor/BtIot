package ch.bittailor.iot.core.mqttsn.messages;

import java.nio.ByteBuffer;

import ch.bittailor.iot.core.utils.Utilities;

public class Subscribe implements Message {

	private Flags mFlags;
	private int mMsgId;
	private String mTopicName;

	public Subscribe(ByteBuffer buffer) {
		mFlags = new Flags(buffer.get());
		mMsgId = Utilities.getUnsignedShort(buffer);
		mTopicName = Utilities.getString(buffer);
	}
   
	@Override
	public void writeToBuffer(ByteBuffer buffer) {
    int length = 5 + mTopicName.length();
    buffer.put((byte)length);
    buffer.putInt(MsgType.SUBSCRIBE.octet);
    buffer.put(mFlags.asByte());
    Utilities.putUnsignedShort(buffer, mMsgId);
    Utilities.putString(buffer, mTopicName);
	}

	@Override
	public void accept(MessageVisitor vistor) {
		vistor.visit(this);		
	}

	public boolean isDup() {
		return mFlags.isDup();
	}


	public void setDup(boolean dup) {
		mFlags.setDup(dup);
	}


	public int getQos() {
		return mFlags.getQos();
	}


	public void setQos(int qos) {
		mFlags.setQos(qos);
	}

	public TopicIdType getTopicIdType() {
		return mFlags.getTopicIdType();
	}


	public void setTopicIdType(TopicIdType topicIdType) {
		mFlags.setTopicIdType(topicIdType);;
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
