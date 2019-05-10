package org.anyline.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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

	public static byte[] readFile(String file) throws IOException {
		InputStream is = new FileInputStream(file);
		int length = is.available();
		byte bt[] = new byte[length];
		is.read(bt);
		return bt;
	}

	public static void main(String[] agrs) throws IOException {
		byte[] bt = HexFinder.readFile("d:\\a.sbd");
		String hexData = HexFinder.format(bt);
		System.out.println(hexData);
	}
}
