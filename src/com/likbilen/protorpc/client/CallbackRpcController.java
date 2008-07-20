package com.likbilen.protorpc.client;

public interface CallbackRpcController {
	public void addControllerInfoListener(ControllerInfoListener l);
	public void removeControllerInfoListener(ControllerInfoListener l);
}
