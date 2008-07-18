package com.likbilen.protorpc.stream.session;

import com.google.protobuf.RpcChannel;
/**
 * Interface for ChannelControllers that may call back to the other side
 * @author freqmod
 *
 */
public interface TwoWayRpcChannelController  {
	/**
	 * Returns the rpc channel that may be used to call methods on the other side
	 */
	public RpcChannel getRpcChannel();
}
