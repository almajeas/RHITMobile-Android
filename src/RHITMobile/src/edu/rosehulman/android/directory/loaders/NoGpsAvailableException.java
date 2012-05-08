package edu.rosehulman.android.directory.loaders;

public class NoGpsAvailableException extends LoaderException {

	private static final long serialVersionUID = -1960746002868098143L;

	public NoGpsAvailableException() {
		super("No GPS hardware available");
	}

}
