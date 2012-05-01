package edu.rosehulman.android.directory.service;

import java.io.IOException;

public class NetworkException extends IOException {
	
	private static final long serialVersionUID = 7922314419709150932L;
	
	private Throwable mCause;
	
	public NetworkException(String s, Throwable cause) {
		super(s);
		mCause = cause;
	}

	@Override
	public Throwable getCause() {
		return mCause;
	}
}
