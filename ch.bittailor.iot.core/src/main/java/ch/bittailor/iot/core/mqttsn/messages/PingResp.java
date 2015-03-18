package ch.bittailor.iot.core.mqttsn.messages;

import java.nio.ByteBuffer;

public class PingResp extends MessageBase {

	public PingResp() {		
	}
	
	public PingResp(ByteBuffer buffer) {
	}
	
	@Override
	protected int calculateLength() {
		return 2;
	}

	@Override
	public ByteBuffer writeToByteBuffer(ByteBuffer buffer) {
		putLength(buffer);
		buffer.put(MsgType.PINGRESP.octet);
		return buffer;
	}

	@Override
	public void accept(MessageVisitor vistor) {
		vistor.visit(this);	
	}

}
