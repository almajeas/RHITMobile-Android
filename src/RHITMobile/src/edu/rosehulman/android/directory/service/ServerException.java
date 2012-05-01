package edu.rosehulman.android.directory.service;

import java.io.IOException;

public class ServerException extends IOException {

	private static final long serialVersionUID = -2387674069699322969L;
	
	private Throwable mCause;
	
	public ServerException(String s, Throwable cause) {
		super(s);
		mCause = cause;
	}

	@Override
	public Throwable getCause() {
		return mCause;
	}
}
