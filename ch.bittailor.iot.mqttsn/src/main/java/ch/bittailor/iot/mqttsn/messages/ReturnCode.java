package ch.bittailor.iot.mqttsn.messages;

public enum ReturnCode {
	
  ACCEPTED(0x00),
  REJECTED_CONGESTION(0x01),
  REJECTED_INVALID_TOPIC_ID(0x02),
  REJECTED_NOT_SUPPORTED(0x03);
  
  public final byte octet;

  private ReturnCode(int octet) {
		this.octet = (byte) octet;
	}

	public static ReturnCode parse(byte octet) {
		for (ReturnCode returnCode : ReturnCode.values()) {
			if (returnCode.octet == octet) {
				return returnCode;
			}
		}
		throw new IllegalArgumentException(String.format("No ReturnCode definition for 0x%x",octet));
	}
  
}
