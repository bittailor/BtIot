package ch.bittailor.iot.core.mqttsn.messages;

import java.nio.ByteBuffer;

import ch.bittailor.iot.core.utils.Utilities;

public class Disconnect implements Message {
  boolean mWithDuration;
  int mDuration;
	
  Disconnect() {
  	mWithDuration = false;
  	mDuration = 0;
  }
	
	Disconnect(ByteBuffer buffer) {
		if(buffer.remaining() == 0) {
			mWithDuration = false;
			return;
		}
		mWithDuration = true;
		mDuration = Utilities.getUnsignedShort(buffer);
	}
	
	@Override
	public void writeToBuffer(ByteBuffer buffer) {
		int length = 2;

    if(mWithDuration) {
    	length = 4;
    }

    buffer.put((byte)length);
    buffer.put(MsgType.DISCONNECT.octet);
    if(mWithDuration) {
    	Utilities.putUnsignedShort(buffer, mDuration);
    }
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
