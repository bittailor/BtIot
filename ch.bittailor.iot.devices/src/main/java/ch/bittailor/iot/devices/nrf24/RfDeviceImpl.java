package ch.bittailor.iot.devices.nrf24;

import java.io.IOException;
import java.nio.ByteBuffer;

import jdk.dio.spibus.SPIDevice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bittailor.iot.devices.nrf24.RfAddress;
import ch.bittailor.iot.devices.nrf24.RfPipe;

public class RfDeviceImpl implements RfDevice {
	private static final Logger LOGGER = LoggerFactory.getLogger(RfDeviceImpl.class);

	private final SPIDevice mSpi;
	
	public RfDeviceImpl(SPIDevice spi) {
		mSpi = spi;
		sleep(5);
	}
	
	@Override
	public void close() throws IOException {
		mSpi.close();
	}

	@Override
	public Status status() {
		return new Status(readRegister(OneByteRegister.REGISTER_STATUS));
	}

	@Override
	public void clearDataReady() {
		writeSubRegister(OneByteRegister.REGISTER_STATUS,1,1,6);
	}

	@Override
	public void clearDataSent() {
		writeSubRegister(OneByteRegister.REGISTER_STATUS,1,1,5);
	}

	@Override
	public void clearRetransmitsExceeded() {
		writeSubRegister(OneByteRegister.REGISTER_STATUS,1,1,4);
	}


	@Override
	public boolean powerUp() {
		return toBoolean(readSubRegister(OneByteRegister.REGISTER_CONFIG,1,1));
	}

	@Override
	public void powerUp(boolean value) {
		writeSubRegister(OneByteRegister.REGISTER_CONFIG,value?1:0,1,1);
	}


	@Override
	public TransceiverMode transceiverMode() {
		return toBoolean(readSubRegister(OneByteRegister.REGISTER_CONFIG,1,0)) ? TransceiverMode.RX_MODE : TransceiverMode.TX_MODE;
	}

	@Override
	public void transceiverMode(TransceiverMode mode) {
		writeSubRegister(OneByteRegister.REGISTER_CONFIG,mode == TransceiverMode.RX_MODE?1:0,1,0);
	}

	@Override
	public int autoRetransmitDelay() {
		return readSubRegister(OneByteRegister.REGISTER_SETUP_RETR,4,4);
	}

	@Override
	public void autoRetransmitDelay(int delay) {
		writeSubRegister(OneByteRegister.REGISTER_SETUP_RETR,delay,4,4);
	}

	@Override
	public int autoRetransmitCount() {
		return readSubRegister(OneByteRegister.REGISTER_SETUP_RETR,4,0);
	}

	@Override
	public void autoRetransmitCount(int count) {
		writeSubRegister(OneByteRegister.REGISTER_SETUP_RETR,count,4,0);
	}

	@Override
	public int autoRetransmitCounter(){
		return readSubRegister(OneByteRegister.REGISTER_OBSERVE_TX,4,0);
	}


	@Override
	public int channel() {
		byte value = (byte)(readRegister(OneByteRegister.REGISTER_RF_CH) & Masks.MASK_RF_CH.mask);
		return value & 0x7F;
	}

	@Override
	public void channel(int channel) {
		int value = channel & Masks.MASK_RF_CH.mask;
		writeRegister(OneByteRegister.REGISTER_RF_CH, value);
	}

	@Override
	public DataRate dataRate() {
		boolean low = toBoolean(readSubRegister(OneByteRegister.REGISTER_RF_SETUP, 1, 5));
		boolean high = toBoolean(readSubRegister(OneByteRegister.REGISTER_RF_SETUP, 1, 3));
		if(low) {
			return DataRate.DR_250_KBPS;
		}
		if(high) {
			return DataRate.DR_2_MBPS;
		}
		return DataRate.DR_1_MBPS;

	}

	@Override
	public void dataRate(DataRate dataRate) {
		int low = (dataRate == DataRate.DR_250_KBPS)?0x01:0x00;
		int high = (dataRate == DataRate.DR_2_MBPS)?0x01:0x00;
		writeSubRegister(OneByteRegister.REGISTER_RF_SETUP, low, 1, 5);
		writeSubRegister(OneByteRegister.REGISTER_RF_SETUP, high, 1, 3);
	}

