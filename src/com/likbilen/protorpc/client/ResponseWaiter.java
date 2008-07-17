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

/**
 * <p>This class implements a way to make non blocking Rpc calls blocking. It may
 * be used like this:</p>
 * <pre>
 * ResponseWaiter<E> waiter = new ResponseWaiter<E>(rpcChannel);
 * service.callMethod(controller, request,waiter);
 * try { 
 *  	E response = waiter.await(); 
 *  	waiter.cleanup();
 * 		
 * 		//handle response 
 * 
 * } catch (InterruptedException e) { 
 * 		//handle exception 
 * }
 * catch (TimeoutException e) {
 * 		 //handle exception 
 * }
 * waiter.reset(rpcChannel);//if you want to use it again
 * </pre>
 * @author Frederik
 * 
 * @param <E>
 *            - the callback type for the Rpc call
 */
public class ResponseWaiter<E> implements RpcCallback<E>, ChannelBrokenListener {
	private boolean responded;
	// RpcCallback<E> cb;
	private ReentrantLock wl = new ReentrantLock();
	private Condition wc = wl.newCondition();
	private ReentrantLock al = new ReentrantLock();
	private E cbr;
	private BreakableChannel bc;

	/**
	 * @param channel
	 *            if null, or doesnt implement BreakableRpcChannel the waiter
	 *            will wait infinitly if the channel is broken and it doesn't
	 *            timeout
	 */
	public ResponseWaiter(RpcChannel channel) {
		listen(channel);
	}

	/**
	 * Wait for the method to return
	 * 
	 * @return Response
	 * @throws InterruptedException
	 *             if the thread is interrupted
	 */
	public E await() throws InterruptedException {
		try {
			return await(0, null);
		} catch (TimeoutException e) {
			// Should never happen
			return null;
		}
	}

	/**
	 * Wait for the method to return
	 * 
	 * @param timeout
	 *            - the time in millisecounds before returning timeout exception
	 * @return Response
	 * @throws InterruptedException
	 *             if the thread is interrupted
	 * @throws TimeoutException
	 *             if the waiting timed out
	 */
	public E await(long timeout) throws InterruptedException, TimeoutException {
		return await(timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Wait for the method to return
	 * 
	 * @param timeout
	 *            - the time in TimeUnit before returning timeout exception
	 * @param unit
	 *            - the time unit the timeout is specified in
	 * @return Response
	 * @throws InterruptedException
	 *             if the thread is interrupted
	 * @throws TimeoutException
	 *             if the waiting timed out
	 */
	public E await(long timeout, TimeUnit unit) throws InterruptedException,
			TimeoutException {
		if (responded)
			return cbr;
		wl.lock();
		al.lock();
		try {
			if (timeout == 0)
				wc.await();
			else
				wc.await(timeout, unit);
			if (responded)
				return cbr;
			else
				throw new TimeoutException("Callback timed out");
		} finally {
			wl.unlock();
			al.unlock();
		}

	}

	/**
	 * Called by the callback to signalize that a response has arrived
	 */
	@Override
	public void run(E param) {
		wl.lock();
		try {
			cbr = param;
			responded = true;
			wc.signalAll();
		} finally {
			wl.unlock();
		}
	}

	/**
	 * Reset the waiter to make it wait for new responses
	 * 
	 * @param newchan
	 *            if null, or doesnt implement BreakableRpcChannel the waiter
	 *            will wait infinitly if the channel is broken and it doesn't
	 *            timeout
	 */
	public void reset(RpcChannel newchan) {
		if (al.tryLock()) {
			try {
				cbr = null;
				responded = false;
				cleanup();
				listen(newchan);
			} finally {
				al.unlock();
			}
		} else {
			throw new RejectedExecutionException(
					"The response is allready waiting on something");
		}
	}

	/**
	 * Clean up the waiter after use and remove the pointer to the channel
	 */
	public void cleanup() {
		if (bc != null) {
			bc.removeChannelBrokenListener(this);
		}
	}

	private void listen(RpcChannel c) {
		if (c != null && c instanceof BreakableChannel) {
			BreakableChannel bc = (BreakableChannel) c;
			bc.addChannelBrokenListener(this);
		}
	}

	/**
	 * Called by the (Breakable)RpcChannel to signalize that the channel is
	 * breaked. Stops the waiting and sets the result to null
	 */
	@Override
	public void channelBroken(RpcChannel b) {
		wl.lock();
		try {
			cbr = null;
			responded = true;
			wc.signalAll();
		} finally {
			wl.unlock();
		}
	}
}
