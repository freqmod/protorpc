package com.likbilen.protorpc.tools;

import java.lang.reflect.Method;

public class ThreadTools{
	public static void invokeInSeparateThread(Method m,Object instance,Object[] arguments){
		ThreadInvoker i= new ThreadInvoker(m,instance,arguments,false);
		i.run();
	}	
}
