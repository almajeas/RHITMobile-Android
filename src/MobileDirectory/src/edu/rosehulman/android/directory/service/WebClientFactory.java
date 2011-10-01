package edu.rosehulman.android.directory.service;

/**
 * Factory class to generate Client objects that communicate
 * with a web service to acquire their data
 *
 */
public class WebClientFactory implements ClientFactory {
	
	@Override
	public RestClient makeRestClient(String host, int port, String path) {
		return new BasicRestClient(host, port, path);
	}

	@Override
	public JsonClient makeJsonClient(String host, int port, String path) {
		return new JsonClient(host, port, path);
	}
}
