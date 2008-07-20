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

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.likbilen.exprint.Exprintdata.Exprintconfig;
import com.likbilen.exprint.Exprintdata.Void;
import com.likbilen.protorpc.client.SimpleRpcController;
import com.likbilen.protorpc.stream.session.TwoWayRpcChannelController;

/**
 * Exprintservice is a service that responses to requests.
 * It can make calls back to the server if twoway is enabled.
 * @author Frederik
 *
 */
public class Exprintservice extends Exprintdata.Exprintserver implements RpcCallback<Exprintdata.ExprintserverSetConfigResponse>{
	String id;
	boolean twoway;
	public Exprintservice(String id,boolean twoway){
		this.id=id;
		this.twoway=twoway;
	}
	@Override
	public void setConfig(RpcController controller, Exprintconfig request,
			RpcCallback<Exprintdata.ExprintserverSetConfigResponse> done) {
		//Handle the request and make a response (se protcol buffers for more info)
		String ret =id+":"+request.getPrinter();
		for(Exprintdata.Exprintconfig.Exprint exp:request.getExprintsList()){
			ret+="|"+exp.getSubjectcode()+";"+exp.getParalell()+";"+(exp.getSolutions()?"jippi":"buu");
		}
		Exprintdata.ExprintserverSetConfigResponse.Builder resp=  Exprintdata.ExprintserverSetConfigResponse.newBuilder();
		resp.setResponsecode(ret);
		//Send the response
		done.run(resp.build());
		if(twoway)
			callBack(controller);//make a call back to the server if configured to do it
	}
	private void callBack(RpcController controller){
		//No way to call back witout a TwoWayRpcChannelController to get the RpcChannel back from
		if(!(controller instanceof TwoWayRpcChannelController))
			return; 
		//Get a TwoWayRpcControler from the controller 
		TwoWayRpcChannelController twc=(TwoWayRpcChannelController)controller;
		//Make a response to send back
		SimpleRpcController cont = new SimpleRpcController();
		Exprintdata.Exprintserver service = Exprintdata.Exprintserver
				.newStub(twc.getRpcChannel());//Get the channel back from the TwoWayRpcController
		Exprintdata.Exprintconfig.Builder reqbld = Exprintdata.Exprintconfig
				.newBuilder();
		reqbld.setPrinter("Masmasmas");
		Exprintdata.Exprintconfig.Exprint.Builder expb;
		expb = Exprintdata.Exprintconfig.Exprint.newBuilder();
		expb.setParalell("SadHour");
		expb.setSubjectcode("TMA4140");
		expb.setSolutions(true);
		reqbld.addExprints(expb);
		//Send a message back
		service.setConfig(cont, reqbld.build(), this);
	}
	public void run(Exprintdata.ExprintserverSetConfigResponse r){
		//Handle the response from the message sent back to the server
		System.out.println("Got response:"+r.getResponsecode());
	}
	public String toString(){
		return "Exprintservice@"+twoway+":"+id;
	}
	@Override
	public void getConfig(RpcController controller, Void request,
			RpcCallback<Exprintconfig> done) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void resetConfig(RpcController controller, Void request,
			RpcCallback<Void> done) {
		// TODO Auto-generated method stub
		
	}
}
