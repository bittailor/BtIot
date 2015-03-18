package ch.bittailor.iot.core.utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class Utilities {
	public final static String STRING_ENCODING = "UTF-8";	
	final private static char[] s_hexArray = "0123456789ABCDEF".toCharArray();
	
	public static String getString(ByteBuffer buffer) {
		byte[] raw = new byte[buffer.remaining()];
  	buffer.get(raw);
  	try {
			return new String(raw, Utilities.STRING_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static int getBufferLengthForString(String string) {
		try {
			return string.getBytes(STRING_ENCODING).length;
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
	
	public static void delayInMilliseconds(long milliseconds) {
		try {
			TimeUnit.MILLISECONDS.sleep(milliseconds);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void delayInMicroseconds(long microseconds) {
		try {
			TimeUnit.MICROSECONDS.sleep(microseconds);
		}
			catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String toHexString(byte aByte)
    {
        final char[] hexChars = new char[2];
        hexChars[0] = s_hexArray[aByte >>> 4];
        hexChars[1] = s_hexArray[aByte & 0x0F];
        return new String(hexChars);
    }
	
	public static String toHexString(ByteBuffer bytes)
    {
        final int offset = bytes.position();
        final int size = bytes.remaining();
        final char[] hexChars = new char[size * 2];
        for (int i = 0; i < size; i++)
        {
            final int aByte = bytes.get(i+offset);
            hexChars[i * 2] = s_hexArray[(aByte & 0xF0) >>> 4];
	        hexChars[i * 2 + 1] = s_hexArray[aByte & 0x0F];
            
        }
        return new String(hexChars);
    }
	
	public static boolean toBoolean(byte value) {
		return value != 0;
	}
	
	public static boolean toBoolean(int value) {
		return value != 0;
	}

	public static byte toByte(boolean value) {
		return (byte)(value ? 0x01 : 0x00);
	}
	
}
