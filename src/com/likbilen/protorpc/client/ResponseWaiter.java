package com.likbilen.protorpc.client;

/* Copyright (C) 2008 Frederik M.J. Vestre

*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at

*   http://www.apache.org/licenses/LICENSE-2.0
*/

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.google.protobuf.RpcCallback;

public class ResponseWaiter<E> implements RpcCallback<E>{
	private boolean responded;
	//RpcCallback<E> cb;
	private ReentrantLock wl=new ReentrantLock();
	private Condition wc=wl.newCondition();
	private ReentrantLock al=new ReentrantLock();
	private E cbr;
	public ResponseWaiter(){
	}
	public E await()throws InterruptedException,TimeoutException{
		return await(0, null);
	}
	public E await(long timeout) throws InterruptedException,TimeoutException{
		return await(timeout, TimeUnit.MILLISECONDS);
	}
	public E await(long timeout,TimeUnit unit) throws InterruptedException,TimeoutException{
		wl.lock();
		al.lock();
		try{
			if(timeout==0)
				wc.await();
			else
				wc.await(timeout,unit);
			if(responded)
				return cbr;
			else
				throw new TimeoutException("Callback timed out");
		}finally{
			wl.unlock();
			al.unlock();
		}
		
	}
	@Override
	public void run(E param) {
		// TODO Auto-generated method stub
		wl.lock();
		try {
			cbr=param;
			responded=true;
			wc.signal();
		}finally{
			wl.unlock();
		}
	}
	public void reset(){
		if(al.tryLock()){
			try{
				cbr=null;
				responded=false;
			}finally{
				al.unlock();
			}
		}else{
			throw new RejectedExecutionException("The response is allready waiting on something");
		}
	}
}
