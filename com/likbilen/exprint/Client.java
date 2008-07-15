package com.likbilen.exprint;

import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import com.likbilen.protorpc.StreamChannel;

public class Client implements RpcCallback<Message>{
	protected RpcChannel chan;
	public Client(InputStream in,OutputStream out){
		chan=new StreamChannel(in,out);
	}
	 public void setConfiguration() {
		// You provide classes MyRpcChannel and MyRpcController, which implement
		// the abstract interfaces protobuf::RpcChannel and
		// protobuf::RpcController.
		
		// controller = new MyRpcController;

		// The protocol compiler generates the SearchService class based on the
		// definition given above.
		// service = new SearchService::Stub(channel);

		// Set up the request.
		request.set_query("protocol buffers");

		// Execute the RPC.
		// service->Search(controller, request, response,
		// protobuf::NewCallback(&Done));
	}

	@Override
	public void run(Message parameter) {
		// TODO Auto-generated method stub

	}
}