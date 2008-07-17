package com.likbilen.protorpc.client;
/**
 * This interface represents an RpcChannel that may be broken and tell about it. 
 * @author Frederik
 *
 */
public interface BreakableChannel {
	public void addChannelBrokenListener(ChannelBrokenListener l);
	public void removeChannelBrokenListener(ChannelBrokenListener l);
}
