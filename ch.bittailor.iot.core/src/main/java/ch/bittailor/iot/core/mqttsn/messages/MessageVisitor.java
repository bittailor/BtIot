package ch.bittailor.iot.core.mqttsn.messages;

public interface MessageVisitor {
	void visit(Connect connect);
	void visit(Connack connack);
	void visit(Register register);
	void visit(Regack regack);
	void visit(Publish publish);
	void visit(Disconnect disconnect);
	void visit(Subscribe subscribe);
	void visit(Suback suback);
	void visit(PingReq pingreq);
	void visit(PingResp pingResp);
}
