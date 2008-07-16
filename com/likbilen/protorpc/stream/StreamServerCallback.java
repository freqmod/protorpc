package com.likbilen.protorpc.stream;

import com.google.protobuf.RpcCallback;

class StreamServerCallback<E> implements RpcCallback<E>{
	private Integer id;
	private TwoWayStream srv;
	public StreamServerCallback(TwoWayStream srv,Integer id){
		this.id=id;
		this.srv=srv;
	}
	@Override
	public void run(E parameter) {
		srv.run(id,(Object)parameter);
	}
}
