package ch.bittailor.iot.core.mqttsn.messages;

import java.nio.ByteBuffer;

import ch.bittailor.iot.core.utils.Utilities;

public class Suback extends MessageBase {
	private Flags mFlags;
	private int mTopicId;
	private int mMsgId;
	private ReturnCode mReturnCode;

	public Suback(int qos, int topicId, int msgId, ReturnCode returnCode) {
		mFlags = new Flags();
		mFlags.setQos(qos);
		mTopicId = topicId;
		mMsgId = msgId;
		mReturnCode = returnCode;
	}
	
	public Suback(ByteBuffer buffer) {
		mFlags = new Flags(buffer.get());
		mTopicId = Utilities.getUnsignedShort(buffer);
		mMsgId = Utilities.getUnsignedShort(buffer);
		mReturnCode = ReturnCode.parse(buffer.get());
	}

	@Override
	protected int calculateLength() {
		return 8;
	}

	@Override
	public ByteBuffer writeToByteBuffer(ByteBuffer buffer) {
		putLength(buffer);
		buffer.put(MsgType.SUBACK.octet);
		buffer.put(mFlags.asByte());
		Utilities.putUnsignedShort(buffer, mTopicId);
		Utilities.putUnsignedShort(buffer, mMsgId);
		buffer.put(mReturnCode.octet);
		return buffer;
	}

	@Override
	public void accept(MessageVisitor vistor) {
		vistor.visit(this);

	}
	
	public int getQos() {
		return mFlags.getQos();
	}


	public void setQos(int qos) {
		mFlags.setQos(qos);
	}

}
