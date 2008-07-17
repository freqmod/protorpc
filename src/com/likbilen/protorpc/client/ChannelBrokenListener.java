package com.likbilen.protorpc.client;

import com.google.protobuf.RpcChannel;
/**
 * This interface represents a part that wants to know if the RpcChannel has been broken (i.e. shutdown, or failed)
 * @author Frederik
 *
 */

public interface ChannelBrokenListener {
	/**
	 * Report that the channel is broken
	 * @param b - the broken channel
	 */
	public void channelBroken(RpcChannel b);
}
