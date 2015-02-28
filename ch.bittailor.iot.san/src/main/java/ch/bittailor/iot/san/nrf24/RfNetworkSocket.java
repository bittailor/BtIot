package ch.bittailor.iot.san.nrf24;

import java.nio.ByteBuffer;

public interface RfNetworkSocket {
	
	int payloadCapacity();
	
	boolean send(RfSocketAddress destination, ByteBuffer packet);
    void startListening(Listener listener);
    void stopListening();
    
    public static interface Listener {
    	void packetReceived(RfSocketAddress source, ByteBuffer packet);
    }

    
}
