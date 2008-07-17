package com.likbilen.protorpc.client;

import com.google.protobuf.RpcChannel;

public interface ChannelBrokenListener {
	public void channelBroken(RpcChannel b);
}
