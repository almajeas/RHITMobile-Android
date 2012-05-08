package edu.rosehulman.android.directory.loaders;

public class InvalidAuthTokenException extends LoaderException {

	private static final long serialVersionUID = 7014998753155502194L;

	public InvalidAuthTokenException() {
		super("Invalid auth token");
	}
}
