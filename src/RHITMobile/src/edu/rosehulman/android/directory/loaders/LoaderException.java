package edu.rosehulman.android.directory.loaders;

public class LoaderException extends Exception {
	
	private static final long serialVersionUID = -5963682795448228471L;

	public LoaderException() {
		super();
	}
	
	public LoaderException(String errorMessage) {
		super(errorMessage);
	}
}
