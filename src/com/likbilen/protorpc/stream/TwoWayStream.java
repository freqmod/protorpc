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

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.likbilen.protorpc.MessageProto;
import com.likbilen.protorpc.client.BreakableChannel;
import com.likbilen.protorpc.client.ChannelBrokenListener;
import com.likbilen.protorpc.client.SimpleRpcController;
import com.likbilen.protorpc.stream.session.SessionManager;
import com.likbilen.util.Pair;
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

public class TwoWayStream extends Object implements SessionManager,RpcChannel,SelfDescribingChannel{
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
	private HashMap<Integer,Trio<RpcCallback<Message>,RpcController, Message>> currentCalls=new HashMap<Integer, Trio<RpcCallback<Message>,RpcController ,Message>>();
	private Lock streamlock=new ReentrantLock();
	private Condition initcond = streamlock.newCondition();
	private RpcCallback<Boolean> shutdownCallback;
	private HashSet<ChannelBrokenListener> channelBrokenListeners= new HashSet<ChannelBrokenListener>();
	HiddenMethods hiddenmethods=new HiddenMethods(this);
	private RpcController descriptorRequestController=null;
	private RpcCallback<Pair<String,FileDescriptor>> gotDescriptorCallback=null;
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
			MessageProto.Message.Builder reqbld = MessageProto.Message.newBuilder();
			reqbld.setType(MessageProto.Type.REQUEST);
			reqbld.setId(callnum);
			reqbld.setName(method.getName());
			reqbld.setBuffer(request.toByteString());
			writeMessage(reqbld.build());
			currentCalls.put(callnum, new Trio<RpcCallback<Message>,RpcController, Message>(
					done, controller,responsePrototype));
			callnum++;
		} catch (IOException e) {
			controller.setFailed(e.getMessage());
		} finally {
			streamlock.unlock();
		}

	}
	@Override
	public void requestServiceDescriptor(RpcCallback<Pair<String,FileDescriptor>> cb,RpcController ctrl){
		if(descriptorRequestController!=null&&gotDescriptorCallback==null){
			ctrl.setFailed("Can't make two descriptor requests at the same time");
			return;
		}
		streamlock.lock();
		try {
			MessageProto.Message.Builder reqbld = MessageProto.Message.newBuilder();
			reqbld.setType(MessageProto.Type.DESCRIPTOR_REQUEST);
			writeMessage(reqbld.build());
			descriptorRequestController=ctrl;
			gotDescriptorCallback=cb;
		} catch (IOException e) {
			ctrl.setFailed(e.getMessage());
		} finally {
			streamlock.unlock();
		}
		
	}
	private void writeMessage(Message m) throws IOException{
		out.writeUnsignedLittleEndianShort(m.getSerializedSize());
		out.write(m.toByteArray());
		out.flush();
	}
	private Message fillMessage(Message type,boolean timeout) throws IOException,TimeoutException{
		int msglen=in.readUnsignedLittleEndianShort();
		if(msglen==-1)
			return null;
		byte[] msgbuf=new byte[msglen];
		if(timeout)
			in.readFully(msgbuf,this.timeout);
		else
			in.readFully(msgbuf);
		return type.newBuilderForType().mergeFrom(msgbuf).build();
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
			streamlock.lock();
			try{
				MessageProto.Message.Builder reqbld = MessageProto.Message.newBuilder();
				reqbld.setType(MessageProto.Type.DISCONNECT);
				writeMessage(reqbld.build());
			}catch(IOException e){
				//don't handle
			}finally{
				streamlock.unlock();
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
	
	private MessageProto.DescriptorResponse generateDescriptorResponse(FileDescriptor parent,String serviceName){
		MessageProto.DescriptorResponse.Builder rspbld= MessageProto.DescriptorResponse.newBuilder();
		if(serviceName!=null){
			rspbld.setServiceName(serviceName);
		}
		rspbld.setDesc(parent.toProto().toByteString());
		for(FileDescriptor dep:parent.getDependencies()){
			rspbld.addDeps(generateDescriptorResponse(dep,null));
		}
		
		return rspbld.build();
	}
	private Descriptors.FileDescriptor parseDescriptorResponse(MessageProto.DescriptorResponse parent) throws InvalidProtocolBufferException, DescriptorValidationException{
		Descriptors.FileDescriptor[] deps= new Descriptors.FileDescriptor[parent.getDepsCount()];
		for(int i=0;i<parent.getDepsCount();i++){
			deps[i]=parseDescriptorResponse(parent.getDeps(i));
		}
		return Descriptors.FileDescriptor.buildFrom(FileDescriptorProto.parseFrom(parent.getDesc()), deps);
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
			Trio<RpcCallback<Message>,RpcController, Message> msg;
			MessageProto.Message inmsg=null;
			try{
				try {
					while (connected) {
						try {
							inmsg=(MessageProto.Message)fillMessage(MessageProto.Message.getDefaultInstance(),false);
						} catch (TimeoutException e) {//will never happen
						}
						if(inmsg==null||inmsg.getType()==MessageProto.Type.DISCONNECT){//disconnected by stream
							connected=false;
							break;
						}	
						if (inmsg.getType()==MessageProto.Type.REQUEST&&service!=null) {
							method=null;
							if(service==null){//send not implemented
								streamlock.lock();
								try{
									MessageProto.Message.Builder resp=MessageProto.Message.newBuilder();
									resp.setType(MessageProto.Type.RESPONSE_NOT_IMPLEMENTED);
									resp.setId(inmsg.getId());
									writeMessage(resp.build());
								}finally{
									streamlock.unlock();
								}
							} else{
								streamlock.lock();
								try{
									if((method=service.getDescriptorForType().findMethodByName(inmsg.getName()))==null){
										MessageProto.Message.Builder resp=MessageProto.Message.newBuilder();
										resp.setType(MessageProto.Type.RESPONSE_NOT_IMPLEMENTED);
										resp.setId(inmsg.getId());
										writeMessage(resp.build());
									}
								}finally{
									streamlock.unlock();
								}
								if(method!=null){
									request = service.getRequestPrototype(method).newBuilderForType().mergeFrom(inmsg.getBuffer()).build();
									controller = new TwoWayRpcController(encloser);
									controller.notifyOnCancel(new StreamServerCallback<Object>(
													this, inmsg.getId(),controller));
									service.callMethod(method, controller, request,
												new StreamServerCallback<Message>(this,
														inmsg.getId(),controller));
								}
							}
						}else if (inmsg.getType()==MessageProto.Type.DESCRIPTOR_REQUEST) {
								streamlock.lock();
								try{
									MessageProto.Message.Builder rspbld = MessageProto.Message.newBuilder();
									rspbld.setType(MessageProto.Type.DESCRIPTOR_RESPONSE);
									if(service!=null){
										rspbld.setBuffer(generateDescriptorResponse(service.getDescriptorForType().getFile(),service.getDescriptorForType().getName()).toByteString());
									}else{
										rspbld.setBuffer(ByteString.copyFrom(new byte[0]));
									}
									writeMessage(rspbld.build());
								}finally{
									streamlock.unlock();
								}
						}else if (inmsg.getType()==MessageProto.Type.RESPONSE) {
								if (currentCalls.containsKey(new Integer(inmsg.getId()))) {
									msg = currentCalls.get(new Integer(inmsg.getId()));
									Message response = msg.last.newBuilderForType().mergeFrom(inmsg.getBuffer()).build();
									msg.first.run(response);
									currentCalls.remove(new Integer(inmsg.getId()));
								}
						}else if (inmsg.getType()==MessageProto.Type.RESPONSE_CANCEL) {
								if (currentCalls.containsKey(new Integer(inmsg.getId()))) {
									msg = currentCalls.get(new Integer(inmsg.getId()));//get controller
									msg.middle.startCancel();
									currentCalls.remove(new Integer(inmsg.getId()));
								}
						}else if (inmsg.getType()==MessageProto.Type.RESPONSE_FAILED) {
							if (currentCalls.containsKey(new Integer(inmsg.getId()))) {
								msg = currentCalls.get(new Integer(inmsg.getId()));//get controller
								msg.middle.setFailed(inmsg.getBuffer().toStringUtf8());
								currentCalls.remove(new Integer(inmsg.getId()));
							}
						}else if (inmsg.getType()==MessageProto.Type.RESPONSE_NOT_IMPLEMENTED) {
								if (currentCalls.containsKey(new Integer(inmsg.getId()))) {
									msg = currentCalls.get(new Integer(inmsg.getId()));//get controller
									msg.middle.setFailed("Not implemented by peer");
									currentCalls.remove(new Integer(inmsg.getId()));
								}
						}else if (inmsg.getType()==MessageProto.Type.DESCRIPTOR_RESPONSE&&descriptorRequestController!=null&&gotDescriptorCallback!=null) {
							try{
								if(inmsg.getBuffer().isEmpty()){//signal not available
									descriptorRequestController.startCancel();
									gotDescriptorCallback.run(null);
								}else{
									try {
										MessageProto.DescriptorResponse topresp= MessageProto.DescriptorResponse.parseFrom(inmsg.getBuffer());
										Descriptors.FileDescriptor fldsc = parseDescriptorResponse(topresp);
										gotDescriptorCallback.run(new Pair<String, FileDescriptor>(topresp.getServiceName(),fldsc));
									} catch (DescriptorValidationException e) {//signal transport error
										descriptorRequestController.setFailed("Parsing error while parsing service descriptor"+e);
										gotDescriptorCallback.run(null);
									}
								}
							}finally{//clean up
								descriptorRequestController=null;
								gotDescriptorCallback=null;
							}
						}else{//empty buffer
							//in.skip(in.available());
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
		public void run(Integer id, Object param,RpcController ctrl) {
			if(service==null)
				return;
			streamlock.lock();
			try {
				if (param instanceof Message) {// response
					Message parameter = (Message) param;
					MessageProto.Message.Builder rspbld = MessageProto.Message.newBuilder();
					rspbld.setType(MessageProto.Type.RESPONSE);
					rspbld.setId(id);
					rspbld.setBuffer(parameter.toByteString());
					writeMessage(rspbld.build());
				} else if (param == null) {// canceled
					if(ctrl.failed()){
						MessageProto.Message.Builder rspbld = MessageProto.Message.newBuilder();
						rspbld.setType(MessageProto.Type.RESPONSE_CANCEL);
						rspbld.setId(id);
						writeMessage(rspbld.build());
					}else{
						MessageProto.Message.Builder rspbld = MessageProto.Message.newBuilder();
						rspbld.setType(MessageProto.Type.RESPONSE_FAILED);
						rspbld.setId(id);
						rspbld.setBuffer(ByteString.copyFromUtf8(ctrl.errorText()));
						writeMessage(rspbld.build());
					}
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
	public BreakableChannel getBreakableChannel(){
		return hiddenmethods;
	}
}
