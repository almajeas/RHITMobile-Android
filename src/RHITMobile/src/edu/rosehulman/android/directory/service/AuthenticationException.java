package edu.rosehulman.android.directory.service;

public class AuthenticationException extends ClientException {

	private static final long serialVersionUID = 9000770976450108448L;

	public AuthenticationException(int code, String s, Throwable cause) {
		super(code, s, cause);
	}

}
