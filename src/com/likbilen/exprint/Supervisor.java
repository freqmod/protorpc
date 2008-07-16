package com.likbilen.exprint;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeoutException;

import com.likbilen.protorpc.client.ResponseWaiter;
import com.likbilen.protorpc.client.SimpleRpcController;
import com.likbilen.protorpc.stream.TwoWayStream;

public class Supervisor {
	public static void main(String[] args) {
		try{
			PipedInputStream cin=new PipedInputStream();
			PipedOutputStream cout= new PipedOutputStream();
			PipedInputStream sin= new PipedInputStream(cout);
			PipedOutputStream sout= new PipedOutputStream(cin);
			TwoWayStream srv=new TwoWayStream(sin,sout,new Exprintservice());
			setConfiguration(cin,cout);
			sin.close();
			cin.close();
			cout.close();
			sout.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	 public static  void setConfiguration(InputStream in,OutputStream out) {
		 TwoWayStream chan=new TwoWayStream(in,out);
		 SimpleRpcController cont=new SimpleRpcController();
		 Exprintdata.Exprintserver service = Exprintdata.Exprintserver.newStub(chan);
		 ResponseWaiter<Exprintdata.ExprintserverSetConfigResponse> waiter = new ResponseWaiter<Exprintdata.ExprintserverSetConfigResponse>();
		 Exprintdata.Exprintconfig.Builder reqbld=Exprintdata.Exprintconfig.newBuilder();
		 reqbld.setPrinter("Morrohjørnet");
		 Exprintdata.Exprintconfig.Exprint.Builder expb;
		 expb=Exprintdata.Exprintconfig.Exprint.newBuilder();
		 expb.setParalell("HappyHour");
		 expb.setSubjectcode("TFY4125");
		 reqbld.addExprints(expb);
		 expb=Exprintdata.Exprintconfig.Exprint.newBuilder();
		 expb.setSubjectcode("TDT4100");
		 reqbld.addExprints(expb);
		 service.setConfig(cont, reqbld.build(),waiter);
		 try {
			Exprintdata.ExprintserverSetConfigResponse resp =waiter.await();
			System.out.println(resp.getResponsecode());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		chan.shutdown(false);
	}
}
