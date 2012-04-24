package edu.rosehulman.android.directory.service;

import java.io.IOException;

public class NetworkException extends IOException {
	
	private static final long serialVersionUID = -8067406022902036650L;

	public NetworkException(String s, Throwable cause) {
		super(s, cause);
	}
}
