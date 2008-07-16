package com.likbilen.protorpc.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.Service;
import com.likbilen.protorpc.stream.TwoWayStream;
import com.likbilen.protorpc.stream.standalone.StreamServer;

public class SocketServer extends Thread implements RpcCallback<Boolean>{
	protected ServerSocket ssc;
	protected boolean running;
	protected Service service;
	protected HashSet<StreamServer> streamServers=new HashSet<StreamServer>();
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
				ss.start();
			}
		}catch(IOException e){
			
			e.printStackTrace();
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
			for(StreamServer s:streamServers){
				s.shutdown(closeStreams);
			}
			
		}
	}
	@Override
	public void run(Boolean parameter) {
		shutdown(parameter);
	}
}
