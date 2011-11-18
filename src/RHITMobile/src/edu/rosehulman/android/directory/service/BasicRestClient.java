package edu.rosehulman.android.directory.service;

import org.apache.http.HttpResponse;

/**
 * Provides a basic implementation of a RestClient that
 * provides a HttpResponse as a result
 */
public class BasicRestClient extends RestClient implements Client<HttpResponse> {

	/**
	 * Create a new BasicRestClient
	 * 
	 * @param host The host to send the request to (i.e. example.com)
	 * @param port The port the remote service is running on
	 * @param path The request path (i.e. index.htm)
	 */
	public BasicRestClient(String host, int port, String path) {
		super(host, port, path);
	}

	/**
	 * Perform the REST request, parsing the server's response as a
	 * JSONObject
	 * 
	 * @return The HttpResponse
	 * 
	 * @throws Exception On error, including Internal Server Errors
	 */
	@Override
	public HttpResponse execute() throws Exception {
		return super.performRequest();
	}

}
