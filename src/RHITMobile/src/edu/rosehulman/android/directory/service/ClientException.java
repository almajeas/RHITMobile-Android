package edu.rosehulman.android.directory.service;

import java.io.IOException;

public class ClientException extends IOException {

	private static final long serialVersionUID = 82453397641414670L;
	
	private int mStatusCode;

	public ClientException(int code, String s, Throwable cause) {
		super(s, cause);
		mStatusCode = code;
	}
	
	public int getStatusCode() {
		return mStatusCode;
	}
}
