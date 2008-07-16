package com.likbilen.protorpc.proto.handlers.pbrpc;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler{
	public Handler(){
	}
	@Override
	protected URLConnection openConnection(URL u) throws IOException {
		return new PbrcpUrlConnection(u); 
	}

}
class PbrcpUrlConnection extends URLConnection{
	protected PbrcpUrlConnection(URL url) {
		super(url);
	}

	@Override
	public void connect() throws IOException {
	}
	
}
