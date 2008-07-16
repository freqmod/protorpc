package com.likbilen.protorpc.stream.session;

import com.likbilen.protorpc.client.SimpleRpcController;

public class SessionRpcControllerImpl extends SimpleRpcController implements SessionRpcController {
	private SessionManager sesman;
	public SessionRpcControllerImpl(SessionManager sesman) {
		super();
		this.sesman=sesman;
	}
	@Override
	public Object getSessionId() {
		return sesman.getSessionId();
	}
	@Override
	public void setSessionId(Object id) {
		sesman.setSessionId(id);
	}
}
