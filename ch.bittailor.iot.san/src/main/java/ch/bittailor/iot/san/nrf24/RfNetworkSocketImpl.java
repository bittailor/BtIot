package ch.bittailor.iot.san.nrf24;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bittailor.iot.devices.nrf24.RfDeviceController;
import ch.bittailor.iot.devices.nrf24.RfDeviceImpl;
import ch.bittailor.iot.devices.nrf24.RfPipe;
import ch.bittailor.iot.san.utils.Utilities;

public class RfNetworkSocketImpl implements RfNetworkSocket {
	private static final Logger LOGGER = LoggerFactory.getLogger(RfDeviceImpl.class);

	
	private final int HEADER_SIZE = 3;
	private final RfSocketAddress mAddress;
	private final RfDeviceController mController;
	private final RfNetworkRoutingAlgorithm mRouting;
	private int mIdCounter;
	private Listener mListener;
	
	RfNetworkSocketImpl(RfSocketAddress address, RfDeviceController controller) {
		mAddress = address;
		mController = controller;
		mRouting = new RfNetworkRoutingAlgorithm();
		
		RfDeviceController.Configuration configuration = new RfDeviceController.Configuration();
		configuration.mAutoRetransmitDelay = ((mAddress.getId() % 6) * 2) + 5;
		for(RfPipe pipe : RfPipe.values()) {
			mRouting.configurePipe(mAddress, pipe, configuration.pipeConfiguration(pipe));
		}
		mController.configure(configuration);		
	}
	
	
	
	@Override
	public void close() throws IOException {
		mController.close();	
	}



	@Override
	public int payloadCapacity() {
		return mController.payloadCapacity() - HEADER_SIZE;
	}

	@Override
	public boolean send(RfSocketAddress destination, ByteBuffer packet) {
		ByteBuffer networkPacket = ByteBuffer.allocate(packet.remaining() + HEADER_SIZE);
		networkPacket.put((byte)mAddress.getId());
		networkPacket.put((byte)destination.getId());
		networkPacket.put((byte)mIdCounter++);
		networkPacket.put(packet);
		networkPacket.flip();
		if (destination.equals(mAddress)) {
			receiveInternal(mAddress, packet);
			return true;
		}
		return sendInternal(destination, networkPacket);
	}

	@Override
	public void startListening(Listener listener) {
		mListener = listener;
		mController.startListening(new RfDeviceController.Listener() {		
			@Override
			public void packageReceived(RfPipe pipe, ByteBuffer packet) {
				onPacketReceived(pipe,packet);
				
			}
		});
	}

	@Override
	public void stopListening() {
		mListener = null;
		mController.stopListening();
	}
	
	boolean sendInternal(RfSocketAddress destination, ByteBuffer packet) {
		RfPipe pipe = mRouting.calculateRoutingPipe(mAddress, destination);
		int counter = 0;
		while(!mController.write(pipe, packet)) {
			counter++;
			if (counter >= 5) {
				LOGGER.warn("NetworkSocket send {} => {} failed after {} retries",
						new Object [] {mAddress,
						destination,
						counter});
				return false;
			}
			LOGGER.warn("NetworkSocket send {} => {} failed do retry {} after delay",
					new Object [] {mAddress,
					destination,
					counter});
			Utilities.delay(counter*100);
		}
		return true;
	}
	
	void receiveInternal(RfSocketAddress source, ByteBuffer packet) {
		if(mListener != null) {
			mListener.packetReceived(source,packet);
		}
	}
	
	void onPacketReceived(RfPipe iPipe, ByteBuffer packet) {
		packet.mark();
		RfSocketAddress source = new RfSocketAddress(packet.get());
		RfSocketAddress destination = new RfSocketAddress(packet.get());
		packet.get();	
		if (destination.equals(mAddress)) {
			LOGGER.debug("route  {} => {} ", source, destination);
			packet.reset();
			sendInternal(destination,packet);
			return;
		}
		LOGGER.debug("receive  {} => {} ", source, destination);	
		receiveInternal(source, packet.slice());
	}

}
