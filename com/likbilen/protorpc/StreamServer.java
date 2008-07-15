package com.likbilen.protorpc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.TimeoutException;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.likbilen.protorpc.proto.Constants;
import com.likbilen.protorpc.tools.DataInputStream;
import com.likbilen.protorpc.tools.DataOutputStream;
import com.likbilen.protorpc.tools.ThreadTools;


public class StreamServer implements Runnable{
	/*protected OutputStream origStream;*/
	protected DataInputStream in;
	protected DataOutputStream out;
	protected int timeout=10000;
	protected Service service;
	public StreamServer(InputStream in,OutputStream out,Service service){
		this.in=new DataInputStream(new BufferedInputStream(in));
		this.out=new DataOutputStream(new BufferedOutputStream(out));
		this.service=service;
	}
	public void run(){
		int code;
		byte[] tmpb;
		MethodDescriptor method;
		DynamicMessage request;
		String methodName;
		try {
			while(Constants.fromCode(code=in.read())!=Constants.TYPE_DISCONNECT||code==-1){				
				method=service.getDescriptorForType().getMethods().get(in.readUnsignedLittleEndianShort());
				tmpb=new byte[in.readUnsignedLittleEndianShort()];
				in.readFully(tmpb, timeout);
				request= DynamicMessage.parseFrom(method.getInputType(), tmpb);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*public final void callMethod(
        com.google.protobuf.Descriptors.MethodDescriptor method,
        com.google.protobuf.RpcController controller,
        com.google.protobuf.Message request,
        com.google.protobuf.RpcCallback<
          com.google.protobuf.Message> done) {*/
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
			try{
				out.write(Constants.getCode(Constants.TYPE_MESSAGE));
				byte[] tmpb=method.toProto().toByteArray();
				out.writeUnsignedLittleEndianShort(tmpb.length);
				out.write(tmpb);
				tmpb=request.toByteArray();
				out.writeUnsignedLittleEndianShort(tmpb.length);
				out.write(tmpb);
				out.flush();
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
			}
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int default_timeout) {
		this.timeout = default_timeout;
	}

}
