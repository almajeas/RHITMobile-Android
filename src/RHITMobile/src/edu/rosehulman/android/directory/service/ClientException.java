package edu.rosehulman.android.directory.service;

import java.io.IOException;

public class ClientException extends IOException {

	private static final long serialVersionUID = 82453397641414670L;

	public ClientException(String s, Throwable cause) {
		super(s, cause);
	}

}
