package org.anyline.util;


public class HexFinder {
	public static String format(byte[] bt) {
		int line = 0;
		StringBuilder buf = new StringBuilder();
		for (byte d : bt) {
			if (line % 16 == 0)
				buf.append(String.format("%05x: ", line));
			buf.append(String.format("%02x ", d));
			line++;
			if (line % 16 == 0)
				buf.append("\n");
		}
		buf.append("\n");
		return buf.toString();
	}
}
