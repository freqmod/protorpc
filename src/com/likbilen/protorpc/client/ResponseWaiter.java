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
import com.google.protobuf.RpcChannel;

public class ResponseWaiter<E> implements RpcCallback<E>,ChannelBrokenListener{
	private boolean responded;
	//RpcCallback<E> cb;
	private ReentrantLock wl=new ReentrantLock();
	private Condition wc=wl.newCondition();
	private ReentrantLock al=new ReentrantLock();
	private E cbr;
	private BreakableChannel bc;
	public ResponseWaiter(RpcChannel c){
		listen(c);
	}
	public E await()throws InterruptedException,TimeoutException{
		return await(0, null);
	}
	public E await(long timeout) throws InterruptedException,TimeoutException{
		return await(timeout, TimeUnit.MILLISECONDS);
	}
	public E await(long timeout,TimeUnit unit) throws InterruptedException,TimeoutException{
		if(responded)
			return cbr;
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
		wl.lock();
		try {
			cbr=param;
			responded=true;
			wc.signalAll();
		}finally{
			wl.unlock();
		}
	}
	public void reset(RpcChannel newchan){
		if(al.tryLock()){
			try{
				cbr=null;
				responded=false;
				cleanup();
				listen(newchan);
			}finally{
				al.unlock();
			}
		}else{
			throw new RejectedExecutionException("The response is allready waiting on something");
		}
	}
	public void cleanup(){
		if(bc!=null){
			bc.removeChannelBrokenListener(this);
		}
	}
	private void listen(RpcChannel c){
		if (c instanceof BreakableChannel) {
			BreakableChannel bc = (BreakableChannel) c;
			bc.addChannelBrokenListener(this);
		}
	}
	@Override
	public void channelBroken(RpcChannel b) {
		wl.lock();
		try {
			cbr=null;
			responded=true;
			wc.signalAll();
		}finally{
			wl.unlock();
		}
	}
}