	@Override
	public RfAddress receiveAddress(RfPipe pipe) {
		switch (pipe) {
		case PIPE_0:
			return new RfAddress(readRegister(FiveByteRegister.REGISTER_RX_ADDR_P0));
		case PIPE_1:
			return new RfAddress(readRegister(FiveByteRegister.REGISTER_RX_ADDR_P1));
		default:
			break;
		}

		ByteBuffer raw = readRegister(FiveByteRegister.REGISTER_RX_ADDR_P1);
		OneByteRegister oneByteRegister = OneByteRegister.REGISTER_RX_ADDR_P2;
		switch (pipe) {
		case PIPE_2 : oneByteRegister = OneByteRegister.REGISTER_RX_ADDR_P2; break;
		case PIPE_3 : oneByteRegister = OneByteRegister.REGISTER_RX_ADDR_P3; break;
		case PIPE_4 : oneByteRegister = OneByteRegister.REGISTER_RX_ADDR_P4; break;
		case PIPE_5 : oneByteRegister = OneByteRegister.REGISTER_RX_ADDR_P5; break;
		default     : oneByteRegister = OneByteRegister.REGISTER_RX_ADDR_P2; break;
		}
		raw.put(0,readRegister(oneByteRegister));
		return new RfAddress(raw);
	}

	@Override
	public void receiveAddress(RfPipe pipe, RfAddress address) {
		switch (pipe) {
		case PIPE_0:
			writeRegister(FiveByteRegister.REGISTER_RX_ADDR_P0, address.raw());
			return;
		case PIPE_1:
			writeRegister(FiveByteRegister.REGISTER_RX_ADDR_P1, address.raw());
			return;
		default:
			break;
		}

		OneByteRegister oneByteRegister = OneByteRegister.REGISTER_RX_ADDR_P2;
		switch (pipe) {
		case PIPE_2 : oneByteRegister = OneByteRegister.REGISTER_RX_ADDR_P2; break;
		case PIPE_3 : oneByteRegister = OneByteRegister.REGISTER_RX_ADDR_P3; break;
		case PIPE_4 : oneByteRegister = OneByteRegister.REGISTER_RX_ADDR_P4; break;
		case PIPE_5 : oneByteRegister = OneByteRegister.REGISTER_RX_ADDR_P5; break;
		default     : oneByteRegister = OneByteRegister.REGISTER_RX_ADDR_P2; break;
		}

		writeRegister(oneByteRegister, address.raw()[0]);
	}


	@Override
	public boolean receivePipeEnabled(RfPipe pipe) {
		int offset = 0;

		switch (pipe) {
		case PIPE_0 : offset = 0; break;
		case PIPE_1 : offset = 1; break;
		case PIPE_2 : offset = 2; break;
		case PIPE_3 : offset = 3; break;
		case PIPE_4 : offset = 4; break;
		case PIPE_5 : offset = 5; break;
		default     : offset = 0; break;
		}

		return toBoolean(readSubRegister(OneByteRegister.REGISTER_EN_RXADDR, 1, offset)) ;
	}

	@Override
	public void receivePipeEnabled(RfPipe pipe, boolean value) {
		int offset = 0;
		switch (pipe) {
		case PIPE_0 : offset = 0; break;
		case PIPE_1 : offset = 1; break;
		case PIPE_2 : offset = 2; break;
		case PIPE_3 : offset = 3; break;
		case PIPE_4 : offset = 4; break;
		case PIPE_5 : offset = 5; break;
		default     : offset = 0; break;
		}
		writeSubRegister(OneByteRegister.REGISTER_EN_RXADDR, toByte(value), 1, offset);
	}

	@Override
	public int receivePayloadSize(RfPipe pipe) {
		OneByteRegister oneByteRegister;
		switch (pipe) {
		case PIPE_0 : oneByteRegister = OneByteRegister.REGISTER_RX_PW_P0; break;
		case PIPE_1 : oneByteRegister = OneByteRegister.REGISTER_RX_PW_P1; break;
		case PIPE_2 : oneByteRegister = OneByteRegister.REGISTER_RX_PW_P2; break;
		case PIPE_3 : oneByteRegister = OneByteRegister.REGISTER_RX_PW_P3; break;
		case PIPE_4 : oneByteRegister = OneByteRegister.REGISTER_RX_PW_P4; break;
		case PIPE_5 : oneByteRegister = OneByteRegister.REGISTER_RX_PW_P5; break;
		default     : oneByteRegister = OneByteRegister.REGISTER_RX_PW_P0; break;
		}

		return readSubRegister(oneByteRegister,6,0);
	}

