package ch.bittailor.iot.core.wsn;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RfPacketSocketImpl implements RfPacketSocket {
	private static final Logger LOG = LoggerFactory.getLogger(RfPacketSocketImpl.class);
	
	private final ExecutorService mExecutorService;
	private final RfNetworkSocket mNetworkSocket;
	private final AtomicReference<RfPacketSocket.Listener> mListener;
	
	public RfPacketSocketImpl(ExecutorService executorService, RfNetworkSocket networkSocket) {
		mExecutorService = executorService;
		mNetworkSocket = networkSocket;
		mListener = new AtomicReference<RfPacketSocket.Listener>(new NullListener());
		mNetworkSocket.startListening(new RfNetworkSocket.Listener() {
			@Override
			public void packetReceived(RfSocketAddress source, ByteBuffer packet) {
				mListener.get().received(source, packet);
			}
		});
	}
	
	@Override
	public void close() throws Exception {
		mNetworkSocket.stopListening();
		mNetworkSocket.close();
		mExecutorService.shutdown();
	}
	
	@Override
	public int payloadCapacity() {
		return mNetworkSocket.payloadCapacity();
	}

	@Override
	public int send(final RfSocketAddress destination, final ByteBuffer payload) {
		int sendSize = Math.min(payload.remaining(), payloadCapacity());	
		try {
			Future<Boolean> future = mExecutorService.submit(new Callable<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return mNetworkSocket.send(destination, payload);
						}
			});
			if(future.get()) {
				return sendSize;
			}
			return -1;
		} catch (InterruptedException | ExecutionException e) {
			throw new  RuntimeException(e);
		}
	}
	
	
	
	@Override
	public void setListener(Listener listener) {
		mListener.set(listener);
		
	}

	@Override
	public void resetListener() {
		mListener.set(new NullListener());
	}

	private static class NullListener implements RfPacketSocket.Listener {
		@Override
		public void received(RfSocketAddress source, ByteBuffer payload) {
			LOG.warn("drop packet no listener set");
		}
	}

	/*
	@Override
	public RfSocketAddress receive(ByteBuffer payload) {
		try{
			Item item = mReceivedQueue.take();
			if(item.mSource == null) {
				return null;
			}
			payload.put(item.mPacket);
			payload.flip();
			return item.mSource;
		} catch (InterruptedException e) {
			throw new  RuntimeException(e);
		}
	}

	private static class Item {
		public final RfSocketAddress mSource;
		public final ByteBuffer mPacket;

		public Item(RfSocketAddress source, ByteBuffer packet){
			mSource = source;
			mPacket = packet;		
		}
	}
	*/
	
	
	
	
	
}
