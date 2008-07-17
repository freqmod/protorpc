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

public class SocketServer extends Thread implements RpcCallback<Boolean>{
	protected ServerSocket ssc;
	protected boolean running;
	protected Service service;
	protected HashSet<TwoWayStream> streamServers=new HashSet<TwoWayStream>();
	public SocketServer(Service server) throws IOException{
		this.service=server;
		ssc=new ServerSocket();
	}
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
	public void start(){
		if(!running){
			running=true;
			super.start();
		}
	}
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
		shutdown(parameter);
	}
}
