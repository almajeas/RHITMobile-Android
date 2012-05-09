package edu.rosehulman.android.directory.tasks;

import android.os.AsyncTask;

/**
 * Provides a generic method of requesting input from the user,
 * validating it in a background thread, and potentially getting
 * more input from the user
 *
 * @param <Progress> The type of input from the suer
 * @param <Result> The type of output from this task
 */
public abstract class UITask<Progress, Result> {
	
	/**
	 * Start the process of getting input from the user.  This method should call
	 * either setInput or cancel to continue the operation
	 * 
	 * @param attempt How many times the user has failed to give valid input
	 */
	public abstract void getInput(int attempt);
	
	/**
	 * Process the input on a background thread.  If the data is valid, this method
	 * should call setResult.
	 *  
	 * @param data The data given by the user
	 */
	public abstract void processInput(Progress data);
	
	/**
	 * Called when the task is completed successfully
	 * 
	 * @param res The computed value
	 */
	public abstract void taskCompleted(Result res);
	
	private Progress value;
	private Object valueLock = new Object();
	
	private Result result;
	private boolean hasResult = false;
	
	private InnerTask task;
	
	/**
	 * Starts the UITask
	 */
	public void start() {
		assert(task == null);
		
		task = new InnerTask();
		task.execute();
	}
	
	/**
	 * Cancels the UITask
	 */
	public void cancel() {
		assert(task != null);
		
		task.cancel(true);
		task = null;
	}
	
	/**
	 * Sets the input to the given value
	 * 
	 * @param value The input value
	 */
	protected void setInput(Progress value) {
		this.value = value;
		
		synchronized(valueLock) {
			valueLock.notify();
		}
	}
	
	/**
	 * Sets the result and completes the task
	 * 
	 * @param res The result
	 */
	protected void setResult(Result res) {
		result = res;
		hasResult = true;
	}

	private class InnerTask extends AsyncTask<Void, Void, Void> {
		
		public int attempt = 0;

		@Override
		protected Void doInBackground(Void... params) {
			while (!hasResult) {
				publishProgress();
				
				synchronized(valueLock) {
					try {
						valueLock.wait();
					} catch (InterruptedException e) {
						return null;
					}
				}
				
				processInput(value);
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... status) {
			getInput(attempt);
			attempt++;
		}
		
		@Override
		protected void onCancelled() {
		}
		
		@Override
		protected void onPostExecute(Void res) {
			taskCompleted(result);
		}
	}
}
