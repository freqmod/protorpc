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

public class Exprintservice extends Exprintdata.Exprintserver{
	public Exprintservice(){
		
	}
	@Override
	public void setConfig(RpcController controller, Exprintconfig request,
			RpcCallback<Exprintdata.ExprintserverSetConfigResponse> done) {
		String ret =request.getPrinter();
		for(Exprintdata.Exprintconfig.Exprint exp:request.getExprintsList()){
			ret+="|"+exp.getSubjectcode()+";"+exp.getParalell()+";"+(exp.getSolutions()?"jippi":"buu");
		}
		Exprintdata.ExprintserverSetConfigResponse.Builder resp=  Exprintdata.ExprintserverSetConfigResponse.newBuilder();
		resp.setResponsecode(ret);
		done.run(resp.build());
	}
	
}
