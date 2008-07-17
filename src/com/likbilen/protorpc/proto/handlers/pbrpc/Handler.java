package com.likbilen.protorpc.proto.handlers.pbrpc;

/* Copyright (C) 2008 Frederik M.J. Vestre

*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at

*   http://www.apache.org/licenses/LICENSE-2.0
*/

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
/**
 * Dummy url handler that does nothing but makes pbrpc-urls possible when registred
 * @author Frederik
 *
 */
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
