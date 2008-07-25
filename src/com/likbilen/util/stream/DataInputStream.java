package com.likbilen.util.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.TimeoutException;

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
 * Data input stream that support little endian operations
 */
public class DataInputStream extends java.io.DataInputStream {
	public DataInputStream(InputStream in) {
		super(in);
	}

	public int readSignedLittleEndianInt() throws IOException {
		byte[] tmp = new byte[4];
		readFully(tmp, 0, 4);
		return (tmp[3]) << 24 | (tmp[2] & 0xff) << 16 | (tmp[1] & 0xff) << 8
				| (tmp[0] & 0xff);
	}

	public long readUnsignedLittleEndianInt() throws IOException {
		byte[] tmp = new byte[4];
		readFully(tmp, 0, 4);
		return ((tmp[3] & 0xff) << 24 |(tmp[2] & 0xff) << 16 |(tmp[1] & 0xff) << 8 | (tmp[0] & 0xff));
	}
	public long readUnsignedBigEndianInt() throws IOException {
		byte[] tmp = new byte[4];
		readFully(tmp, 0, 4);
		return ((tmp[0] & 0xff) << 24 |(tmp[1] & 0xff) << 16 |(tmp[2] & 0xff) << 8 | (tmp[3] & 0xff));
	}

	public int readUnsignedLittleEndianShort() throws IOException {
		byte[] tmp = new byte[2];
		readFully(tmp, 0, 2);
		return ((tmp[1] & 0xff) << 8 | (tmp[0] & 0xff));
	}

	public short readSignedLittleEndianShort() throws IOException {
		byte[] tmp = new byte[2];
		readFully(tmp, 0, 2);
		return (short) ((tmp[1] & 0xff) << 8 | (tmp[0] & 0xff));
	}
	public void readFully(byte[] b,long timeout) throws IOException, TimeoutException{
		long start=(new Date()).getTime();
		try{
			while(available()<b.length){
				Thread.sleep(timeout/10);
			}
			if(((new Date()).getTime()-start)>timeout)
				throw new TimeoutException();
		}catch(InterruptedException e){
				throw new TimeoutException(e.getMessage());
		}
		//It should be available now
		readFully(b);
	}
}