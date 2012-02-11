package edu.rosehulman.android.directory;

import java.util.HashSet;
import java.util.Set;

import android.os.AsyncTask;

/**
 * Manage a set of tasks associated with an Activity.
 */
@SuppressWarnings("rawtypes")
public class TaskManager {
	
	private Set<AsyncTask> tasks;

	/**
	 * Creates a new TaskManager
	 * 
	 */
	public TaskManager() {
		tasks = new HashSet<AsyncTask>();
	}
	
	/**
	 * Add a task to be canceled if necessary
	 * 
	 * @param task The task to manage
	 */
	public void addTask(AsyncTask task) {
		tasks.add(task);
	}
	
	/**
	 * Cancel all tasks, allowing them to complete if they
	 * do not check the isCancelled() method
	 */
	public void abortTasks() {
		for (AsyncTask task : tasks) {
			task.cancel(true);

			if (task instanceof BackgroundTask) {
				((BackgroundTask)task).abort();
			}
		}
		tasks.clear();
	}

}
