package com.likbilen.protorpc.stream;

/* Copyright (C) 2008 Frederik M.J. Vestre

*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at

*   http://www.apache.org/licenses/LICENSE-2.0
*/

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
