package edu.rosehulman.android.directory.util;

import java.util.LinkedList;
import java.util.List;

/**
 * Provides a queue of tasks that are executed in the order that they are added
 */
public class TaskQueue {
	
	/**
	 * A runnable task that can be inserted into the task queue
	 */
	public interface Task {
		
		/**
		 * Called when the task should perform its main operation.  The task is
		 * free to perform operations on the TaskQueue, including adding new tasks
		 * 
		 * @param queue The TaskQueue the task is running from
		 */
		void run(TaskQueue queue);
	}
	
	private List<Task> q;
	private Task currentTask;
	
	/**
	 * Creates a new TaskQueue
	 */
	public TaskQueue() {
		q = new LinkedList<Task>();
	}
	
	/**
	 * Adds a task to the queue to be executed
	 * 
	 * @param task The task to be executed
	 */
	public void addTask(Task task) {
		synchronized (q) {
			q.add(task);
		}
	}
	
	/**
	 * Moves the given task to the start of the queue
	 * 
	 * @param task The task to run next
	 * @return True if the task was found and moved; False otherwise
	 */
	public boolean prioritizeTask(Task task) {
		synchronized (q) {
			
			if (task.equals(currentTask))
				return true;
			
			if (q.remove(task)) {
				q.add(0, task);
				return true;	
			}
			return false;
			
		}
	}
	
	/**
	 * Determines if there are any remaining tasks to run
	 * 
	 * @return True if the queue is empty
	 */
	public boolean isEmpty() {
		synchronized (q) {
			return q.isEmpty();
		}
	}
	
	/**
	 * Runs the next task in the queue and removes it
	 */
	public void runTask() {
		synchronized (q) {
			currentTask = q.remove(0);
		}
		
		currentTask.run(this);
	}
	
	/**
	 * Retrieves the most recently run task
	 * 
	 * @return The most recently run task, or null if none have been run
	 */
	public Task getLatestTask() {
		return currentTask;
	}
	
	/**
	 * Determines if the queue will execute the task in question
	 * 
	 * @param task The task to check
	 * @return True if the task is enqueued to be run
	 */
	public boolean contains(Task task) {
		synchronized (q) {
			return q.contains(task);
		}
	}
	
}
