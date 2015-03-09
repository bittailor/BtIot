package ch.bittailor.iot.mqttsn.messages;

public enum MsgType {

	ADVERTISE(0x00), SEARCHGW(0x01), GWINFO(0x02), CONNECT(0x04), CONNACK(0x05), WILLTOPICREQ(
			0x06), WILLTOPIC(0x07), WILLMSGREQ(0x08), WILLMSG(0x09), REGISTER(0x0a), REGACK(
			0x0b), PUBLISH(0x0c), PUBACK(0x0d), PUBCOMP(0x0e), PUBREC(0x0f), PUBREL(
			0x10), SUBSCRIBE(0x12), SUBACK(0x13), UNSUBSCRIBE(0x14), UNSUBACK(0x15), PINGREQ(
			0x16), PINGRESP(0x17), DISCONNECT(0x18), WILLTOPICUPD(0x1a), WILLTOPICRESP(
			0x1b), WILLMSGUPD(0x1c), WILLMSGRESP(0x1d);

	public final byte octet;

	MsgType(int octet) {
		this.octet = (byte) octet;
	}
	
	public static MsgType parse(byte octet) {
		for (MsgType msgType : MsgType.values()) {
			if (msgType.octet == octet) {
				return msgType;
			}
		}
		throw new IllegalArgumentException(String.format("No MsgType definition for 0x%x",octet));
	}
	
	
	

}
