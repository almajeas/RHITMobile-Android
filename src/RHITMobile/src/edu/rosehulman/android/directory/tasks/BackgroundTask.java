package edu.rosehulman.android.directory.tasks;

import android.os.AsyncTask;

public abstract class BackgroundTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	private String mErrorMessage;
	
	/**
	 * Called on the UI thread when the task has been cancelled
	 * but before the background thread has been terminated.  Useful
	 * for doing things such as closing dialogs
	 */
	protected abstract void onAbort();
	
	protected void setError(String error) {
		mErrorMessage = error;
	}
	
	protected boolean hasError() {
		return mErrorMessage != null;
	}
	
	protected String getError() {
		return mErrorMessage;
	}
	
}
