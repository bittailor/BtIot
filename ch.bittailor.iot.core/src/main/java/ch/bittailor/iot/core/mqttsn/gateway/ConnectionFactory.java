package ch.bittailor.iot.core.mqttsn.gateway;

import ch.bittailor.iot.core.wsn.PacketSocket;
import ch.bittailor.iot.core.wsn.RfSocketAddress;

public interface ConnectionFactory {

	GatewayConnection create(RfSocketAddress address, PacketSocket packetSocket);

}
