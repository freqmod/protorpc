package com.likbilen.protorpc.client;

/* Copyright (C) 2008 Frederik M.J. Vestre

*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at

*   http://www.apache.org/licenses/LICENSE-2.0
*/

import java.util.HashSet;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

/**
 * Simple RPC controller that remembers all the things it is told. 
 * @author Frederik
 *
 */
public class SimpleRpcController implements RpcController,CallbackRpcController {
	private String reason;
	private boolean hasFailed;
	private boolean canceled;
	private HashSet<RpcCallback<Object>> cancelListeners = new HashSet<RpcCallback<Object>>();
	private HashSet<ControllerInfoListener> controllerInfoListeners= new HashSet<ControllerInfoListener>();
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
		controllerInfoListeners.clear();

	}

	@Override
	public void setFailed(String reason) {
		this.hasFailed=true;
		this.reason=reason;
		fireMethodFailed(reason);
	}

	@Override
	public void startCancel() {
		this.canceled=true;
		for(RpcCallback<Object> callback:cancelListeners){
			callback.run(null);
		}
		for(ControllerInfoListener l:controllerInfoListeners){
			l.methodCanceled();
		}
	}
	@Override
	public void addControllerInfoListener(ControllerInfoListener l) {
		controllerInfoListeners.add(l);
	}
	@Override
	public void removeControllerInfoListener(ControllerInfoListener l) {
		controllerInfoListeners.remove(l);
	}
	private void fireMethodFailed(String reason){
		for(ControllerInfoListener l:controllerInfoListeners){
			l.methodFailed(reason);
		}
	}


}
