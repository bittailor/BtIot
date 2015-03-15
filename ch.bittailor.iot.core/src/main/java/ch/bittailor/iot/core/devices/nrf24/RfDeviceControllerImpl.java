package ch.bittailor.iot.core.devices.nrf24;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;
import jdk.dio.gpio.PinEvent;
import jdk.dio.gpio.PinListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bittailor.iot.core.devices.nrf24.RfDeviceController.Configuration.PipeConfiguration;
import ch.bittailor.iot.core.utils.Utilities;

public class RfDeviceControllerImpl implements RfDeviceController {

	private static final Logger LOG = LoggerFactory.getLogger(RfDeviceControllerImpl.class);

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
		
		try {
			mPower.setDirection(GPIOPin.OUTPUT);
			mChipEnable.setDirection(GPIOPin.OUTPUT);
			mInterruptPin.setDirection(GPIOPin.INPUT);
			mInterruptPin.setTrigger(GPIOPinConfig.TRIGGER_FALLING_EDGE);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
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
		LOG.debug("...send payload of size {} with ", sentSize, Utilities.toHexString(packet));
		LOG.debug("transceiverMode {}", mDevice.transceiverMode());
		LOG.debug("write current state is {}", mCurrentState.getClass().getName());

		mCurrentState.ToStandbyI();

		RfAddress backupPipe0 = mDevice.receiveAddress(RfPipe.PIPE_0);
		RfAddress transmitPipeAddress = mDevice.receiveAddress(pipe);
		mDevice.transmitAddress(transmitPipeAddress);
		mDevice.receiveAddress(RfPipe.PIPE_0, transmitPipeAddress);

		packet.mark();
		mDevice.writeTransmitPayload(packet);
		while(mDevice.isTransmitFifoEmpty()) {
			LOG.warn("transmit FIFO empty after sending payload ==> try again ");
			packet.reset();
			mDevice.writeTransmitPayload(packet);
		}

		if(!interruptPin()) {
			LOG.error("IRQ already set before transmit - status {}", mDevice.status());
		}
		CountDownLatch writtenLatch = new CountDownLatch(1);
		if(mWrittenLatch.getAndSet(writtenLatch) != null) {
			LOG.error("old written latch was not null");
		}
		mCurrentState.ToTxMode();
		boolean flushTransmitFifo = false;
		try {
			// MaxRetry [15] * ( MaxRetryDelay [4ms] + MaxTrasnmittionTime [0.5ms]) => ~100ms
			if(!writtenLatch.await(100, TimeUnit.MILLISECONDS)) {
				mWrittenLatch.set(null);
				flushTransmitFifo = true;
				sentSize = 0;
				LOG.warn("await for written latch was timed out");
			}
		} catch (InterruptedException e) {
			mWrittenLatch.set(null);
			flushTransmitFifo = true;
			sentSize = 0;
			LOG.error("await for written latch was interrupted",e);
			
		} 
		mCurrentState.ToStandbyI();
		RfDevice.Status status = mDevice.status();
		LOG.debug("status after IRQ {}", status);
				
		if (status.retransmitsExceeded()) {
			mDevice.clearRetransmitsExceeded();
			flushTransmitFifo = true;
			LOG.warn("transmitPacket: send failed retransmits exceeded!");
			sentSize = 0;
		}

		if (status.dataSent()) {
			mDevice.clearDataSent();
		}
		
		if(!interruptPin()) {
			LOG.error("IRQ still set after transmit - status = {} ", status);
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

		LOG.info("Rf24DeviceController::configureDevice - channel is {}", mConfiguration.mChannel);

		mDevice.dynamicPayloadFeatureEnabled(true);
		mDevice.autoRetransmitDelay(mConfiguration.mAutoRetransmitDelay);
		mDevice.autoRetransmitCount(0xf);
		mDevice.channel(mConfiguration.mChannel);
		mDevice.dataRate(RfDevice.DataRate.DR_2_MBPS);

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
		LOG.debug("onInterrupt - InterruptState = {}", interruptState);
		switch (interruptState) {
			case Ignore: {
				LOG.warn("IRQ in InterruptState Ignore");
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
					LOG.warn("written latch is null on Tx IRQ");
					return;
				}
				writtenLatch.countDown();
				return;
			}
		}
	}
	
