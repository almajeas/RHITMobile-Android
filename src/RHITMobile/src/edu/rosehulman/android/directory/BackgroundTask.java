package edu.rosehulman.android.directory;

import android.os.AsyncTask;

public abstract class BackgroundTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	/**
	 * Called on the UI thread when the task has been cancelled
	 * but before the background thread has been terminated.  Useful
	 * for doing things such as closing dialogs
	 */
	protected abstract void onAbort();
	
}
