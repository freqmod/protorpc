package com.likbilen.protorpc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.likbilen.protorpc.proto.Constants;
import com.likbilen.protorpc.tools.DataInputStream;
import com.likbilen.protorpc.tools.DataOutputStream;
import com.likbilen.protorpc.tools.ThreadTools;


public class StreamChannel implements RpcChannel{
	/*protected OutputStream origStream;*/
	protected DataInputStream in;
	protected DataOutputStream out;
	protected int timeout=10000;
	protected int callnum=0;
	protected Lock streamlock=new ReentrantLock();
	public StreamChannel(InputStream in,OutputStream out){
		this.in=new DataInputStream(new BufferedInputStream(in));
		this.out=new DataOutputStream(new BufferedOutputStream(out));
	}
	@Override
	public void callMethod(MethodDescriptor method, RpcController controller,
			Message request, Message responsePrototype,
			RpcCallback<Message> done) {
		Class paramTypes[]={method.getClass(),controller.getClass(),request.getClass(),responsePrototype.getClass(),done.getClass()};
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
			boolean unlocked=false;
			try{
				streamlock.lock();
				out.write(Constants.getCode(Constants.TYPE_MESSAGE));
				out.writeUnsignedLittleEndianShort(callcount);
				out.writeUnsignedLittleEndianShort(method.getIndex());
				byte[] tmpb=request.toByteArray();
				out.writeUnsignedLittleEndianShort(tmpb.length);
				out.write(tmpb);
				out.flush();
				streamlock.unlock();
				unlocked=false;
				//implement threaded asyncronous response
				if(Constants.fromCode(in.read())==Constants.TYPE_RESPONSE){
					int msglen =in.readUnsignedLittleEndianShort();
					tmpb=new byte[msglen];
					in.readFully(tmpb,timeout);
					DynamicMessage response = DynamicMessage.parseFrom(responsePrototype.getDescriptorForType(), tmpb);
					done.run(response);
				}
			}catch(IOException e){
				controller.setFailed(e.getMessage());
			}catch(TimeoutException e){
				controller.setFailed(e.getMessage());
			}finally{
				if(!unlocked)
					streamlock.unlock();
			}
			
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int default_timeout) {
		this.timeout = default_timeout;
	}

}
