package com.likbilen.protorpc.stream;

/* Copyright (C) 2008 Frederik M.J. Vestre

*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at

*   http://www.apache.org/licenses/LICENSE-2.0
*/

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import sun.misc.Signal;

import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.likbilen.protorpc.client.BreakableChannel;
import com.likbilen.protorpc.client.ChannelBrokenListener;
import com.likbilen.protorpc.client.SimpleRpcController;
import com.likbilen.protorpc.proto.Constants;
import com.likbilen.protorpc.stream.session.SessionManager;
import com.likbilen.protorpc.stream.session.TwoWayRpcController;
import com.likbilen.protorpc.tools.DataInputStream;
import com.likbilen.protorpc.tools.DataOutputStream;
import com.likbilen.protorpc.tools.ThreadTools;
import com.likbilen.util.Pair;

public class TwoWayStream extends Thread implements SessionManager,RpcChannel,BreakableChannel{
	/* protected OutputStream origStream; */
	protected DataInputStream in;
	protected DataOutputStream out;
	protected int timeout = 10000;
	protected Service service=null;
	protected boolean connected=false;
	protected Object session=null;
	protected int callnum=0;
	private boolean spawnCallers=false;
	protected int protoversion=-1;
	public final int maxSupportedProtocolVersion=1;
	public final int preferedProtocolVesion=maxSupportedProtocolVersion;
	protected HashMap<Integer,Pair<RpcCallback<Message>,Message>> currentCalls=new HashMap<Integer, Pair<RpcCallback<Message>,Message>>();
	protected Lock streamlock=new ReentrantLock();
	protected Condition initcond = streamlock.newCondition();
	protected RpcCallback<Boolean> shutdownCallback;
	protected HashSet<ChannelBrokenListener> channelBrokenListeners= new HashSet<ChannelBrokenListener>();
	public TwoWayStream(InputStream in,OutputStream out){
		streamChannelConstructor(in, out,null, true);
	}
	public TwoWayStream(InputStream in,OutputStream out,Service srv){
		streamChannelConstructor(in, out, srv,true);
	}
	public TwoWayStream(InputStream in,OutputStream out,Service srv,RpcCallback<Boolean> shutdownCallback){
		this.shutdownCallback=shutdownCallback;
		streamChannelConstructor(in, out, srv,true);
	}

	public TwoWayStream(InputStream in,OutputStream out,boolean autostart){
		streamChannelConstructor(in, out, null, autostart);
	}
	public TwoWayStream(InputStream in,OutputStream out,Service srv,boolean autostart){
		streamChannelConstructor(in, out, srv, autostart);
	}

	private void streamChannelConstructor(InputStream in,OutputStream out,Service srv,boolean autostart){
		this.in=new DataInputStream(new BufferedInputStream(in));
		this.out=new DataOutputStream(new BufferedOutputStream(out));
		this.service=srv;
		if(autostart)
			start();
	}

