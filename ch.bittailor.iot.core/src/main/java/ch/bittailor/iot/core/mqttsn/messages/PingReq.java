package ch.bittailor.iot.core.mqttsn.messages;

import java.nio.ByteBuffer;

import ch.bittailor.iot.core.utils.Utilities;

public class PingReq extends MessageBase {
	private boolean mWithClientId;
	String mClientId;
	
	public PingReq(){
		mWithClientId = false;
	}
 
	public PingReq(ByteBuffer buffer) {
		if(buffer.remaining() == 0) {
			mWithClientId = false;
      return;
		}
		mWithClientId = true;
		mClientId = Utilities.getString(buffer);
	}
	
	@Override
	protected int calculateLength() {
		if(mWithClientId) {
			return 2 + Utilities.getBufferLengthForString(mClientId);
		}
		return 0;
	} 
	
	@Override
	public ByteBuffer writeToByteBuffer(ByteBuffer buffer) {
		putLength(buffer);
		buffer.putInt(MsgType.PINGREQ.octet);
		if(mWithClientId) {
			Utilities.putString(buffer, mClientId);
		}
		return buffer;
	}
	
	@Override
	public void accept(MessageVisitor vistor) {
		vistor.visit(this);
		
	}

	public boolean isWithClientId() {
		return mWithClientId;
	}

	public void setWithClientId(boolean withClientId) {
		mWithClientId = withClientId;
	}

	public String getClientId() {
		return mClientId;
	}

	public void setClientId(String clientId) {
		mClientId = clientId;
	}
	
}
