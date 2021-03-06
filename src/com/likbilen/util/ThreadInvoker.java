package com.likbilen.util;

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

import java.lang.reflect.Method;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Class that invokes methods in separate a thread
 * @author freqmod
 *
 */
class ThreadInvoker extends Thread{
	private Method m;
	private Object instance;
	private Object[] arguments;
	private Lock exlock=new ReentrantLock();
	private Exception ex;
	private boolean quiet;
	public ThreadInvoker(Method m,Object instance,Object[] arguments,boolean quiet){
		this.m=m;
		this.instance=instance;
		this.arguments=arguments;
		this.quiet=quiet;
	}
	public void run(){
		try {
			m.invoke(instance,arguments);
		} catch (Exception e) {
			if(!quiet){
				System.out.println("Invokelater failed:"+e.getMessage());
				e.printStackTrace();
			}
			exlock.lock();
			try{
				ex=e;
			}finally{
				exlock.unlock();
			}
		}
	}
	public Exception gotException(){
		try{
			exlock.lock();
			return ex;
		}finally{
			exlock.unlock();
		}
	}
}