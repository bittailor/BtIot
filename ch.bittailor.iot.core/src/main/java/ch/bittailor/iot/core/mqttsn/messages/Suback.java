package ch.bittailor.iot.core.mqttsn.messages;

import java.nio.ByteBuffer;

import ch.bittailor.iot.core.utils.Utilities;

public class Suback extends MessageBase {
	private Flags mFlags;
	private int mTopicId;
	private int mMsgId;
	private ReturnCode mReturnCode;

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
		buffer.put((byte)calculateLength());
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

}
