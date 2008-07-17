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
			TwoWayStream srv=new TwoWayStream(sin,sout,new Exprintservice("",false));
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
		 ResponseWaiter<Exprintdata.ExprintserverSetConfigResponse> waiter = new ResponseWaiter<Exprintdata.ExprintserverSetConfigResponse>(chan);
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
			waiter.cleanup();
			System.out.println(resp.getResponsecode());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		chan.shutdown(false);
	}
}
