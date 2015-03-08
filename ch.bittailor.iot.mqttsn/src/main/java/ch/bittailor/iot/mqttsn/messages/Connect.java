package ch.bittailor.iot.mqttsn.messages;

import java.nio.ByteBuffer;

import ch.bittailor.iot.mqttsn.utils.Utilities;

public class Connect {
  private Flags mFlags;
  private int mDuration;
  private String mClientId;

  Connect(ByteBuffer buffer) {
  	mFlags = new Flags(buffer.get());
  	buffer.get(); // ProtocolId
  	mDuration = buffer.getShort();
  	mClientId = Utilities.readString(buffer);
  }
  
  Connect(String clientId) {
  	mFlags = new Flags();
  	mFlags.setWill(false); //TODO (BT) implement support for will topic!
  	mFlags.setCleanSession(true);
    mDuration = 0xFFFF; //TODO (BT) implement support duration of Keep Alive timer
    mClientId = clientId;
 }
  
  void writeToBuffer(ByteBuffer buffer) {
  	int length = 6 + mClientId.length();
  	buffer.put((byte)length);
  	buffer.put(MsgType.CONNECT.octet);
  	buffer.put(mFlags.asByte());
  	buffer.put(ProtocolId.PROTOCOL_ID_1_2.octet);
  	buffer.putShort((short)mDuration);
  	Utilities.writeString(mClientId, buffer);
  }
  
  public boolean isWill() {
		return mFlags.isWill();
	}

	public void setWill(boolean will) {
		mFlags.setWill(will);
	}
	
	public boolean isCleanSession() {
		return mFlags.isCleanSession();
	}

	public void setCleanSession(boolean cleanSession) {
		mFlags.setCleanSession(cleanSession);
	}

	public int getDuration() {
		return mDuration;
	}

	public void setDuration(int duration) {
		mDuration = duration;
	}

	public String getClientId() {
		return mClientId;
	}

	public void setClientId(String clientId) {
		mClientId = clientId;
	}
  
}