	public void run() {
		MethodDescriptor method;
		Message request;
		SimpleRpcController controller;
		byte tmpb[];
		int code = 0, msgid, msglen;
		Pair<RpcCallback<Message>, Message> msg;
		try{
			try {
				while (connected) {
					code=in.read();
					if(Constants.fromCode(code) == Constants.TYPE_DISCONNECT ||code == -1){//disconnected by stream
						connected=false;
						break;
					}	
					try {
						if(protoversion==-2){
							streamlock.lock();
							try{
								protoversion=in.read();
								initcond.signalAll();
							}finally{
								streamlock.unlock();
							}
						}
						if(protoversion==-1){
							if(Constants.fromCode(code) != Constants.TYPE_INIT){
								System.out.println("Invalid code:"+code);
								break;//invalid
							}
							streamlock.lock();
							try{
								protoversion=in.read();
								out.write(Constants.getCode(Constants.TYPE_INIT));
								protoversion=Math.min(preferedProtocolVesion,protoversion);
								out.write(protoversion);
								out.flush();
							}finally{
								streamlock.unlock();
							}
						}
						if (Constants.fromCode(code) == Constants.TYPE_REQUEST&&service!=null) {
							streamlock.lock();
							try{
								msgid = in.readUnsignedLittleEndianShort();
								method = service.getDescriptorForType().getMethods()
										.get(in.readUnsignedLittleEndianShort());
								tmpb = new byte[in.readUnsignedLittleEndianShort()];
								in.readFully(tmpb, timeout);
								request = service.getRequestPrototype(method)
										.newBuilderForType().mergeFrom(tmpb).build();
								controller = new TwoWayRpcController(this);
								controller.notifyOnCancel(new StreamServerCallback<Object>(
												this, msgid));
							}finally{
								streamlock.unlock();
							}
							service.callMethod(method, controller, request,
											new StreamServerCallback<Message>(this,
													msgid));
						}else if (Constants.fromCode(code) == Constants.TYPE_RESPONSE) {
							streamlock.lock();
							try{
								msgid = in.readUnsignedLittleEndianShort();
								msglen = in.readUnsignedLittleEndianShort();
								tmpb = new byte[msglen];
								in.readFully(tmpb, timeout);
								if (currentCalls.containsKey(new Integer(msgid))) {
									msg = currentCalls.get(new Integer(msgid));
									Message response = msg.last.newBuilderForType().mergeFrom(tmpb).build(); 
									msg.first.run(response);
									currentCalls.remove(new Integer(msgid));
								}
							}finally{
								streamlock.unlock();
							}
						}else{//empty buffer
							in.skip(in.available());
						}
					} catch (TimeoutException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				
			} 
		}finally{
			connected=false;
			streamlock.lock();
			try{
				initcond.signalAll();//make sure no caller is waiting for an init signal that will never come
			}finally{
				streamlock.unlock();
			}
			if(shutdownCallback!=null)
				shutdownCallback.run(false);
			fireChannelBroken();
		}
	}

	public void run(Integer id, Object param) {
		if(service==null)
			return;
		streamlock.lock();
		try {
			if (param instanceof Message) {// response
				Message parameter = (Message) param;
				out.write(Constants.getCode(Constants.TYPE_RESPONSE));
				out.writeUnsignedLittleEndianShort(id);
				byte[] tmpb = parameter.toByteArray();
				out.writeUnsignedLittleEndianShort(tmpb.length);
				out.write(tmpb);
				out.flush();
			} else if (param == null) {// canceled
				out.write(Constants.getCode(Constants.TYPE_RESPONSE_CANCEL));
				out.writeUnsignedLittleEndianShort(id);
				out.flush();
			}
		} catch (IOException e) {

		} finally{
			streamlock.unlock();
		}
	}


	/*-------------Rpc channel methods ---------*/
	@Override
	public void callMethod(MethodDescriptor method, RpcController controller,
			Message request, Message responsePrototype,
			RpcCallback<Message> done) {
		if(spawnCallers){
			Class<?> paramTypes[]={MethodDescriptor.class,RpcController.class,Message.class,Message.class,RpcCallback.class};
			Object params[]={method,controller,request,responsePrototype,done};
			try {
				ThreadTools.invokeInSeparateThread(getClass().getMethod("callMethodThreaded", paramTypes), this,params);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			callMethodThreaded(method, controller, request, responsePrototype, done);
		}
	}
	/**
	 * From RpcChannel
	 */
	public void callMethodThreaded(MethodDescriptor method,
			RpcController controller, Message request,
			Message responsePrototype, RpcCallback<Message> done) {
		streamlock.lock();
		try {
			if (protoversion == -1) {
				protoversion=-2;
				out.write(Constants.getCode(Constants.TYPE_INIT));
				out.write(preferedProtocolVesion);
				out.flush();
				initcond.await();
				if(protoversion==-2)
					return;
			}
			out.write(Constants.getCode(Constants.TYPE_REQUEST));
			out.writeUnsignedLittleEndianShort(callnum);
			out.writeUnsignedLittleEndianShort(method.getIndex());
			byte[] tmpb = request.toByteArray();
			out.writeUnsignedLittleEndianShort(tmpb.length);
			out.write(tmpb);
			out.flush();
			currentCalls.put(callnum, new Pair<RpcCallback<Message>, Message>(
					done, responsePrototype));
			callnum++;
		} catch (IOException e) {
			controller.setFailed(e.getMessage());
		} catch (InterruptedException e) {
			shutdown(false);
		} finally {
			streamlock.unlock();
		}

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*misc methods*/
	
	public void start(){
		if(!connected){
			connected=true;
			super.start();
		}
	}
	public void shutdown(boolean closeStreams){
		if(connected){
			try{
				out.write(Constants.getCode(Constants.TYPE_DISCONNECT));
				out.flush();
			}catch(IOException e){
				//don't handle
			}
			try{
				interrupt();
			}catch (SecurityException e) {
				//don't handle
			}
			connected=false;
			if(closeStreams){
				try {
					in.close();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(shutdownCallback!=null)
				shutdownCallback.run(false);
			fireChannelBroken();
		}
	}
	public boolean isRunning() {
		return connected;
	}
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int default_timeout) {
		this.timeout = default_timeout;
	}

	@Override
	public Object getSessionId() {
		return session;
	}

	@Override
	public void setSessionId(Object id) {
		this.session=id;
	}
	public Service getService() {
		return service;
	}
	public void setService(Service service) throws IllegalStateException{
		if(this.service!=null)
			throw new IllegalStateException("Service allready set to:"+service);
		this.service = service;
	}
	public void setSpawnCallers(boolean spawnCallers) {
		this.spawnCallers = spawnCallers;
	}
	public boolean doesSpawnCallers() {
		return spawnCallers;
	}
	@Override
	public void addChannelBrokenListener(ChannelBrokenListener l) {
		channelBrokenListeners.add(l);
	}
	@Override
	public void removeChannelBrokenListener(ChannelBrokenListener l) {
		channelBrokenListeners.remove(l);
	}
	protected void fireChannelBroken(){
		for(ChannelBrokenListener l:channelBrokenListeners){
			l.channelBroken(this);
		}
	}

}
