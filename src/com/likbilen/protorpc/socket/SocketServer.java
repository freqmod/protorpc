package com.likbilen.protorpc.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

import com.google.protobuf.Service;
import com.likbilen.protorpc.stream.TwoWayStream;
import com.likbilen.protorpc.stream.standalone.StreamServer;

public class SocketServer extends Thread {
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
				TwoWayStream ss=new TwoWayStream(client.getInputStream(),client.getOutputStream(),service);
				ss.start();
			}
		}catch(IOException e){
			running=false;
			e.printStackTrace();
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
}
