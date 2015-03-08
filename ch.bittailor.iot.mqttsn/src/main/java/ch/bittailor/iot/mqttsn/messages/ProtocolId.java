package ch.bittailor.iot.mqttsn.messages;

public enum ProtocolId {
	PROTOCOL_ID_1_2(0x01);
	
	public final byte octet;

	ProtocolId(int octet) {
		this.octet = (byte) octet;
	}
}