	private void readReceiveData() {
		while(!mDevice.isReceiveFifoEmpty()) {
			LOG.debug("receive payload ...");
			final RfPipe pipe = mDevice.readReceivePipe();
			final ByteBuffer packet = mDevice.readReceivePayload();
			if(packet.remaining() <= 0) {
				LOG.error("invalid read size of {} => drop packet", packet.remaining());
			} else {
				LOG.debug("... payload received of size {} with {}",
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
	
	private void changeState(StateBase newState) {
		LOG.debug("Change state {} => {}", mCurrentState.toString(), newState.toString());
		mCurrentState = newState;
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
		public String toString() {
			return "Off";
		}
		
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
			Utilities.delayInMilliseconds(100);
			changeState(mPowerDown);
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
		
		@Override
		public String toString() {
			return "PowerDown";
		}
		
		@Override
		public void ToOff() {
			power(false);
			changeState(mOff);
		}

		@Override
		public void ToPowerDown() {
			// self nothing to do
		}

		@Override
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
			Utilities.delayInMicroseconds(150);
			changeState(mStandbyI);
		}

		@Override
		public void ToRxMode(){
			ToStandbyI();
			mCurrentState.ToRxMode();
		}

		@Override
		public void ToTxMode(){
			ToStandbyI();
			mCurrentState.ToTxMode();
		}

	}

	private class StandbyI implements StateBase {
		
		@Override
		public String toString() {
			return "StandbyI";
		}
		
		@Override
		public void ApplyTo(StateBase other) {
			other.ToStandbyI();
		}
		
		@Override
		public void ToOff() {
			ToPowerDown();
			mCurrentState.ToOff();
		}

		@Override
		public void ToPowerDown(){
			mDevice.powerUp(false);
			try {
				mInterruptPin.setInputListener(null);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			changeState(mPowerDown);
		}

		@Override
		public void ToStandbyI() {
			// self nothing to do
		}

		@Override
		public void ToRxMode(){
			if(!interruptPin()) {
				LOG.warn("IRQ already set before StandbyI::ToRxMode transition - status = {} ", mDevice.status());
			}

			mDevice.clearDataReady();
			mDevice.clearDataSent();
			mDevice.clearRetransmitsExceeded();

			if(!interruptPin()) {
				LOG.error("IRQ still set even after a clear before StandbyI::ToRxMode transition - status = {}", mDevice.status());
			}

			mInterruptState.set(InterruptState.Rx);

			mDevice.transceiverMode(RfDevice.TransceiverMode.RX_MODE);
			chipEnable(true);
			Utilities.delayInMicroseconds(130);
			changeState(mRxMode);
			LOG.debug("RxMode");
		}

		@Override
		public void ToTxMode(){
			if (mDevice.isTransmitFifoEmpty())
			{
				LOG.warn("StandbyI::ToTxMode: transmit fifo is empty => StandbyI !");
				return;
			}

			if(!interruptPin()){
				LOG.warn("StandbyI::ToTxMode: InterruptPin still set => clear all !");
				mDevice.clearDataReady();
				mDevice.clearDataSent();
				mDevice.clearRetransmitsExceeded();
			}

			mInterruptState.set(InterruptState.Tx);
			mDevice.transceiverMode(RfDevice.TransceiverMode.TX_MODE);
			chipEnable(true);
			Utilities.delayInMicroseconds(140);
			changeState(mTxMode);
		}

	}

	private class RxMode implements StateBase {
		
		@Override
		public String toString() {
			return "RxMode";
		}
		
		@Override
		public void ApplyTo(StateBase other) {
			other.ToRxMode();
		}
		
		@Override
		public void ToOff() {
			ToPowerDown();
			mCurrentState.ToOff();
		}
		
		@Override
		public void ToPowerDown(){
			ToStandbyI();
			mCurrentState.ToPowerDown();
		}

		@Override
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

			Utilities.delayInMicroseconds(10);
			changeState(mStandbyI);
		}

		@Override
		public void ToRxMode(){
			// self nothing to do
		}

		@Override
		public void ToTxMode(){
			ToStandbyI();
			mCurrentState.ToTxMode();
		}

	}

	private class TxMode implements StateBase{
		
		@Override
		public String toString() {
			return "TxMode";
		}
		
		@Override
		public void ApplyTo(StateBase other) {
			other.ToTxMode();
		}
		
		@Override
		public void ToPowerDown(){
			ToStandbyI();
			mCurrentState.ToPowerDown();
		}
		
		@Override
		public void ToOff() {
			ToPowerDown();
			mCurrentState.ToOff();
		}

		@Override
		public void ToStandbyI(){
			chipEnable(false);
			Utilities.delayInMicroseconds(10);
			changeState(mStandbyI);
		}

		@Override
		public void ToRxMode(){
			ToStandbyI();
			mCurrentState.ToRxMode();
		}

		@Override
		public void ToTxMode(){
			// self nothing to do
		}
	}

}
