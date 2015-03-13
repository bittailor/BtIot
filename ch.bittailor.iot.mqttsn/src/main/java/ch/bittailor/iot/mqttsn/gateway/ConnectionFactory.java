package ch.bittailor.iot.mqttsn.gateway;

import ch.bittailor.iot.san.nrf24.RfSocketAddress;

public interface ConnectionFactory {

	GatewayConnection create(RfSocketAddress address);

}
