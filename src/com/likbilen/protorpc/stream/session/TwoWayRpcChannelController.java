package com.likbilen.protorpc.stream.session;

import com.google.protobuf.RpcChannel;
/**
 * Interface for ChannelControllers that may call back to the other side
 * @author freqmod
 *
 */
public interface TwoWayRpcChannelController  {
	public RpcChannel getRpcChannel();
}
