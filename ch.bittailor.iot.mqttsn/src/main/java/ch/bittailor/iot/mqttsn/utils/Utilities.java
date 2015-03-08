package ch.bittailor.iot.mqttsn.utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class Utilities {
	public final static String STRING_ENCODING = "UTF-8";
	
	
	public static String readString(ByteBuffer buffer) {
		byte[] raw = new byte[buffer.remaining()];
  	buffer.get(raw);
  	try {
			return new String(raw, Utilities.STRING_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void writeString(String string, ByteBuffer buffer) {
		try {
			buffer.put(string.getBytes(Utilities.STRING_ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
