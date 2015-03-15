package ch.bittailor.iot.core.integrationtest.wsn;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bittailor.iot.core.devices.nrf24.RfDeviceImpl;
import ch.bittailor.iot.core.utils.Utilities;
import ch.bittailor.iot.core.wsn.RfPacketSocket;
import ch.bittailor.iot.core.wsn.RfPacketSocketFactoryImpl;
import ch.bittailor.iot.core.wsn.RfSocketAddress;

public class RfPacketSocketImplTest {
	private static final Logger LOG = LoggerFactory.getLogger(RfDeviceImpl.class);

	
	private RfPacketSocket mSocket;
	
	@Before
	public void Before() {
		LOG.info("Create rf packet socket\n\n\n");
		mSocket = new RfPacketSocketFactoryImpl().create(0);
	}
	
	@After
	public void After() throws IOException, InterruptedException {
		mSocket.close();
		LOG.info("Closed rf packet socket\n\n\n");			
		Thread.sleep(500);
	}
	
	@Test
	public void sendAndReceiveViaPingServer_oneMessage() throws InterruptedException {
		byte[] array = new byte[]{9,8,7,6};
		ByteBuffer payload = ByteBuffer.wrap(array);
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<ByteBuffer> answer = new AtomicReference<ByteBuffer>();
		mSocket.setListener(new RfPacketSocket.Listener() {			
			@Override
			public void received(RfSocketAddress source, ByteBuffer payload) {
				answer.set(payload);	
				latch.countDown();
			}
		});
		int sent = mSocket.send(new RfSocketAddress(2), payload);
		assertTrue("wait for latch ", latch.await(10, TimeUnit.SECONDS));	
		assertEquals(array.length, sent);
		byte[] answerArray = new byte[answer.get().remaining()];
		answer.get().get(answerArray);
		assertArrayEquals(array, answerArray);	
	}
	
	@Test
	public void sendAndReceiveViaPingServer_aStringMessage() throws InterruptedException {
		String message = "Hello nRf24 WSN";
		ByteBuffer payload = ByteBuffer.wrap(message.getBytes());
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<ByteBuffer> answer = new AtomicReference<ByteBuffer>();
		mSocket.setListener(new RfPacketSocket.Listener() {			
			@Override
			public void received(RfSocketAddress source, ByteBuffer payload) {
				answer.set(payload);	
				latch.countDown();
			}
		});
		int sent = mSocket.send(new RfSocketAddress(2), payload);
		assertTrue("wait for latch ", latch.await(10, TimeUnit.SECONDS));	
		assertEquals(message.getBytes().length, sent);
		String answerMessage = Utilities.getString(answer.get());
		assertEquals(message, answerMessage);	
	}
	
	@Test
	public void sendAndReceiveViaPingServer_multipleMessage() throws InterruptedException {
		final int numberOfMessages = 5;
		final AtomicReference<ByteBuffer> answer = new AtomicReference<ByteBuffer>();
		final AtomicReference<CountDownLatch> latch = new AtomicReference<CountDownLatch>();
		
		mSocket.setListener(new RfPacketSocket.Listener() {			
			@Override
			public void received(RfSocketAddress source, ByteBuffer payload) {
				answer.set(payload);
				latch.get().countDown();
			}
		});
		
		for (int i = 0; i < numberOfMessages; i++) {
			latch.set(new CountDownLatch(1));
			ByteBuffer payload = ByteBuffer.allocate(4);
			payload.putInt(i).flip();
			assertEquals("send size ",4, mSocket.send(new RfSocketAddress(2), payload));
			assertTrue("wait for latch ", latch.get().await(10, TimeUnit.SECONDS));
			assertEquals("answer ",i, answer.get().getInt());
			
		}
	}
	
}
