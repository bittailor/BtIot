package ch.bittailor.iot.san.nrf24;

import ch.bittailor.iot.devices.nrf24.RfAddress;
import ch.bittailor.iot.devices.nrf24.RfDeviceController;
import ch.bittailor.iot.devices.nrf24.RfPipe;

public class RfNetworkRoutingAlgorithm {
	
	public RfPipe calculateRoutingPipe(int self, int destination) {
		return calculateRoutingPipe(new RfSocketAddress(self), new RfSocketAddress(destination));
	}
	
	public RfPipe calculateRoutingPipe(RfSocketAddress self, RfSocketAddress destination) {
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

	public void configurePipe(RfSocketAddress iSelf, RfPipe iPipe, RfDeviceController.Configuration.PipeConfiguration oPipeConfiguration) {
	   if(isLeafNode(iSelf) && iPipe != RfPipe.PIPE_0) {
	      oPipeConfiguration.mEnabled = false;
	      return;
	   }

	   oPipeConfiguration.mEnabled = true;
	   oPipeConfiguration.mAddress = calculatePipeAddress(iSelf,iPipe);
	}
	
	private RfAddress calculatePipeAddress(RfSocketAddress iSelf, RfPipe iPipe) {
	   int byte0 = iSelf.getId();
	   int child = iSelf.getId() * 5;
	   switch(iPipe) {
	      case PIPE_0 : byte0 = iSelf.getId(); break;
	      case PIPE_1 : byte0 = child + 1; break;
	      case PIPE_2 : byte0 = child + 2; break;
	      case PIPE_3 : byte0 = child + 3; break;
	      case PIPE_4 : byte0 = child + 4; break;
	      case PIPE_5 : byte0 = child + 5; break;
	   }
	   return new RfAddress(0xC2,0xC2,0xC2,0xC2,byte0);
	}
	
	private boolean isLeafNode(RfSocketAddress iSelf) {
	   return iSelf.getId() > 50;
	}
	
	
}
