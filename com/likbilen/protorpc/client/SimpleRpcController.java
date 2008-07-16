package com.likbilen.protorpc.client;

import java.util.HashSet;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

public class SimpleRpcController implements RpcController {
	private String reason;
	private boolean hasFailed;
	private boolean canceled;
	HashSet<RpcCallback<Object>> cancelListeners=new HashSet<RpcCallback<Object>>();
	public SimpleRpcController(){
		reset();
	}
	@Override
	public String errorText() {
		// TODO Auto-generated method stub
		return reason;
	}

	@Override
	public boolean failed() {
		// TODO Auto-generated method stub
		return hasFailed;
	}

	@Override
	public boolean isCanceled() {
		// TODO Auto-generated method stub
		return canceled;
	}

	@Override
	public void notifyOnCancel(RpcCallback<Object> callback) {
		cancelListeners.add(callback);
	}

	@Override
	public void reset() {
		reason=null;
		hasFailed=false;
		canceled=false;

	}

	@Override
	public void setFailed(String reason) {
		this.reason=reason;
	}

	@Override
	public void startCancel() {
		this.canceled=true;
		for(RpcCallback<Object> callback:cancelListeners){
			callback.run(null);
		}
	}

}
