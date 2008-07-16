package com.likbilen.exprint;

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
import com.likbilen.protorpc.stream.standalone.StreamChannel;

public class SocketSupervisor {
	public static void main(String[] args) {
		int port=12360;
		try {
			SocketChannel.registerProtocolHandler();
			SocketServer srv = new SocketServer(new Exprintservice());
			srv.getServerSocket().bind(new InetSocketAddress(port));
			srv.start();
			setConfiguration("pbrpc://localhost:"+port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setConfiguration(String url) {
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
		chan.shutdown(false);
	}
}