	@Override
	public void receivePayloadSize(RfPipe pipe, int size) {
		OneByteRegister oneByteRegister;
		switch (pipe) {
		case PIPE_0 : oneByteRegister = OneByteRegister.REGISTER_RX_PW_P0; break;
		case PIPE_1 : oneByteRegister = OneByteRegister.REGISTER_RX_PW_P1; break;
		case PIPE_2 : oneByteRegister = OneByteRegister.REGISTER_RX_PW_P2; break;
		case PIPE_3 : oneByteRegister = OneByteRegister.REGISTER_RX_PW_P3; break;
		case PIPE_4 : oneByteRegister = OneByteRegister.REGISTER_RX_PW_P4; break;
		case PIPE_5 : oneByteRegister = OneByteRegister.REGISTER_RX_PW_P5; break;
		default     : oneByteRegister = OneByteRegister.REGISTER_RX_PW_P0; break;
		}
		writeSubRegister(oneByteRegister,size,6,0);
	}

	@Override
	public RfAddress transmitAddress() {
		return new RfAddress(readRegister(FiveByteRegister.REGISTER_TX_ADDR));
	}

	@Override
	public void transmitAddress(RfAddress address) {
		writeRegister(FiveByteRegister.REGISTER_TX_ADDR, address.raw());
	}

	@Override
	public boolean isTransmitFifoEmpty() {
		return toBoolean(readSubRegister(OneByteRegister.REGISTER_FIFO_STATUS,1,4));
	}

	@Override
	public boolean isTransmitFifoFull() {
		return toBoolean(readSubRegister(OneByteRegister.REGISTER_FIFO_STATUS,1,5));
	}

	@Override
	public void flushTransmitFifo() {
		writeAndRead(Commands.CMD_FLUSH_TX.word);
	}

	@Override
	public boolean isReceiveFifoEmpty() {
		return toBoolean(readSubRegister(OneByteRegister.REGISTER_FIFO_STATUS,1,0));
	}

	@Override
	public boolean isReceiveFifoFull() {
		return toBoolean(readSubRegister(OneByteRegister.REGISTER_FIFO_STATUS,1,1));
	}


	@Override
	public void flushReceiveFifo() {
		writeAndRead(Commands.CMD_FLUSH_RX.word);
	}

	@Override
	public int writeTransmitPayload(ByteBuffer data) {
		int dataSize;
		if (data.remaining() <= MAX_PAYLOAD_SIZE) {
			dataSize = data.remaining();
		} else {
			dataSize = MAX_PAYLOAD_SIZE;
		}

		byte[] buffer = new byte[dataSize+1];
		buffer[0] = Commands.CMD_W_TX_PAYLOAD.word;
		data.get(buffer, 1 ,dataSize);
		ByteBuffer txBuffer = ByteBuffer.wrap(buffer);
		ByteBuffer rxBuffer = ByteBuffer.allocate(MAX_PAYLOAD_SIZE + 1);
		writeAndRead(txBuffer, rxBuffer);
		return dataSize;
	}

	@Override
	public int availableReceivePayload() {

		ByteBuffer txBuffer = ByteBuffer.wrap(new byte[]{Commands.CMD_R_RX_PL_WID.word,Commands.CMD_NOP.word});
		ByteBuffer rxBuffer = ByteBuffer.allocate(2);
		writeAndRead(txBuffer, rxBuffer);
		return rxBuffer.get(1);
	}

	@Override
	public RfPipe readReceivePipe() {
		byte pipe = readSubRegister(OneByteRegister.REGISTER_STATUS,3,1);
		switch(pipe)
		{
		case 0 : return  RfPipe.PIPE_0;
		case 1 : return  RfPipe.PIPE_1;
		case 2 : return  RfPipe.PIPE_2;
		case 3 : return  RfPipe.PIPE_3;
		case 4 : return  RfPipe.PIPE_4;
		case 5 : return  RfPipe.PIPE_5;
		}
		throw new IllegalStateException(Byte.toString(pipe));
	}

