package ch.bittailor.iot.mqttsn.gateway;

import ch.bittailor.iot.core.wsn.RfSocketAddress;

public interface ConnectionFactory {

	GatewayConnection create(RfSocketAddress address);

}
