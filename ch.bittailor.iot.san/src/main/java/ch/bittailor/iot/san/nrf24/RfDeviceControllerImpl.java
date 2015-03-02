package ch.bittailor.iot.san.nrf24;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.PinEvent;
import jdk.dio.gpio.PinListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bittailor.iot.san.nrf24.RfDevice.DataRate;
import ch.bittailor.iot.san.nrf24.RfDevice.Status;
import ch.bittailor.iot.san.nrf24.RfDeviceController.Configuration.PipeConfiguration;
import ch.bittailor.iot.san.utils.Utilities;

public class RfDeviceControllerImpl implements RfDeviceController {

	private static final Logger s_logger = LoggerFactory.getLogger(RfDeviceControllerImpl.class);

	private final Executor mExecutor;
	private final RfDevice mDevice;
	private final GPIOPin mPower;
	private final GPIOPin mChipEnable;
	private final GPIOPin mInterruptPin;
	private final AtomicReference<InterruptState> mInterruptState;
	private final AtomicReference<CountDownLatch> mWrittenLatch;
	private final Off mOff; 
	private final PowerDown mPowerDown; 
	private final StandbyI mStandbyI; 
	private final RxMode mRxMode; 
	private final TxMode mTxMode;
	
	private StateBase mCurrentState;
	private Configuration mConfiguration;
	private Listener mListener;

	
	public RfDeviceControllerImpl(Executor executor, RfDevice device, GPIOPin power ,GPIOPin chipEnable, GPIOPin interruptPin) {
		mExecutor = executor;
		mDevice = device;
		mPower = power;
		mChipEnable = chipEnable;
		mInterruptPin = interruptPin;
		mInterruptState = new AtomicReference<RfDeviceControllerImpl.InterruptState>(InterruptState.Ignore);
		mWrittenLatch = new AtomicReference<CountDownLatch>();
		mOff = new Off();
		mPowerDown = new PowerDown();
		mStandbyI = new StandbyI();
		mRxMode = new RxMode();
		mTxMode = new TxMode();
		mCurrentState = mOff;
		chipEnable(false);
	}
	
	@Override
	public void close() throws IOException {
		mCurrentState.ToOff();
		mDevice.close();
		mChipEnable.close();
		mInterruptPin.close();
		mPower.close();
	}

	@Override
	public int payloadCapacity() {
		return RfDevice.MAX_PAYLOAD_SIZE;
	}

	@Override
	public void configure(Configuration configuration) {
		StateBase originalState = mCurrentState;
		mCurrentState.ToPowerDown();
		this.mConfiguration = configuration;
		originalState.ApplyTo(mCurrentState);
	}

	@Override
	public boolean write(RfPipe pipe, ByteBuffer packet) {
		int size = transmitPacket(pipe, packet);
		return size != 0;
	}

	private int transmitPacket(RfPipe pipe, ByteBuffer packet) {
		StateBase originalState = mCurrentState;
		int sentSize = packet.remaining();
		s_logger.debug("...send payload of size {} with ", sentSize, Utilities.toHexString(packet));
		s_logger.debug("transceiverMode {}", mDevice.transceiverMode());
		s_logger.debug("write current state is {}", mCurrentState.getClass().getName());

		mCurrentState.ToStandbyI();

		RfAddress backupPipe0 = mDevice.receiveAddress(RfPipe.PIPE_0);
		RfAddress transmitPipeAddress = mDevice.receiveAddress(pipe);
		mDevice.transmitAddress(transmitPipeAddress);
		mDevice.receiveAddress(RfPipe.PIPE_0, transmitPipeAddress);

		mDevice.writeTransmitPayload(packet);
		while(mDevice.isTransmitFifoEmpty()) {
			s_logger.warn("transmit FIFO empty after sending payload ==> try again ");
			mDevice.writeTransmitPayload(packet);
		}

		if(!interruptPin()) {
			s_logger.error("IRQ already set before transmit - status {}", mDevice.status());
		}
		CountDownLatch writtenLatch = new CountDownLatch(1);
		if(mWrittenLatch.getAndSet(writtenLatch) != null) {
			s_logger.error("old written latch was not null");
		}
		mCurrentState.ToTxMode();
		boolean flushTransmitFifo = false;
		try {
			// MaxRetry [15] * ( MaxRetryDelay [4ms] + MaxTrasnmittionTime [0.5ms]) => ~100ms
			if(!writtenLatch.await(100, TimeUnit.MILLISECONDS)) {
				mWrittenLatch.set(null);
				flushTransmitFifo = true;
				sentSize = 0;
				s_logger.warn("await for written latch was timed out");
			}
		} catch (InterruptedException e) {
			mWrittenLatch.set(null);
			flushTransmitFifo = true;
			sentSize = 0;
			s_logger.error("await for written latch was interrupted",e);
			
		} 
		mCurrentState.ToStandbyI();
		Status status = mDevice.status();
		s_logger.debug("status after IRQ {}", status);
				
		if (status.retransmitsExceeded()) {
			mDevice.clearRetransmitsExceeded();
			flushTransmitFifo = true;
			s_logger.warn("transmitPacket: send failed retransmits exceeded!");
			sentSize = 0;
		}

		if (status.dataSent()) {
			mDevice.clearDataSent();
		}
		
		if(!interruptPin()) {
			s_logger.error("IRQ still set after transmit - status = {} ", status);
		}

		if (flushTransmitFifo) {
			mDevice.flushTransmitFifo();
		}

	   mDevice.receiveAddress(RfPipe.PIPE_0, backupPipe0);

	   originalState.ApplyTo(mCurrentState);
	   return sentSize;
	}
	
