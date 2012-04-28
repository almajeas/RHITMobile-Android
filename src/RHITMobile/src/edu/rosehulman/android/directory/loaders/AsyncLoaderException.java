package edu.rosehulman.android.directory.loaders;

public class AsyncLoaderException extends Exception {
	
	private static final long serialVersionUID = -5963682795448228471L;

	public AsyncLoaderException() {
		super();
	}
	
	public AsyncLoaderException(String errorMessage) {
		super(errorMessage);
	}
}
