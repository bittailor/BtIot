package ch.bittailor.iot.san.nrf24;

import java.io.Closeable;
import java.nio.ByteBuffer;

import ch.bittailor.iot.san.utils.Utilities;

public interface RfDevice extends Closeable {
	static public final int MAX_PAYLOAD_SIZE = 32;
	
	public enum DataRate {
		DR_250_KBPS,
		DR_1_MBPS,
		DR_2_MBPS
	}
	
	public enum TransceiverMode {
		TX_MODE,
		RX_MODE
	};
	
	public static class Status {
		private final int mStatus;
        
		public Status(byte status) {
			mStatus = status;
		}

		boolean dataReady() {
			return Utilities.toBoolean(mStatus & 0x40);
		}
		
		boolean dataSent() {
			return Utilities.toBoolean(mStatus & 0x20);
		}
		
		boolean retransmitsExceeded() {
			return Utilities.toBoolean(mStatus & 0x10);
		}

		boolean receiveFifoEmpty() {
			return (mStatus & 0x0e) == 0x0e;
		}

		boolean transmitFifoFull() {
			return Utilities.toBoolean(mStatus & 0x01);
		}

		@Override
		public String toString() {
			return Utilities.toHexString((byte)mStatus);
		}
     };

	
    Status status();	

	void dynamicPayloadEnabled(RfPipe iPipe, boolean iValue);

	boolean dynamicPayloadEnabled(RfPipe pipe);

	void dynamicPayloadFeatureEnabled(boolean value);

	boolean dynamicPayloadFeatureEnabled();

	ByteBuffer readReceivePayload();

	RfPipe readReceivePipe();

	int availableReceivePayload();

	int writeTransmitPayload(ByteBuffer data);

	void flushReceiveFifo();

	boolean isReceiveFifoFull();

	boolean isReceiveFifoEmpty();

	void flushTransmitFifo();

	boolean isTransmitFifoFull();

	boolean isTransmitFifoEmpty();

	void transmitAddress(RfAddress address);

	RfAddress transmitAddress();

	void receivePayloadSize(RfPipe pipe, int size);

	int receivePayloadSize(RfPipe pipe);

	void receivePipeEnabled(RfPipe pipe, boolean value);

	boolean receivePipeEnabled(RfPipe pipe);

	void receiveAddress(RfPipe pipe, RfAddress address);

	RfAddress receiveAddress(RfPipe pipe);

	void dataRate(DataRate dataRate);

	DataRate dataRate();

	void channel(int channel);

	int channel();

	int autoRetransmitCounter();

	void autoRetransmitCount(int count);

	int autoRetransmitCount();

	void autoRetransmitDelay(int delay);

	int autoRetransmitDelay();
	
	void transceiverMode(TransceiverMode mode);

	TransceiverMode transceiverMode();

	void powerUp(boolean value);

	boolean powerUp();

	void clearRetransmitsExceeded();

	void clearDataSent();

	void clearDataReady();

	
}
