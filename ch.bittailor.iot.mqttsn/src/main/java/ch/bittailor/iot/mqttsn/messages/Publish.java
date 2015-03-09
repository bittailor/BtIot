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

	public Flags getFlags() {
		return mFlags;
	}

	public void setFlags(Flags flags) {
		mFlags = flags;
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
