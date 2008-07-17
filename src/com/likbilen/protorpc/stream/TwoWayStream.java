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
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.likbilen.protorpc.client.SimpleRpcController;
import com.likbilen.protorpc.proto.Constants;
import com.likbilen.protorpc.stream.session.SessionManager;
import com.likbilen.protorpc.stream.session.SessionRpcControllerImpl;
import com.likbilen.protorpc.tools.DataInputStream;
import com.likbilen.protorpc.tools.DataOutputStream;
import com.likbilen.protorpc.tools.ThreadTools;
import com.likbilen.util.Pair;

public class TwoWayStream extends Thread implements SessionManager,RpcChannel{
	/* protected OutputStream origStream; */
	protected DataInputStream in;
	protected DataOutputStream out;
	protected int timeout = 10000;
	protected Service service;
	protected boolean connected=false;
	protected Object session=null;
	protected int callnum=0;
	protected HashMap<Integer,Pair<RpcCallback<Message>,Message>> currentCalls=new HashMap<Integer, Pair<RpcCallback<Message>,Message>>();
	protected Lock streamlock=new ReentrantLock();
	protected RpcCallback<Boolean> shutdownCallback;
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
						if (Constants.fromCode(code) == Constants.TYPE_MESSAGE&&service!=null) {
							streamlock.lock();
							try{
								msgid = in.readUnsignedLittleEndianShort();
								method = service.getDescriptorForType().getMethods()
										.get(in.readUnsignedLittleEndianShort());
								tmpb = new byte[in.readUnsignedLittleEndianShort()];
								in.readFully(tmpb, timeout);
								request = service.getRequestPrototype(method)
										.newBuilderForType().mergeFrom(tmpb).build();
								controller = new SessionRpcControllerImpl(this);
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
			if(shutdownCallback!=null)
				shutdownCallback.run(false);
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
	}
	/**
	 * From RpcChannel
	 */
	public void callMethodThreaded(MethodDescriptor method, RpcController controller,
			Message request, Message responsePrototype,
			RpcCallback<Message> done) {
			try{
				streamlock.lock();
				out.write(Constants.getCode(Constants.TYPE_MESSAGE));
				out.writeUnsignedLittleEndianShort(callnum);
				out.writeUnsignedLittleEndianShort(method.getIndex());
				byte[] tmpb=request.toByteArray();
				out.writeUnsignedLittleEndianShort(tmpb.length);
				out.write(tmpb);
				out.flush();
				currentCalls.put(callnum, new Pair<RpcCallback<Message>, Message>(done,responsePrototype));
				callnum++;
			}catch(IOException e){
				controller.setFailed(e.getMessage());
			}finally{
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

}
