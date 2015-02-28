package ch.bittailor.iot.san.nrf24;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class RfPacketSocketImpl implements RfPacketSocket {

	private final ExecutorService mExecutorService;
	private final RfNetworkSocket mNetworkSocket;
	private final BlockingQueue<Item> mReceivedQueue;
	
	public RfPacketSocketImpl(ExecutorService executorService, RfNetworkSocket networkSocket) {
		mExecutorService = executorService;
		mNetworkSocket = networkSocket;
		mReceivedQueue = new LinkedBlockingQueue<Item>();
		mNetworkSocket.startListening(new RfNetworkSocket.Listener() {
			@Override
			public void packetReceived(RfSocketAddress source, ByteBuffer packet) {
				try {
					mReceivedQueue.put(new Item(source,packet));
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	@Override
	public void close() throws IOException {
		mNetworkSocket.stopListening();
		try {
			mReceivedQueue.put(new Item(null,null));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public int payloadCapacity() {
		return mNetworkSocket.payloadCapacity();
	}

	@Override
	public int send(final ByteBuffer payload, final RfSocketAddress destination) {
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
	
	
	
	
	
}
