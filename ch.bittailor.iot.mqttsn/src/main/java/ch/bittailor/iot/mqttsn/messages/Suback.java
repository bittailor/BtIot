package ch.bittailor.iot.mqttsn.messages;

import java.nio.ByteBuffer;

import ch.bittailor.iot.mqttsn.utils.Utilities;

public class Suback implements Message {
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
	public void writeToBuffer(ByteBuffer buffer) {
		 int length = 8 ;
		 buffer.put((byte)length);
		 buffer.put(MsgType.SUBACK.octet);
		 buffer.put(mFlags.asByte());
		 Utilities.putUnsignedShort(buffer, mTopicId);
		 Utilities.putUnsignedShort(buffer, mMsgId);
		 buffer.put(mReturnCode.octet);
	}

	@Override
	public void accept(MessageVisitor vistor) {
		vistor.visit(this);
		
	}

}
