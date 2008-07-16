package com.likbilen.protorpc.stream.standalone;

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
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.likbilen.protorpc.proto.Constants;
import com.likbilen.protorpc.tools.DataInputStream;
import com.likbilen.protorpc.tools.DataOutputStream;
import com.likbilen.protorpc.tools.ThreadTools;
import com.likbilen.util.Pair;


public class StreamChannel extends Thread implements RpcChannel{
	/*protected OutputStream origStream;*/
	protected DataInputStream in;
	protected DataOutputStream out;
	protected int timeout=10000;
	protected int callnum=0;
	protected boolean connected=false;
	protected HashMap<Integer,Pair<RpcCallback<Message>,Message>> currentCalls=new HashMap<Integer, Pair<RpcCallback<Message>,Message>>();
	protected Lock streamlock=new ReentrantLock();
	public StreamChannel(InputStream in,OutputStream out){
		streamChannelConstructor(in, out, true);
	}

	public StreamChannel(InputStream in,OutputStream out,boolean autoconnect){
		streamChannelConstructor(in, out, autoconnect);
	}
	private void streamChannelConstructor(InputStream in,OutputStream out,boolean autoconnect){
		this.in=new DataInputStream(new BufferedInputStream(in));
		this.out=new DataOutputStream(new BufferedOutputStream(out));
		if(autoconnect)
			connect();
	}
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
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int default_timeout) {
		this.timeout = default_timeout;
	}
	public void run() {
		byte tmpb[];
		int code = 0, msgid, msglen;
		Pair<RpcCallback<Message>, Message> msg;
		try {
			//Should we make a read(timeout) to make shure disconnect works properly?
			while (connected) {
				code=in.read();
				if(Constants.fromCode(code) == Constants.TYPE_DISCONNECT ||code == -1){//disconnected by stream
					connected=false;
					break;
				}				
	
				try {
					// implement threaded asyncronous response
					if (Constants.fromCode(code) == Constants.TYPE_RESPONSE) {
						streamlock.lock();
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
					}
				} catch (TimeoutException e) {
					e.printStackTrace();
				}finally{
					streamlock.unlock();
				}
			}
		} catch (IOException e) {
			connected=false;//most likely a disconnect
		}
	}
	public boolean isConnected() {
		return connected;
	}
	public void connect() {
		if(!connected){
			connected=true;
			start();
		}
	}
	public void disconnect(boolean closeStreams){
		if(connected){
			connected=false;
			try{
				out.write(Constants.getCode(Constants.TYPE_DISCONNECT));
			}catch(IOException e){
				//don't handle
			}
			try{
				interrupt();
			}catch (SecurityException e) {
				//don't handle
			}
			if(closeStreams){
				try {
					in.close();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
