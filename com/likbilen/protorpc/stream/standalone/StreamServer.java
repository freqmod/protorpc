package com.likbilen.protorpc.stream.standalone;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.likbilen.protorpc.client.SimpleRpcController;
import com.likbilen.protorpc.proto.Constants;
import com.likbilen.protorpc.stream.session.SessionManager;
import com.likbilen.protorpc.stream.session.SessionRpcControllerImpl;
import com.likbilen.protorpc.tools.DataInputStream;
import com.likbilen.protorpc.tools.DataOutputStream;

public class StreamServer extends Thread implements SessionManager {
	/* protected OutputStream origStream; */
	protected DataInputStream in;
	protected DataOutputStream out;
	protected int timeout = 10000;
	protected Service service;
	protected boolean connected=false;
	protected Object session=null;
	public StreamServer(InputStream in, OutputStream out, Service service) {
		this.in = new DataInputStream(new BufferedInputStream(in));
		this.out = new DataOutputStream(new BufferedOutputStream(out));
		this.service = service;
	}

	public void run() {
		int code, callnum;
		byte[] tmpb;
		MethodDescriptor method;
		Message request;
		SimpleRpcController controller;
		try {
			
			while (connected) {
				code=in.read();
				if(Constants.fromCode(code) == Constants.TYPE_DISCONNECT ||code == -1){//disconnected by stream
					connected=false;
					break;
				}				
				try {
					if (Constants.fromCode(code) == Constants.TYPE_MESSAGE) {
						callnum = in.readUnsignedLittleEndianShort();
						method = service.getDescriptorForType().getMethods()
								.get(in.readUnsignedLittleEndianShort());
						tmpb = new byte[in.readUnsignedLittleEndianShort()];
						in.readFully(tmpb, timeout);
						request = service.getRequestPrototype(method)
								.newBuilderForType().mergeFrom(tmpb).build();
						controller = new SessionRpcControllerImpl(this);
						/*FIXME: commented out because of class conflict
						controller.notifyOnCancel(new StreamServerCallback<Object>(
										this, callnum));
						service.callMethod(method, controller, request,
										new StreamServerCallback<Message>(this,
												callnum));*/
					}
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			connected=false;//Most likely a disconnect
		} 
	}

	public void run(Integer id, Object param) {
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

		}
	}

	/**
	 * From RpcChannel
	 */
	public void callMethodThreaded(MethodDescriptor method,
			RpcController controller, Message request,
			Message responsePrototype, RpcCallback<Message> done) {
		try {
			out.write(Constants.getCode(Constants.TYPE_MESSAGE));
			byte[] tmpb = method.toProto().toByteArray();
			out.writeUnsignedLittleEndianShort(tmpb.length);
			out.write(tmpb);
			tmpb = request.toByteArray();
			out.writeUnsignedLittleEndianShort(tmpb.length);
			out.write(tmpb);
			out.flush();
			if (Constants.fromCode(in.read()) == Constants.TYPE_RESPONSE) {
				int msglen = in.readUnsignedLittleEndianShort();
				tmpb = new byte[msglen];
				in.readFully(tmpb, timeout);
				DynamicMessage response = DynamicMessage.parseFrom(
						responsePrototype.getDescriptorForType(), tmpb);
				done.run(response);
			}
		} catch (IOException e) {
			controller.setFailed(e.getMessage());
		} catch (TimeoutException e) {
			controller.setFailed(e.getMessage());
		}
	}
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
