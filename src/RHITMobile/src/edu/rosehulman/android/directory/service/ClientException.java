package edu.rosehulman.android.directory.service;

import java.io.IOException;

public class ClientException extends IOException {

	private static final long serialVersionUID = 1985746485425213265L;
	
	private int mStatusCode;
	private Throwable mCause;

	public ClientException(int code, String s, Throwable cause) {
		super(s);
		mStatusCode = code;
		mCause = cause;
	}
	
	public int getStatusCode() {
		return mStatusCode;
	}

	@Override
	public Throwable getCause() {
		return mCause;
	}
}
