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
/**
 * A channel that communicates over a socket. The socket must be connected before the channel is connected.
 * Usage example:
 * 		try {
 *			SocketChannel.registerProtocolHandler();
 *			String url= "pbrpc://localhost:port";
 *			URL endpoint=new URL(url);
 *			//Create a socket to be used by the socket channel
 *			Socket soc=new Socket(endpoint.getHost(),endpoint.getPort());//the socket may of course be made without an url
 *			//Create a socket channel
 *			chan = new SocketChannel(soc);
 *			//Set a service that may be called by the server to facillitate two way communication
 *			chan.setService(Service);
 *			
 *			//use channel, call messages etc.
 *			
 *			chan.shutdown(false);
 *		} catch (IOException e1) {
 *			e1.printStackTrace();
 *		}
 *
 * @author Frederik
 */
public class SocketChannel extends TwoWayStream{
	private static boolean handlerRegistered=false;
	static{//register url handler
		registerProtocolHandler();
	}
	private Socket soc;
	/**
	 * Create a SocketChannel based on soc, and connects the channel
	 * @param soc - a connected socket.
	 * @throws IOException
	 */
	public SocketChannel(Socket soc) throws IOException {
		super(soc.getInputStream(),soc.getOutputStream());
		this.soc=soc;
	}
	/**
	 * Create a SocketChannel based on soc, and connects the channel
	 * @param soc - should be connected if autoconnect is true
	 * @throws IOException
	 */	
	public SocketChannel(Socket soc,boolean autoconnect) throws IOException {
		super(soc.getInputStream(),soc.getOutputStream(),autoconnect);
		this.soc=soc;
	}
	/**
	 * Shut down this channel and close the socket it communicates over
	 */
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
	/**
	 * Register a pbrpc:// protocol handler to make it easier to create a socket from an url
	 */
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
