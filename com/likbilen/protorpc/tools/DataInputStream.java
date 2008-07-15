package com.likbilen.protorpc.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.TimeoutException;

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