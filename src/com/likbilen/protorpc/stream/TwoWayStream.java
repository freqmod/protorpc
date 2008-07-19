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
import com.likbilen.util.ThreadTools;
import com.likbilen.util.Trio;
import com.likbilen.util.stream.DataInputStream;
import com.likbilen.util.stream.DataOutputStream;
/**
 * <p>The main class. This class implements an rpc channel and a server, that communicates over IO streams.</p>
 * <h4>Server usage example:</h4>
 * <pre>TwoWayStream srv=new TwoWayStream(in,out,service);</pre>
 * where in and out are valid opened streams.
 * 
 * <p>To shutdown server:</p>
 * <pre>chan.shutdown(false);</pre>
 * O
 * <h4>Client usage example:</h4>
 * <pre>
 * TwoWayStream chan=new TwoWayStream(in,out);
 * // call methods by the RpcChannel interface
 * chan.shutdown(false);
 * </pre>
 * <p>The channel may be used as a client and server at the same time (even when it is initialized with a service)</p>
 *
 * @author Frederik
 *
 */

public class TwoWayStream extends Object implements SessionManager,RpcChannel{
	/* protected OutputStream origStream; */
	protected DataInputStream in;
	protected DataOutputStream out;
	private int timeout = 10000;
	private Service service=null;
	private boolean connected=false;
	private Object session=null;
	private int callnum=0;
	private boolean spawnCallers=false;
	private int protoversion=-1;
	/**
	 * The highest protocol version this stream supports
	 */
	public final int maxSupportedProtocolVersion=1;
	private final int preferedProtocolVesion=maxSupportedProtocolVersion;
	private HashMap<Integer,Trio<RpcCallback<Message>,RpcController, Message>> currentCalls=new HashMap<Integer, Trio<RpcCallback<Message>,RpcController ,Message>>();
	private Lock streamlock=new ReentrantLock();
	private Condition initcond = streamlock.newCondition();
	private RpcCallback<Boolean> shutdownCallback;
	private HashSet<ChannelBrokenListener> channelBrokenListeners= new HashSet<ChannelBrokenListener>();
	HiddenMethods hiddenmethods=new HiddenMethods(this);
	
	/*Constructors*/
	
	/**
	 * Call TwoWayStream(in,out,null,true)
	 * @see  #TwoWayStream(InputStream,OutputStream,Service,boolean)
	 * @param in
	 * @param out
	 */
	public TwoWayStream(InputStream in,OutputStream out){
		streamChannelConstructor(in, out,null, true);
	}
	/**
	 * Call TwoWayStream(in,out,srv,true)
	 * @see  #TwoWayStream(InputStream,OutputStream,Service,boolean)
	 * @param in
	 * @param out
	 */
	public TwoWayStream(InputStream in,OutputStream out,Service srv){
		streamChannelConstructor(in, out, srv,true);
	}
	/**
	 * Call TwoWayStream(in,out,srv,true)
	 * and set a callback that is ran when the stream shuts down 
	 * @see  #TwoWayStream(InputStream,OutputStream,Service,boolean)
	 * @param in
	 * @param out
	 */
	public TwoWayStream(InputStream in,OutputStream out,Service srv,RpcCallback<Boolean> shutdownCallback){
		this.shutdownCallback=shutdownCallback;
		streamChannelConstructor(in, out, srv,true);
	}
	/**
	 * Call TwoWayStream(in,out,null,autostart)
	 * @see  #TwoWayStream(InputStream,OutputStream,Service,boolean)
	 * @param in
	 * @param out
	 */
	public TwoWayStream(InputStream in,OutputStream out,boolean autostart){
		streamChannelConstructor(in, out, null, autostart);
	}
	/**
	 * Create a TwoStreamChannel
	 * @param in - the stream to listen on, should be open
	 * @param out - the stream to write to, should be open
	 * @param srv - the server to wrap, or null if the channel should only be a client 
	 * @param autostart - call start()
	 * @see #start()
	 */
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




