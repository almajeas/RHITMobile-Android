package edu.rosehulman.android.directory.loaders;

public class GpsDisabledException extends LoaderException {

	private static final long serialVersionUID = -5590987124018052900L;
	
	public GpsDisabledException() {
		super("GPS is disabled");
	}

}
