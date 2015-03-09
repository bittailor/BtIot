package ch.bittailor.iot.mqttsn.utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class Utilities {
	public final static String STRING_ENCODING = "UTF-8";
	
	
	public static String getString(ByteBuffer buffer) {
		byte[] raw = new byte[buffer.remaining()];
  	buffer.get(raw);
  	try {
			return new String(raw, Utilities.STRING_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void putString(ByteBuffer buffer, String string) {
		try {
			buffer.put(string.getBytes(Utilities.STRING_ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static int getUnsignedShort (ByteBuffer buffer)
	{
		return (buffer.getShort() & 0xffff);
	}

	public static void putUnsignedShort (ByteBuffer buffer, int value)
	{
		buffer.putShort ((short)(value & 0xffff));
	}
	
	public static byte[] getBytes (ByteBuffer buffer)
	{
		byte[] byteArray = new byte[buffer.remaining()];
		buffer.get(byteArray);
		return byteArray;
	}

	
}
