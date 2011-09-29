package edu.rosehulman.android.directory.service;

import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONObject;

public class JsonClient extends RestClient {

	/**
	 * Create a new JsonClient
	 * 
	 * @param host The host to send the request to (i.e. example.com)
	 * @param port The port the remote service is running on
	 * @param path The request path (i.e. index.htm)
	 */
	public JsonClient(String host, int port, String path) {
		super(host, port, path);
	}

	/**
	 * Execute the REST request, parsing the server's response as a
	 * JSONObject
	 * 
	 * @return A new JSONObject constructed from the server's response
	 * 
	 * @throws URISyntaxException If an invalid URI was used
	 * @throws Exception On other errors, including Internal Server Errors
	 */
	public JSONObject execute() throws URISyntaxException, Exception {
		HttpResponse response = super.performRequest();
		
		ResponseHandler<String> handler = new BasicResponseHandler();
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 204) {
			return null;
		}
		
		String responseBody = handler.handleResponse(response);
		JSONObject root = new JSONObject(responseBody);
		
		return root;
	}
	
}
