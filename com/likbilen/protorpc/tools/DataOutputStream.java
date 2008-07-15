package com.likbilen.protorpc.tools;

import java.io.IOException;
import java.io.OutputStream;


public class DataOutputStream extends java.io.DataOutputStream{
	public DataOutputStream(OutputStream out) {
		super(out);
	}

	public void writeSignedLittleEndianInt(int v) throws IOException {
		byte[] tmp = new byte[4];
		tmp[0] = (byte) v;
		tmp[1] = (byte) (v >> 8);
		tmp[2] = (byte) (v >> 16);
		tmp[3] = (byte) (v >> 24);
		write(tmp, 0, 4);
	}

	public void writeSignedLittleEndianShort(short v) throws IOException {
		writeUnsignedLittleEndianShort(v);
	}

	public void writeUnsignedLittleEndianShort(int v) throws IOException {
		byte[] tmp = new byte[2];
		tmp[0] = (byte) v;
		tmp[1] = (byte) (v >> 8);
		write(tmp, 0, 2);
	}
}
