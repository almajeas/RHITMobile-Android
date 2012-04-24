package edu.rosehulman.android.directory.service;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides an implementation of RestClient that parses the response
 * as a JSONObject
 */
public class JsonClient extends RestClient implements Client<JSONObject> {

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
	 * Execute the REST request, parsing the server's response as a JSONObject
	 * 
	 * @return A new JSONObject constructed from the server's response, or null if the data was up to date
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	@Override
	public JSONObject execute() throws ClientException, ServerException, JSONException, IOException {
		try {
			HttpResponse response = super.performRequest();
			
			ResponseHandler<String> handler = new BasicResponseHandler();
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 204) {
				return null;
			}
			
			String responseBody = handler.handleResponse(response);
			
			JSONObject root = new JSONObject(responseBody);
			
			return root;

		} catch (ClientProtocolException e) {
			if (e instanceof HttpResponseException) {
				HttpResponseException ex = (HttpResponseException)e;
				int errorClass = ex.getStatusCode() / 100;
				switch (errorClass) {
					case 4:
						throw new ClientException(e.getMessage(), e);

					case 5:
						throw new ServerException(e.getMessage(), e);
						
					default:
						throw new NetworkException("Unexpected server response code", e);
				}
				
			} else {
				throw new NetworkException("Client protocol error", e);
			}
		}
	}
	
}
