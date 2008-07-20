package com.likbilen.protorpc.stream;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.likbilen.util.Pair;

public interface SelfDescribingChannel {
	/**
	 * Requests the file descriptor which includes this service
	 * @param cb
	 * @param ctrl
	 */
	public void requestServiceDescriptor(RpcCallback<Pair<String,FileDescriptor>> cb,RpcController ctrl);
}
