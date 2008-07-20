package com.likbilen.protorpc.client;


public interface ControllerInfoListener{
	public void methodCanceled();
	public void methodFailed(String reason);
}
