package edu.rosehulman.android.directory.service;

/**
 * Factory class to generate Client objects that do not
 * actually talk to a server and only server static data.
 */
public class MockClientFactory implements ClientFactory {

	@Override
	public RestClient makeRestClient(String host, int port, String path) {
		//TODO make a mock rest client (if ever needed)
		return null;
	}
	
	@Override
	public JsonClient makeJsonClient(String host, int port, String path) {
		return new MockJsonClient(host, port, path);
	}


}
