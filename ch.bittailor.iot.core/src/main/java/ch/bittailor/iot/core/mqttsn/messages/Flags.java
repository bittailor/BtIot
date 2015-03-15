package ch.bittailor.iot.core.mqttsn.messages;


public class Flags {
	private boolean dup;
	private int qos;
	private boolean retain;
	private boolean will;
	private boolean cleanSession;
	private TopicIdType topicIdType;
	
	public Flags() {
		this((byte)0x00);		
	}
	
	public Flags(byte abyte) {
		dup = ((abyte & 0x80) != 0);
		qos = ((abyte & 0x60) >> 5);
		retain = ((abyte & 0x10) != 0);
		will = ((abyte & 0x08) != 0);
		cleanSession = ((abyte & 0x04) !=0);
		switch((abyte & 0x03)){
			case 0x00: topicIdType = TopicIdType.NORMAL_TOPIC_ID; break;
			case 0x01: topicIdType = TopicIdType.PREDEFINED_TOPIC_ID; break;
			case 0x02: topicIdType = TopicIdType.SHORT_TOPIC_NAME; break;
			default: throw new RuntimeException("TopicIdType 0x03 is is reserved");
		}	
	}

	public boolean isDup() {
		return dup;
	}


	public void setDup(boolean dup) {
		this.dup = dup;
	}


	public int getQos() {
		return qos;
	}


	public void setQos(int qos) {
		this.qos = qos;
	}


	public boolean isRetain() {
		return retain;
	}


	public void setRetain(boolean retain) {
		this.retain = retain;
	}


	public boolean isWill() {
		return will;
	}


	public void setWill(boolean will) {
		this.will = will;
	}


	public boolean isCleanSession() {
		return cleanSession;
	}


	public void setCleanSession(boolean cleanSession) {
		this.cleanSession = cleanSession;
	}


	public TopicIdType getTopicIdType() {
		return topicIdType;
	}


	public void setTopicIdType(TopicIdType topicIdType) {
		this.topicIdType = topicIdType;
	}


	public byte asByte() {
		byte asByte = 0x00;
		switch (topicIdType) {
		case NORMAL_TOPIC_ID: asByte = 0x00; break;
		case PREDEFINED_TOPIC_ID: asByte = 0x01; break;
		case SHORT_TOPIC_NAME: asByte = 0x02; break;
		}
		
		if (cleanSession) { asByte |= 0x04; }
		if (will) { asByte |= 0x08; }
		if (retain) { asByte |= 0x10; }
		asByte |= ((qos & 0x03) >> 5);
		if (dup) { asByte |= 0x80; }
		
		return asByte;
	}

}
