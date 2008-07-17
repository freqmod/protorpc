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
import java.util.concurrent.TimeoutException;

import com.likbilen.protorpc.client.ResponseWaiter;
import com.likbilen.protorpc.client.SimpleRpcController;
import com.likbilen.protorpc.socket.SocketChannel;
import com.likbilen.protorpc.socket.SocketServer;
import com.likbilen.protorpc.stream.TwoWayStream;

public class SocketSupervisor {
	public static void main(String[] args) {
		boolean server=true;
		boolean client=true;
		int port=12377;
		try {
			
			SocketChannel.registerProtocolHandler();
			if(server){
			SocketServer srv = new SocketServer(new Exprintservice());
			srv.getServerSocket().bind(new InetSocketAddress(port));
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
			Socket soc=new Socket(endpoint.getHost(),endpoint.getPort());
			chan = new SocketChannel(soc);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		SimpleRpcController cont = new SimpleRpcController();
		Exprintdata.Exprintserver service = Exprintdata.Exprintserver
				.newStub(chan);
		ResponseWaiter<Exprintdata.ExprintserverSetConfigResponse> waiter = new ResponseWaiter<Exprintdata.ExprintserverSetConfigResponse>();
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
		service.setConfig(cont, reqbld.build(), waiter);
		try {
			Exprintdata.ExprintserverSetConfigResponse resp = waiter.await();
			System.out.println(resp.getResponsecode());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		chan.shutdown(forceClose);
	}
}
