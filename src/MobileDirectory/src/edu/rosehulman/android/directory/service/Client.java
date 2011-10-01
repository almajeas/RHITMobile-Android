package edu.rosehulman.android.directory.service;

/**
 * Defines the interface for a client that can
 * perform some action with a remote server
 *
 * @param <T> The result of the operation
 */
public interface Client<T> {
	
	/**
	 * Perform the request, generating the response object
	 * 
	 * @return The result of the request
	 * @throws Exception on errors
	 */
	public T execute() throws Exception;
}
