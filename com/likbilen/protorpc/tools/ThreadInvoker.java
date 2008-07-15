package com.likbilen.protorpc.tools;

import java.lang.reflect.Method;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
			ex=e;
		}finally{
			exlock.unlock();
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