	@Override
	public ByteBuffer readReceivePayload() {	
		int availableSize = availableReceivePayload();
		if (0 >= availableSize ||  availableSize > MAX_PAYLOAD_SIZE) {
			LOGGER.warn("invalid availableSize : 0 >= {} > {} => retry read available receive payload",
					availableSize, MAX_PAYLOAD_SIZE); 
			availableSize = availableReceivePayload();
		}
		if (0 >= availableSize ||  availableSize > MAX_PAYLOAD_SIZE) {
			LOGGER.error("invalid availableSize : 0 >= {} > {} => flush the RX FIFO!",
					availableSize,
					MAX_PAYLOAD_SIZE);
			flushReceiveFifo();
			return ByteBuffer.allocate(0);
		}

		ByteBuffer txBuffer = ByteBuffer.allocate(MAX_PAYLOAD_SIZE + 1);
		ByteBuffer rxBuffer = ByteBuffer.allocate(MAX_PAYLOAD_SIZE + 1);
		txBuffer.put(Commands.CMD_R_RX_PAYLOAD.word);
		for (int i = 0 ; i < availableSize; i++) {
			txBuffer.put(Commands.CMD_NOP.word);
		}
		txBuffer.flip();
		writeAndRead(txBuffer, rxBuffer);
		rxBuffer.get();
		return rxBuffer.slice();
	}

	@Override
	public boolean dynamicPayloadFeatureEnabled() {
		return toBoolean(readSubRegister(OneByteRegister.REGISTER_FEATURE, 1, 2));
	}

	@Override
	public void dynamicPayloadFeatureEnabled(boolean value) {
		writeSubRegister(OneByteRegister.REGISTER_FEATURE, toByte(value), 1, 2);
	}

	@Override
	public boolean dynamicPayloadEnabled(RfPipe pipe) {
		int offset = 0;

		switch (pipe) {
		case PIPE_0 : offset = 0; break;
		case PIPE_1 : offset = 1; break;
		case PIPE_2 : offset = 2; break;
		case PIPE_3 : offset = 3; break;
		case PIPE_4 : offset = 4; break;
		case PIPE_5 : offset = 5; break;
		default     : offset = 0; break;
		}
		return toBoolean(readSubRegister(OneByteRegister.REGISTER_DYNPD, 1, offset));
	}

	@Override
	public void dynamicPayloadEnabled(RfPipe iPipe, boolean iValue) {
		int offset = 0;

		switch (iPipe) {
		case PIPE_0 : offset = 0; break;
		case PIPE_1 : offset = 1; break;
		case PIPE_2 : offset = 2; break;
		case PIPE_3 : offset = 3; break;
		case PIPE_4 : offset = 4; break;
		case PIPE_5 : offset = 5; break;
		default     : offset = 0; break;
		}
		writeSubRegister(OneByteRegister.REGISTER_DYNPD, toByte(iValue), 1, offset);
	}	

	// --- internal details ----

	enum Masks {
		MASK_REGISTER_CMD (0x1F),
		MASK_RF_CH        (0x7F),
		MASK_ARC          (0x0F),
		MASK_ARD          (0xF0);

		public final byte mask;
		Masks(int mask){
			this.mask = (byte)mask;
		}
	};

	enum Commands {
		CMD_R_REGISTER    (0x00),
		CMD_W_REGISTER    (0x20),
		CMD_R_RX_PAYLOAD  (0x61),
		CMD_W_TX_PAYLOAD  (0xA0),
		CMD_FLUSH_TX      (0xE1),
		CMD_FLUSH_RX      (0xE2),
		CMD_R_RX_PL_WID   (0x60),
		CMD_NOP           (0xFF);

		public final byte word;
		Commands(int word){
			this.word = (byte)word;
		}	
	};

	enum OneByteRegister
	{
		REGISTER_CONFIG      (0x00),
		REGISTER_EN_AA       (0x01),
		REGISTER_EN_RXADDR   (0x02),
		REGISTER_SETUP_AW    (0x03),
		REGISTER_SETUP_RETR  (0x04),
		REGISTER_RF_CH       (0x05),
		REGISTER_RF_SETUP    (0x06),
		REGISTER_STATUS      (0x07),
		REGISTER_OBSERVE_TX  (0x08),
		REGISTER_RPD         (0x09),
		REGISTER_RX_ADDR_P2  (0x0C),
		REGISTER_RX_ADDR_P3  (0x0D),
		REGISTER_RX_ADDR_P4  (0x0E),
		REGISTER_RX_ADDR_P5  (0x0F),
		REGISTER_RX_PW_P0    (0x11),
		REGISTER_RX_PW_P1    (0x12),
		REGISTER_RX_PW_P2    (0x13),
		REGISTER_RX_PW_P3    (0x14),
		REGISTER_RX_PW_P4    (0x15),
		REGISTER_RX_PW_P5    (0x16),
		REGISTER_FIFO_STATUS (0x17),
		REGISTER_DYNPD       (0x1C),
		REGISTER_FEATURE     (0x1D);


		public final byte address;
		OneByteRegister(int address){
			this.address = (byte)address;
		}

	};

