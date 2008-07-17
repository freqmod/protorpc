package com.likbilen.protorpc.client;

public interface BreakableChannel {
	public void addChannelBrokenListener(ChannelBrokenListener l);
	public void removeChannelBrokenListener(ChannelBrokenListener l);
}
