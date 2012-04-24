package edu.rosehulman.android.directory.service;

import java.io.IOException;

public class ServerException extends IOException {

	private static final long serialVersionUID = -3328692665307731964L;

	public ServerException(String s, Throwable cause) {
		super(s, cause);
	}
	
}