	enum FiveByteRegister
	{
		REGISTER_RX_ADDR_P0  (0x0A),
		REGISTER_RX_ADDR_P1  (0x0B),
		REGISTER_TX_ADDR     (0x10);

		public final byte address;
		FiveByteRegister(int address){
			this.address = (byte)address;
		}
	};

	// converters

	private boolean toBoolean(byte value) {
		return value != 0;
	}

	private byte toByte(boolean value) {
		return (byte)(value ? 0x01 : 0x00);
	}

	// mask

	private byte calculateMask(byte bitSize, byte offset) {
		byte mask = (byte)(((1 << bitSize) - 1) << offset);
		return mask;
	}

	// read 

	private ByteBuffer readRegister(FiveByteRegister register) {
		byte cmd = (byte)(Commands.CMD_R_REGISTER.word | (register.address & Masks.MASK_REGISTER_CMD.mask));

		ByteBuffer txBuffer = ByteBuffer.wrap(new byte[]{cmd,Commands.CMD_NOP.word,Commands.CMD_NOP.word,Commands.CMD_NOP.word,Commands.CMD_NOP.word,Commands.CMD_NOP.word});
		ByteBuffer rxBuffer = ByteBuffer.allocate(6);
		writeAndRead(txBuffer, rxBuffer);
		rxBuffer.rewind();
		rxBuffer.get();
		return rxBuffer.slice();
	}

	private byte readRegister(OneByteRegister register) {
		byte cmd = (byte)(Commands.CMD_R_REGISTER.word | (register.address & Masks.MASK_REGISTER_CMD.mask));

		ByteBuffer txBuffer = ByteBuffer.wrap(new byte[]{cmd,Commands.CMD_NOP.word});
		ByteBuffer rxBuffer = ByteBuffer.allocate(2);
		writeAndRead(txBuffer,rxBuffer);

		return rxBuffer.get(1);
	}

	private byte readSubRegister(OneByteRegister register, int bitSize, int offset) {
		return readSubRegister(register, (byte)bitSize, (byte)offset);
	}

	private byte readSubRegister(OneByteRegister register, byte bitSize, byte offset) {
		byte mask = calculateMask(bitSize,offset);
		byte value = readRegister(register);
		value &= mask;
		value >>= offset;
		return value;
	}

	// write
	private byte writeRegister(OneByteRegister register, int value) {
		return writeRegister(register, (byte)value);
	}

	private byte writeRegister(OneByteRegister register, byte value) {
		byte cmd = (byte)(Commands.CMD_W_REGISTER.word | (register.address & Masks.MASK_REGISTER_CMD.mask));

		ByteBuffer txBuffer = ByteBuffer.wrap(new byte[]{cmd,value});
		ByteBuffer rxBuffer = ByteBuffer.allocate(2);

		writeAndRead(txBuffer,rxBuffer);

		return rxBuffer.get(0);
	}

	private byte writeSubRegister(OneByteRegister register, int value, int bitSize, int offset) {
		return writeSubRegister(register, (byte)value, (byte)bitSize, (byte)offset);
	}

	private byte writeSubRegister(OneByteRegister register, byte value, byte bitSize, byte offset) {
		byte mask = calculateMask(bitSize,offset);
		byte registerValue = readRegister(register) ;
		registerValue = (byte)(registerValue & ~mask);
		value = (byte)(value << offset);
		registerValue = (byte)(registerValue | (value & mask));
		return writeRegister(register, registerValue);
	}

	private byte writeRegister(FiveByteRegister register, byte[] value) {
		byte cmd = (byte)(Commands.CMD_W_REGISTER.word | (register.address & Masks.MASK_REGISTER_CMD.mask));

		ByteBuffer txBuffer = ByteBuffer.wrap(new byte[]{cmd,value[0],value[1],value[2],value[3],value[4]});
		ByteBuffer rxBuffer = ByteBuffer.allocate(6);

		writeAndRead(txBuffer,rxBuffer);

		return rxBuffer.get(0);
	}

	// exception wrap

	private int writeAndRead(java.nio.ByteBuffer src, java.nio.ByteBuffer dst) {
		try {
			return mSpi.writeAndRead(src,dst);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private int writeAndRead(byte src) {
		try {
			ByteBuffer txBuffer = ByteBuffer.wrap(new byte[]{src});
			ByteBuffer rxBuffer = ByteBuffer.allocate(1);	
			mSpi.writeAndRead(txBuffer,rxBuffer);
			return rxBuffer.get(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
