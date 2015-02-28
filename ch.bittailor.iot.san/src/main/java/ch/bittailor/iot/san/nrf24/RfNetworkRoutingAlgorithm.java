package ch.bittailor.iot.san.nrf24;

public class RfNetworkRoutingAlgorithm {
	
	RfPipe calculateRoutingPipe(int self, int destination) {
		return calculateRoutingPipe(new RfSocketAddress(self), new RfSocketAddress(destination));
	}
	
	RfPipe calculateRoutingPipe(RfSocketAddress self, RfSocketAddress destination) {
		if (self.getLevel() >= destination.getLevel()){
			return RfPipe.PIPE_0;
		}

		int idAtLevel[] = new int[5];
		idAtLevel[destination.getLevel()] = destination.getId();
		for(int i = destination.getLevel() ; i > self.getLevel() ; i-- ) {
			idAtLevel[i - 1] = (idAtLevel[i] - 1) / 5;
		}

		if (idAtLevel[self.getLevel()] != self.getId()) {
			return RfPipe.PIPE_0;
		}

		switch (idAtLevel[self.getLevel() + 1] % 5) {
		case 1 : return RfPipe.PIPE_1;
		case 2 : return RfPipe.PIPE_2;
		case 3 : return RfPipe.PIPE_3;
		case 4 : return RfPipe.PIPE_4;
		case 0 : return RfPipe.PIPE_5;

		default: return RfPipe.PIPE_0;
		}
	}

}
