package com.likbilen.protorpc.socket;

/* Copyright (C) 2008 Frederik M.J. Vestre

*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at

*   http://www.apache.org/licenses/LICENSE-2.0
*/

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.Service;
import com.likbilen.protorpc.stream.TwoWayStream;
/**
 * <p>A server that accepts connections from socket channel</p>
 * 
 * Usage example:
 * <pre>
 * SocketServer srv = new SocketServer(service);
 * //Make the server shut down when a client disconnects, which makes the program close by itself
 * srv.setShutDownOnDisconnect(true);
 * //Bind the serversocket to a port
 * srv.getServerSocket().bind(new InetSocketAddress(port));
 * //Start the server
 * srv.start();
 * </pre>
 * @author Frederik
 *
 */
public class SocketServer extends Thread implements RpcCallback<Boolean>{
	protected ServerSocket ssc;
	protected boolean running;
	protected Service service;
	protected HashSet<TwoWayStream> streamServers=new HashSet<TwoWayStream>();
	protected boolean shutDownOnDisconnect=false;
	/**
	 * Create a socket server, the ServerSocket available from getServerSocket() should be called before it is started.
	 * @param server
	 * @throws IOException
	 */
	public SocketServer(Service server) throws IOException{
		this.service=server;
		ssc=new ServerSocket();
	}
	/**
	 * Get the server socket that this SocketServer listens to
	 */
	public ServerSocket getServerSocket(){
		return ssc;
	}
	public void run(){
		Socket client;
		try{
			while(running){
				client=ssc.accept();
				TwoWayStream ss=new TwoWayStream(client.getInputStream(),client.getOutputStream(),service,this);
				streamServers.add(ss);
			}
		}catch(IOException e){
			//don't handle, most probably shut down
		}finally{
			running=false;
		}
	}
	/**
	 * Start this server socket. The server socket should be set up before calling this method.
	 */
	public void start(){
		if(!running){
			running=true;
			super.start();
		}
	}
	/**
	 * Shut down this server socket
	 * @param closeStreams - should the socket close the streams in this socket, leave to false if in doubt
	 */
	public void shutdown(boolean closeStreams){
		if(running){
			running=false;
			try{
				interrupt();
			}catch(SecurityException e){
				
			}
			for(TwoWayStream s:streamServers){
				s.shutdown(closeStreams);
			}
			try {
				ssc.close();
			} catch (IOException e) {
				//donm't handle
			}
		}
	}
	@Override
	public void run(Boolean parameter) {
		if(shutDownOnDisconnect)
			shutdown(parameter);
	}
	/**
	 * Should this socket shut down if a client disconnects from it?
	 */
	public boolean doShutDownOnDisconnect() {
		return shutDownOnDisconnect;
	}
	/**
	 * Should this socket shut down if a client disconnects from it?
	 */
	public void setShutDownOnDisconnect(boolean shutDownOnDisconnect) {
		this.shutDownOnDisconnect = shutDownOnDisconnect;
	}
}
