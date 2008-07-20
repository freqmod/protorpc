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

import com.google.protobuf.Descriptors;
import com.likbilen.protorpc.client.ResponseWaiter;
import com.likbilen.protorpc.client.SimpleRpcController;
import com.likbilen.protorpc.stream.SelfDescribingChannel;
import com.likbilen.protorpc.stream.TwoWayStream;
import com.likbilen.util.Pair;
/**
 * The Supervsor is an example of a client and a server that communicates only with streams.
 * @author Frederik 
 *
 */

public class Supervisor {
	public static void main(String[] args) {
		try{
			//Set up streams to make the server and client communicate with eachother
			PipedInputStream cin=new PipedInputStream();
			PipedOutputStream cout= new PipedOutputStream();
			PipedInputStream sin= new PipedInputStream(cout);
			PipedOutputStream sout= new PipedOutputStream(cin);
			//Create a "server" that wraps a service
			TwoWayStream srv=new TwoWayStream(sin,sout,new Exprintservice("",false));
			setConfiguration(cin,cout);
			//Close the streams
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
		 //Create a "client" connection
		 TwoWayStream chan=new TwoWayStream(in,out);
		 //Create a rpc controller to send with the rpc method
		 SimpleRpcController cont=new SimpleRpcController();
		 //Create a service that wraps the client channel
		 Exprintdata.Exprintserver service = Exprintdata.Exprintserver.newStub(chan);
		 //Create a responsewaiter that can block while waiting a response from the server
		 ResponseWaiter<Exprintdata.ExprintserverSetConfigResponse> waiter = new ResponseWaiter<Exprintdata.ExprintserverSetConfigResponse>(chan.getBreakableChannel(),cont);
		 //Create and build the message that we send to the method, see the proto buffer documentation for more information
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
		 //Run the RPC method
		 service.setConfig(cont, reqbld.build(),waiter.getCallback());
		 try {
			//Wait for response, if the response is  null, the method is canceled or has failed and the rpc controller will report what happened.
			Exprintdata.ExprintserverSetConfigResponse resp =waiter.await();
			//Clean up the waiter, free a pointer to the RpcChannel so it may be garbage collected.
			//Remember to reset the waiter if you want to use it again.
			waiter.cleanup();
			if(resp==null){
				if(cont.failed()){
					System.out.println("Call failed:"+cont.errorText());
				}else if(cont.isCanceled()){
					System.out.println("Call canceled");
				}else{
					System.out.println("Channel broken");
				}
			}else{
				//Print out the response code from the response
				System.out.println(resp.getResponsecode());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//Shut down the connection
		if (chan instanceof SelfDescribingChannel) {
			SelfDescribingChannel slfchan = (SelfDescribingChannel) chan;
			cont.reset();
			ResponseWaiter<Pair<String,Descriptors.FileDescriptor>> descwaiter = new ResponseWaiter<Pair<String,Descriptors.FileDescriptor>>(chan.getBreakableChannel(),cont);			
			slfchan.requestServiceDescriptor(descwaiter.getCallback(),cont);
			try {
				Pair<String,Descriptors.FileDescriptor> dsc = descwaiter.await(500);
				if(dsc==null){
					System.out.println("Error"+cont.errorText());
				}else{
					System.out.println("Got filedescriptor:"+dsc+":"+dsc.last.findServiceByName(dsc.first));
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
		chan.shutdown(false);
	}
}
