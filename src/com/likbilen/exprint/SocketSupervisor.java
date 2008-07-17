package com.likbilen.exprint;

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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

import com.likbilen.protorpc.client.ResponseWaiter;
import com.likbilen.protorpc.client.SimpleRpcController;
import com.likbilen.protorpc.socket.SocketChannel;
import com.likbilen.protorpc.socket.SocketServer;
import com.likbilen.protorpc.stream.TwoWayStream;
/**
 * The SocketSupervsor is an example of a client and a server that communicates two ways with sockets.
 * The sample may be run on two server by changing the host in the url argument to setConfiguration and turn server off in one instance, and client of in the other.
 * @author Frederik 
 *
 */
public class SocketSupervisor {
	public static void main(String[] args) {
		boolean server=true;
		boolean client=true;
		int port=12387;
		try {
			//Register the dummy protcol handler so we can use pbrpc url's to parse urls.
			SocketChannel.registerProtocolHandler();
			if(server){
				//Create a new socket server that wraps the Exprintservice
				SocketServer srv = new SocketServer(new Exprintservice("serverservice",true));
				//Make the server shut down when a client disconnects, which makes the program close by itself
				srv.setShutDownOnDisconnect(true);
				//Bind the serversocket to a port
				srv.getServerSocket().bind(new InetSocketAddress(port));
				//Start the server
				srv.start();
			}
			if(client)
				setConfiguration("pbrpc://localhost:"+port,client&&server);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setConfiguration(String url,boolean forceClose) {
		TwoWayStream chan=null;
		try {
			URL endpoint=new URL(url);
			//Create a socket to be used by the socket channel
			Socket soc=new Socket(endpoint.getHost(),endpoint.getPort());
			//Create a socket channel
			chan = new SocketChannel(soc);
			//Set a service that may be called by the server to facillitate two way communication
			chan.setService(new Exprintservice("clientservice",false));
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		//Create a rpc controller to send with the rpc method
		SimpleRpcController cont = new SimpleRpcController();
		//Create a service based on the RPC channel
		Exprintdata.Exprintserver service = Exprintdata.Exprintserver
				.newStub(chan);
		//Create a responsewaiter that can block while waiting a response from the server
		ResponseWaiter<Exprintdata.ExprintserverSetConfigResponse> waiter = new ResponseWaiter<Exprintdata.ExprintserverSetConfigResponse>(chan);
		//Create and build the message that we send to the method, see the proto buffer documentation for more information
		Exprintdata.Exprintconfig.Builder reqbld = Exprintdata.Exprintconfig
				.newBuilder();
		reqbld.setPrinter("Morrohjørnet");
		Exprintdata.Exprintconfig.Exprint.Builder expb;
		expb = Exprintdata.Exprintconfig.Exprint.newBuilder();
		expb.setParalell("HappyHour");
		expb.setSubjectcode("TFY4125");
		expb.setSolutions(true);
		reqbld.addExprints(expb);
		expb = Exprintdata.Exprintconfig.Exprint.newBuilder();
		expb.setSubjectcode("TDT4100");
		reqbld.addExprints(expb);
		//Run RPC the method 
		service.setConfig(cont, reqbld.build(), waiter);
		try {
			//Wait for response, if the response is  null, the method is canceled or has failed and the rpc controller will report what happened. 
			Exprintdata.ExprintserverSetConfigResponse resp = waiter.await();
			//Clean up the waiter, free a pointer to the RpcChannel so it may be garbage collected.
			//Remember to reset the waiter if you want to use it again.
			waiter.cleanup();
			//Print out the response code from the response
			System.out.println(resp.getResponsecode());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//Shut down the connection
		chan.shutdown(forceClose);
	}
}
