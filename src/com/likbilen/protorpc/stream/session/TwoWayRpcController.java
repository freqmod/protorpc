package com.likbilen.protorpc.stream.session;

/* Copyright (C) 2008 Frederik M.J. Vestre

*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at

*   http://www.apache.org/licenses/LICENSE-2.0
*/

import com.google.protobuf.RpcChannel;
import com.likbilen.protorpc.client.SimpleRpcController;
import com.likbilen.protorpc.stream.TwoWayStream;

public class TwoWayRpcController extends SimpleRpcController implements SessionRpcController,TwoWayRpcChannelController {
	private TwoWayStream str;
	public TwoWayRpcController(TwoWayStream str) {
		super();
		this.str=str;
	}
	@Override
	public Object getSessionId() {
		return str.getSessionId();
	}
	@Override
	public void setSessionId(Object id) {
		str.setSessionId(id);
	}
	public  TwoWayStream getTwoWayStream(){
		return str;
	}
	public RpcChannel getRpcChannel(){
		return str;
	}
}