	public void startListening(Listener listener) {
		mListener = listener;
		mCurrentState.ToRxMode();
	}

	public void stopListening() {
		mCurrentState.ToStandbyI();
		mListener = null;
	}
	
	private void configureDevice() {

		s_logger.info("Rf24DeviceController::configureDevice - channel is {}", mConfiguration.mChannel);

		mDevice.dynamicPayloadFeatureEnabled(true);
		mDevice.autoRetransmitDelay(mConfiguration.mAutoRetransmitDelay);
		mDevice.autoRetransmitCount(0xf);
		mDevice.channel(mConfiguration.mChannel);
		mDevice.dataRate(DataRate.DR_2_MBPS);

		for (RfPipe pipe : RfPipe.values()) {
			PipeConfiguration pipeConfiguration = mConfiguration.pipeConfiguration(pipe);
			if(pipeConfiguration.mEnabled) {
				mDevice.receivePayloadSize(pipe, RfDevice.MAX_PAYLOAD_SIZE);
				mDevice.receivePipeEnabled(pipe, true);
				mDevice.dynamicPayloadEnabled(pipe, true);
				mDevice.receiveAddress(pipe,pipeConfiguration.mAddress);
			} else {
				mDevice.receivePipeEnabled(pipe, false);
			}
		}
	}
	
	private void onInterrupt() {
		InterruptState interruptState =mInterruptState.get();
		s_logger.debug("onInterrupt - InterruptState = {}", interruptState);
		switch (interruptState) {
			case Ignore: {
				s_logger.warn("IRQ in InterruptState Ignore");
				return;
			}
			case Rx:{
				mExecutor.execute(new Runnable() {
					@Override
					public void run() {
						readReceiveData();
					}
				});
				return;
			}
			case Tx:{
				CountDownLatch writtenLatch = mWrittenLatch.getAndSet(null);
				if(writtenLatch == null) {
					s_logger.warn("written latch is null on Tx IRQ");
					return;
				}
				writtenLatch.countDown();
				return;
			}
		}
	}
	
	private void readReceiveData() {
		while(!mDevice.isReceiveFifoEmpty()) {
			s_logger.debug("receive payload ...");
			final RfPipe pipe = mDevice.readReceivePipe();
			final ByteBuffer packet = mDevice.readReceivePayload();
			if(packet.remaining() <= 0) {
				s_logger.error("invalid read size of {} => drop packet", packet.remaining());
			} else {
				s_logger.debug("... payload received of size {} with {}",
						packet.remaining(),
						Utilities.toHexString(packet));
				mExecutor.execute(new Runnable() {				
					@Override
					public void run() {
						handleReceiveData(pipe, packet);

					}
				});	
			}
			mDevice.clearDataReady();	
		}
	}

	private void handleReceiveData(RfPipe pipe, ByteBuffer packet) {
		if(mListener != null) {
			mListener.packageReceived(pipe, packet);
		}
	}
	
