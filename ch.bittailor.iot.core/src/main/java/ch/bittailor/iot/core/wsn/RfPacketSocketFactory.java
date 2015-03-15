package ch.bittailor.iot.core.wsn;

public interface RfPacketSocketFactory {
	RfPacketSocket create(int networkAddress);
}
