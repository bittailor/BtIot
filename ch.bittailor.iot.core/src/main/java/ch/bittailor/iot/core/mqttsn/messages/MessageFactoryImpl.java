package ch.bittailor.iot.core.mqttsn.messages;

import java.nio.ByteBuffer;

import ch.bittailor.iot.core.utils.Utilities;

public class MessageFactoryImpl implements MessageFactory {
	
	public MessageFactoryImpl() {
	}

	@Override
	public Message createMessage(ByteBuffer packet) throws MessageFactoryException {
		packet.mark();
		parseLength(packet);
		return parseMessage(packet);
	}

	private void parseLength(ByteBuffer buffer) throws MessageFactoryException {
		int length = buffer.get();
		if(length == 0x01) {
			length = Utilities.getUnsignedShort(buffer);
		}
		if(length != buffer.limit()) {
			throw new MessageFactoryException("mismatching length " + length + "  and buffer limit (" + buffer.limit() + ")"); 
		}
	}

	private Message parseMessage(ByteBuffer buffer) throws MessageFactoryException {
		MsgType msgType = MsgType.parse(buffer.get());
		switch (msgType) {
		case ADVERTISE: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		case CONNACK: return new Connack(buffer);
		case CONNECT: return new Connect(buffer);
		case DISCONNECT: return new Disconnect(buffer);
		case GWINFO: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		case PINGREQ: return new PingReq(buffer);
		case PINGRESP: return new PingResp(buffer);
		case PUBACK: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		case PUBCOMP: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		case PUBLISH: return new Publish(buffer);
		case PUBREC: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		case PUBREL: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		case REGACK: return new Regack(buffer);
		case REGISTER: return new Register(buffer);
		case SEARCHGW: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		case SUBACK: return new Suback(buffer);
		case SUBSCRIBE: return new Subscribe(buffer);
		case UNSUBACK: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		case UNSUBSCRIBE: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		case WILLMSG: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		case WILLMSGREQ: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		case WILLMSGRESP: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		case WILLMSGUPD: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		case WILLTOPIC: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		case WILLTOPICREQ: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		case WILLTOPICRESP: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		case WILLTOPICUPD: throw new MessageFactoryException("MsgType " + msgType + " not implemented yet");
		default: throw new MessageFactoryException("MsgType " + msgType + " unknown");
		}
	}

}
