package ch.bittailor.iot.mqttsn.messages;

import java.nio.ByteBuffer;

import ch.bittailor.iot.mqttsn.utils.Utilities;

public class Publish implements Message{
	private Flags mFlags;
	private int mTopicId;
  private int mMsgId;
  private byte[] mData;

  public Publish(ByteBuffer buffer) {
  	mFlags = new Flags(buffer.get());
  	mTopicId = Utilities.getUnsignedShort(buffer);
  	mMsgId = Utilities.getUnsignedShort(buffer);
  	mData = Utilities.getBytes(buffer); 
  }
  
	public Publish(Flags flags, int topicId, int msgId, byte[] data) {
		mFlags = flags;
		mTopicId = topicId;
		mMsgId = msgId;
		mData = data;
	}
	
	@Override
	public void writeToBuffer(ByteBuffer buffer) {
		int length = 7 + mData.length;
		buffer.put((byte)length);
		buffer.put(MsgType.PUBLISH.octet);
		buffer.put(mFlags.asByte());
		Utilities.putUnsignedShort(buffer, mTopicId);
		Utilities.putUnsignedShort(buffer, mMsgId);
		buffer.put(mData);
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

	public boolean isRetain() {
		return mFlags.isRetain();
	}

	public void setRetain(boolean retain) {
		mFlags.setRetain(retain);
	}

	public TopicIdType getTopicIdType() {
		return mFlags.getTopicIdType();
	}

	public void setTopicIdType(TopicIdType topicIdType) {
		mFlags.setTopicIdType(topicIdType);;
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

	public byte[] getData() {
		return mData;
	}

	public void setData(byte[] data) {
		mData = data;
	}

	
	
}
