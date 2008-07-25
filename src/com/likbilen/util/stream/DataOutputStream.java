package com.likbilen.util.stream;

import java.io.IOException;
import java.io.OutputStream;

/*
* Copyright (C) 2008 Frederik M.J. Vestre
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY <copyright holder> ''AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL <copyright holder> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
/**
 * Data output stream that support little endian operations
 */

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
	public void writeUnsignedLittleEndianInt(long v) throws IOException {
		byte[] tmp = new byte[4];
		tmp[0] = (byte) v;
		tmp[1] = (byte) (v >> 8);
		tmp[2] = (byte) (v >> 16);
		tmp[3] = (byte) (v >> 24);
		write(tmp, 0, 4);
	}
	public void writeUnsignedBigEndianInt(long v) throws IOException {
		byte[] tmp = new byte[4];
		tmp[3] = (byte) v;
		tmp[2] = (byte) (v >> 8);
		tmp[1] = (byte) (v >> 16);
		tmp[0] = (byte) (v >> 24);
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
