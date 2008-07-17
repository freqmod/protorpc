package com.likbilen.protorpc.proto;

/*
* Copyright (C) 2008 Frederik M.J. Vestre
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY <copyright holder> ''AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL <copyright holder> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

public enum Constants {
	TYPE_INIT,
	TYPE_REQUEST,
	TYPE_BIGREQUEST,
	TYPE_RESPONSE,
	TYPE_BIGRESPONSE,
	TYPE_RESPONSE_CANCEL,
	TYPE_DISCONNECT,
	TYPE_UNKNOWN;
	public static int getCode(Constants c){
		switch (c) {
		case TYPE_INIT:
			return 1;
		case TYPE_REQUEST:
			return 2;
		case TYPE_BIGREQUEST:
			return 3;
		case TYPE_RESPONSE:
			return 4;
		case TYPE_BIGRESPONSE:
			return 5;
		case TYPE_RESPONSE_CANCEL:
			return 6;
		case TYPE_DISCONNECT:
			return 7;
		default:
			return 255;
		}
	}
	public static Constants fromCode(int b){
		switch (b) {
		case 1:
			return TYPE_INIT;
		case 2:
			return TYPE_REQUEST;
		case 3:
			return TYPE_REQUEST;
		case 4:
			return TYPE_RESPONSE;
		case 5:
			return TYPE_BIGRESPONSE;
		case 6:
			return TYPE_RESPONSE_CANCEL;
		case 7:
			return TYPE_DISCONNECT;
		default:
			return TYPE_UNKNOWN;
		}
	}


}
