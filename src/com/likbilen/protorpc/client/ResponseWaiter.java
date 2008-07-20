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
 * <p>
 * This class implements a way to make non blocking Rpc calls blocking. It may
 * be used like this:
 * </p>
 * 
 * <pre>
 * ResponseWaiter&lt;E&gt; waiter = new ResponseWaiter&lt;E&gt;(breakableRpcChannel);
 * SimpleRpcController cont=new SimpleRpcController();
 * service.callMethod(cont, request, waiter.getCallback());
 * try {
 * 	E response = waiter.await();
 * 	waiter.cleanup();
 * 
 * 	//handle response 
 * 
 * } catch (InterruptedException e) {
 * 	//handle exception 
 * } catch (TimeoutException e) {
 * 	//handle exception 
 * }
 * waiter.reset(rpcChannel);//if you want to use it again
 * </pre>
 * 
 * @author Frederik
 * 
 * @param <E>
 *            - the callback type for the Rpc call
 */
public class ResponseWaiter<E> {
	private boolean responded;
	// RpcCallback<E> cb;
	private ReentrantLock wl = new ReentrantLock();
	private Condition wc = wl.newCondition();
	private ReentrantLock al = new ReentrantLock();
	private E cbr;
	private BreakableChannel bc;
	private ResponseWaiterPrivate priv = new ResponseWaiterPrivate();
	private CallbackRpcController co;
	/**
	 * @param ch Channel that should notify the waiter if it is broken
	 * @param co Controller that should notify the waiter if the call 
	 * 			is canceled or fails 
	 */
	public ResponseWaiter(BreakableChannel ch, CallbackRpcController co) {
		listen(ch, co);
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
	 * Reset the waiter to make it wait for new responses
	 * 
	 * @param newchan
	 *            if null, or doesnt implement BreakableRpcChannel the waiter
	 *            will wait infinitly if the channel is broken and it doesn't
	 *            timeout
	 */
	public void reset(BreakableChannel newchan,CallbackRpcController newco) {
		if (al.tryLock()) {
			try {
				cbr = null;
				responded = false;
				cleanup();
				listen(newchan,newco);
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
			bc.removeChannelBrokenListener(priv);
		}
		if (co != null) {
			bc.removeChannelBrokenListener(priv);
		}

	}

	private void listen(BreakableChannel bc,CallbackRpcController co) {
		if (bc != null ) {
			this.bc=bc;
			bc.addChannelBrokenListener(priv);
		}
		if (co != null) {
			this.co=co;
			co.addControllerInfoListener(priv);
		}
	}

	public RpcCallback<E> getCallback() {
		return priv;
	}

	class ResponseWaiterPrivate implements RpcCallback<E>,
			ChannelBrokenListener,ControllerInfoListener {
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

		@Override
		public void methodCanceled() {
			wl.lock();
			try {
				cbr = null;
				responded = true;
				wc.signalAll();
			} finally {
				wl.unlock();
			}
		}

		@Override
		public void methodFailed(String reason) {
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
}
