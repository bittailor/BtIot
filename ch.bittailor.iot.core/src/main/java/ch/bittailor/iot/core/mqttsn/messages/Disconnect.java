package ch.bittailor.iot.core.mqttsn.messages;

import java.nio.ByteBuffer;

import ch.bittailor.iot.core.utils.Utilities;

public class Disconnect extends MessageBase {
  boolean mWithDuration;
  int mDuration;
	
  public Disconnect() {
  	mWithDuration = false;
  	mDuration = 0;
  }
	
  public Disconnect(ByteBuffer buffer) {
		if(buffer.remaining() == 0) {
			mWithDuration = false;
			return;
		}
		mWithDuration = true;
		mDuration = Utilities.getUnsignedShort(buffer);
	}
	
	
	
	@Override
	protected int calculateLength() {
		if(mWithDuration) {
    	return 4;
    }
    return 2;
	}

	@Override
	public ByteBuffer writeToByteBuffer(ByteBuffer buffer) {
    buffer.put((byte)calculateLength());
    buffer.put(MsgType.DISCONNECT.octet);
    if(mWithDuration) {
    	Utilities.putUnsignedShort(buffer, mDuration);
    }
    return buffer;
	}

	@Override
	public void accept(MessageVisitor vistor) {
		vistor.visit(this);
	}

	public boolean isWithDuration() {
		return mWithDuration;
	}

	public void setWithDuration(boolean withDuration) {
		mWithDuration = withDuration;
	}

	public int getDuration() {
		return mDuration;
	}

	public void setDuration(int duration) {
		mDuration = duration;
	}
	
	

}