	private boolean interruptPin() {
		try {
			return mInterruptPin.getValue();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 
	}

	private void chipEnable(boolean value) {
		try {
			mChipEnable.setValue(value);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void power(boolean value) {
		try {
			mPower.setValue(value);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private enum InterruptState {
		Ignore,
		Rx,
		Tx
	};

	// -- controller state machine 
	
	private interface StateBase {
		void ApplyTo(StateBase other);
		void ToOff();
		void ToPowerDown();
		void ToStandbyI();
		void ToRxMode();
		void ToTxMode();
	}

	private class Off implements StateBase {

		@Override
		public void ApplyTo(StateBase other) {
			other.ToOff();
		}

		@Override
		public void ToOff() {
			// self nothing to do			
		}

		@Override
		public void ToPowerDown() {
			power(true);
			Utilities.delay(100);
			mCurrentState = mPowerDown;
		}

		@Override
		public void ToStandbyI() {
			ToPowerDown();
			mCurrentState.ToStandbyI();
			
		}

		@Override
		public void ToRxMode() {
			ToPowerDown();
			mCurrentState.ToRxMode();
		}

		@Override
		public void ToTxMode() {
			ToPowerDown();
			mCurrentState.ToTxMode();			
		}
	
	}
	
	private class PowerDown implements StateBase {
		public void ApplyTo(StateBase other) {
			other.ToPowerDown();
		}
		
		public void ToOff() {
			power(false);
			mCurrentState = mOff;
		}

		public void ToPowerDown() {
			// self nothing to do
		}

		public void ToStandbyI() {
			mDevice.flushReceiveFifo();
			mDevice.flushTransmitFifo();
			configureDevice();			
			try {
				mInterruptPin.setInputListener(new PinListener() {
					@Override
					public void valueChanged(PinEvent pinEvent) {
						onInterrupt();
					}
				});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			mDevice.powerUp(true);
			Utilities.delay(150);
			mCurrentState = mStandbyI;
		}

		public void ToRxMode(){
			ToStandbyI();
			mCurrentState.ToRxMode();
		}

		public void ToTxMode(){
			ToStandbyI();
			mCurrentState.ToTxMode();
		}

	}

	private class StandbyI implements StateBase {
		public void ApplyTo(StateBase other) {
			other.ToStandbyI();
		}
		
		public void ToOff() {
			ToPowerDown();
			mCurrentState.ToOff();
		}

		public void ToPowerDown(){
			mDevice.powerUp(false);
			try {
				mInterruptPin.setInputListener(null);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			mCurrentState = mPowerDown;
		}

		public void ToStandbyI() {
			// self nothing to do
		}

		public void ToRxMode(){
			if(!interruptPin()) {
				s_logger.warn("IRQ already set before StandbyI::ToRxMode transition - status = {} ", mDevice.status());
			}

			mDevice.clearDataReady();
			mDevice.clearDataSent();
			mDevice.clearRetransmitsExceeded();

			if(!interruptPin()) {
				s_logger.error("IRQ still set even after a clear before StandbyI::ToRxMode transition - status = {}", mDevice.status());
			}

			mInterruptState.set(InterruptState.Rx);

			mDevice.transceiverMode(RfDevice.TransceiverMode.RX_MODE);
			chipEnable(true);
			Utilities.delay(130);
			mCurrentState = mRxMode;
			s_logger.debug("RxMode");
		}

		public void ToTxMode(){
			if (mDevice.isTransmitFifoEmpty())
			{
				s_logger.warn("StandbyI::ToTxMode: transmit fifo is empty => StandbyI !");
				return;
			}

			if(!interruptPin()){
				s_logger.warn("StandbyI::ToTxMode: InterruptPin still set => clear all !");
				mDevice.clearDataReady();
				mDevice.clearDataSent();
				mDevice.clearRetransmitsExceeded();
			}

			mInterruptState.set(InterruptState.Tx);
			mDevice.transceiverMode(RfDevice.TransceiverMode.TX_MODE);
			chipEnable(true);
			Utilities.delay(140);
			mCurrentState = mTxMode;
		}

	}

	private class RxMode implements StateBase {
		public void ApplyTo(StateBase other) {
			other.ToRxMode();
		}
		
		public void ToOff() {
			ToPowerDown();
			mCurrentState.ToOff();
		}
		
		public void ToPowerDown(){
			ToStandbyI();
			mCurrentState.ToPowerDown();
		}

		public void ToStandbyI(){
			chipEnable(false);
			mDevice.transceiverMode(RfDevice.TransceiverMode.TX_MODE);

			mInterruptState.set(InterruptState.Ignore);
			if(!interruptPin()){
				readReceiveData();
				mDevice.clearDataReady();
				mDevice.clearDataSent();
				mDevice.clearRetransmitsExceeded();
			}

			Utilities.delay(10);
			mCurrentState = mStandbyI;
		}

		public void ToRxMode(){
			// self nothing to do
		}

		public void ToTxMode(){
			ToStandbyI();
			mCurrentState.ToTxMode();
		}

	}

	private class TxMode implements StateBase{
		public void ApplyTo(StateBase other) {other.ToTxMode();}
		public void ToPowerDown(){
			ToStandbyI();
			mCurrentState.ToPowerDown();
		}
		
		public void ToOff() {
			ToPowerDown();
			mCurrentState.ToOff();
		}

		public void ToStandbyI(){
			chipEnable(false);
			Utilities.delay(10);
			mCurrentState = mStandbyI;
		}

		public void ToRxMode(){
			ToStandbyI();
			mCurrentState.ToRxMode();
		}

		public void ToTxMode(){
			// self nothing to do
		}
	}

}
