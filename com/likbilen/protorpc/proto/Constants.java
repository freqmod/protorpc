package com.likbilen.protorpc.proto;

public enum Constants {
	TYPE_INIT,
	TYPE_MESSAGE,
	TYPE_BIGMESSAGE,
	TYPE_RESPONSE,
	TYPE_BIGRESPONSE,
	TYPE_DISCONNECT,
	TYPE_UNKNOWN;
	public static int getCode(Constants c){
		switch (c) {
		case TYPE_INIT:
			return 1;
		case TYPE_MESSAGE:
			return 2;
		case TYPE_BIGMESSAGE:
			return 3;
		case TYPE_RESPONSE:
			return 4;
		case TYPE_BIGRESPONSE:
			return 5;
		case TYPE_DISCONNECT:
			return 6;
		default:
			return 255;
		}
	}
	public static Constants fromCode(int b){
		switch (b) {
		case 1:
			return TYPE_INIT;
		case 2:
			return TYPE_MESSAGE;
		case 3:
			return TYPE_MESSAGE;
		case 4:
			return TYPE_RESPONSE;
		case 5:
			return TYPE_BIGRESPONSE;
		case 6:
			return TYPE_DISCONNECT;
		default:
			return TYPE_UNKNOWN;
		}
	}


}