	/*-------------Rpc channel methods ---------*/
	/**
	 * Specified by RpcChannel
	 */
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
	private void callMethodThreaded(MethodDescriptor method,
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
			
			currentCalls.put(callnum, new Trio<RpcCallback<Message>,RpcController, Message>(
					done, controller,responsePrototype));
			callnum++;
		} catch (IOException e) {
			controller.setFailed(e.getMessage());
		} catch (InterruptedException e) {
			shutdown(false);
		} finally {
			streamlock.unlock();
		}

	}
	/*----------------------Connection methods ---------------------*/
	
	/**
	 * Start the stream, must be called before calling callMethod
	 * @see #callMethod(MethodDescriptor, RpcController, Message, Message, RpcCallback)
	 */
	public void start(){
		if(!connected){
			connected=true;
			hiddenmethods.start();
		}
	}
	/**
	 * Shut down the server
	 * @param closeStreams - close the iostreams?
	 */
	public void shutdown(boolean closeStreams){
		if(connected){
			try{
				out.write(Constants.getCode(Constants.TYPE_DISCONNECT));
				out.flush();
			}catch(IOException e){
				//don't handle
			}
			try{
				hiddenmethods.interrupt();
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
			hiddenmethods.fireChannelBroken();
		}
	}
	


	class HiddenMethods extends Thread implements BreakableChannel{
		private TwoWayStream encloser;
		public HiddenMethods(TwoWayStream encloser){
			this.encloser=encloser;
		}
		@Override
		public void addChannelBrokenListener(ChannelBrokenListener l) {
			channelBrokenListeners.add(l);
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void removeChannelBrokenListener(ChannelBrokenListener l) {
			channelBrokenListeners.remove(l);
		}
		protected void fireChannelBroken(){
			for(ChannelBrokenListener l:channelBrokenListeners){
				l.channelBroken(encloser);
			}
		}
		/**
		 * Private: Called by thread, may dissappear at any moment
		 */
		@Override
		public void run() {
			MethodDescriptor method=null;
			Message request;
			SimpleRpcController controller;
			byte tmpb[]=new byte[0];
			int code = 0, msgid, msglen,metid;
			Trio<RpcCallback<Message>,RpcController, Message> msg;
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
									break;//invalid code in this state
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
								metid=-1;
								try{
									msgid = in.readUnsignedLittleEndianShort();
									metid = in.readUnsignedLittleEndianShort();
									if(metid<service.getDescriptorForType().getMethods().size()){
										method=service.getDescriptorForType().getMethods().get(metid);
										tmpb = new byte[in.readUnsignedLittleEndianShort()];
										in.readFully(tmpb, timeout);
									}else{
										out.write(Constants.getCode(Constants.TYPE_RESPONSE_NOT_IMPLEMENTED));
										out.writeUnsignedLittleEndianShort(metid);
									}
								}finally{
									streamlock.unlock();
								}
								if(metid!=-1&&metid<service.getDescriptorForType().getMethods().size()){
									request = service.getRequestPrototype(method)
									.newBuilderForType().mergeFrom(tmpb).build();
									controller = new TwoWayRpcController(encloser);
									controller.notifyOnCancel(new StreamServerCallback<Object>(
													this, msgid));
									service.callMethod(method, controller, request,
												new StreamServerCallback<Message>(this,
														msgid));
								}
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
							}else if (Constants.fromCode(code) == Constants.TYPE_RESPONSE_CANCEL) {
								streamlock.lock();
								try{
									msgid = in.readUnsignedLittleEndianShort();
									if (currentCalls.containsKey(new Integer(msgid))) {
										msg = currentCalls.get(new Integer(msgid));//get controller
										msg.middle.startCancel();
										currentCalls.remove(new Integer(msgid));
									}
								}finally{
									streamlock.unlock();
								}
							}else if (Constants.fromCode(code) == Constants.TYPE_RESPONSE_NOT_IMPLEMENTED) {
								streamlock.lock();
								try{
									msgid = in.readUnsignedLittleEndianShort();
									if (currentCalls.containsKey(new Integer(msgid))) {
										msg = currentCalls.get(new Integer(msgid));//get controller
										msg.middle.setFailed("Not implemented by peer");
										currentCalls.remove(new Integer(msgid));
									}
								}finally{
									streamlock.unlock();
								}
							}else{//empty buffer
								in.skip(in.available());
							}
						} catch (TimeoutException e) {
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
	
		/**
		 * Private: Called to signal that a response is recieved, may dissappear at any moment
		 */
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
	}
	/*----------------Getters & setters-------------------*/
	
	/**
	 * Is the server running?
	 * @return true if the server is running, else false
	 */
	public boolean isRunning() {
		return connected;
	}
	/**
	 * The timeout (in milliseconds) when reading buffers from the stream
	 * @return timeout
	 */
	public int getTimeout() {
		return timeout;
	}
	/**
	 * The timeout (in milliseconds) when reading buffers from the stream
	 * @param timeout
	 */

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getSessionId() {
		return session;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSessionId(Object id) {
		this.session=id;
	}
	/**
	 * Get the service this channel wraps
	 * @return - the service this channel wraps, or null if none
	 */
	public Service getService() {
		return service;
	}
	/**
	 * Get the service this channel wraps
	 * @param service - the service this channel should wrap
	 * @throws IllegalStateException - if the service has been set alleready
	 */
	public void setService(Service service) throws IllegalStateException{
		if(this.service!=null)
			throw new IllegalStateException("Service allready set to:"+service);
		this.service = service;
	}
	/**
	 * Should the stream spawn callMethod's in separate threads, default no.
	 * @see #callMethod(MethodDescriptor, RpcController, Message, Message, RpcCallback)

	 */
	public void setSpawnCallers(boolean spawnCallers) {
		this.spawnCallers = spawnCallers;
	}
	/**
	 * Should the stream spawn callMethod's in separate threads, default no. 
	 * @see #callMethod(MethodDescriptor, RpcController, Message, Message, RpcCallback)

	 */
	public boolean doesSpawnCallers() {
		return spawnCallers;
	}
	/**
	 * Returns the version of the protocol that is currently used by the stream
	 */
	public int getCurrentProtocolVersion() {
		return protoversion;
	}
}
