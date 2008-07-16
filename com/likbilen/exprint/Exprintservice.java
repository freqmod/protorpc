package com.likbilen.exprint;

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
