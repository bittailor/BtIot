package ch.bittailor.iot.core.mqttsn.messages;

import java.nio.ByteBuffer;

import ch.bittailor.iot.core.utils.Utilities;

public class Connect extends MessageBase {
	private Flags mFlags;
	private int mDuration;
	private String mClientId;

	public Connect(ByteBuffer buffer) {
		mFlags = new Flags(buffer.get());
		buffer.get(); // ProtocolId
		mDuration = Utilities.getUnsignedShort(buffer);
		mClientId = Utilities.getString(buffer);
	}

	public Connect(String clientId) {
		mFlags = new Flags();
		mFlags.setWill(false); //TODO (BT) implement support for will topic!
		mFlags.setCleanSession(true);
		mDuration = 0xFFFF; //TODO (BT) implement support duration of Keep Alive timer
		mClientId = clientId;
	}
	
	@Override
	protected int calculateLength() {
		return 6 + Utilities.getBufferLengthForString(mClientId);
	}

	@Override
	public ByteBuffer writeToByteBuffer(ByteBuffer buffer) {
		putLength(buffer);
		buffer.put(MsgType.CONNECT.octet);
		buffer.put(mFlags.asByte());
		buffer.put(ProtocolId.PROTOCOL_ID_1_2.octet);
		Utilities.putUnsignedShort(buffer, mDuration);
		Utilities.putString(buffer, mClientId);
		return buffer;
	}

	@Override
	public void accept(MessageVisitor vistor) {
		vistor.visit(this);
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
