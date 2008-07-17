package com.likbilen.protorpc.socket;

/* Copyright (C) 2008 Frederik M.J. Vestre

*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at

*   http://www.apache.org/licenses/LICENSE-2.0
*/

import java.io.IOException;
import java.net.Socket;

import com.likbilen.protorpc.stream.TwoWayStream;

public class SocketChannel extends TwoWayStream{
	private static boolean handlerRegistered=false;
	static{//register url handler
		registerProtocolHandler();
	}

	Socket soc;
	public SocketChannel(Socket soc) throws IOException {
		super(soc.getInputStream(),soc.getOutputStream());
		this.soc=soc;
	}
	public SocketChannel(Socket soc,boolean autoconnect) throws IOException {
		super(soc.getInputStream(),soc.getOutputStream(),autoconnect);
		this.soc=soc;
	}
	@Override
	public void shutdown(boolean closeSocket){
		super.shutdown(false);
		if(closeSocket){
			try {
				soc.close();
			} catch (IOException e) {
				//don't handle
			}
		}
	}
	public static void registerProtocolHandler(){
		if(!handlerRegistered){
			String handlpkgs=System.getProperty("java.protocol.handler.pkgs");
			if(handlpkgs==null)
				System.setProperty("java.protocol.handler.pkgs","com.likbilen.protorpc.proto.handlers");
			else
				System.setProperty("java.protocol.handler.pkgs",handlpkgs+"com.likbilen.protorpc.proto.handlers");
			handlerRegistered=true;
		}
	}

}
