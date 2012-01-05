package edu.rosehulman.android.directory.service;

import org.json.JSONObject;

import edu.rosehulman.android.directory.model.DirectionsResponse;
import edu.rosehulman.android.directory.model.LocationCollection;
import edu.rosehulman.android.directory.model.LocationNamesCollection;

/**
 * Wraps logic of communicating with the mobile directory web service into
 * native java methods.
 * 
 * Note that all function calls requiring network access are blocking and
 * should not be called on the main thread.
 */
public class MobileDirectoryService implements IMobileDirectoryService {

	private static final String HOST = "mobilewin.csse.rose-hulman.edu";
	private static final int PORT = 5600;
	
	private static ClientFactory factory;
	
	/**
	 * Set the client factory to be used when making web service calls
	 * 
	 * @param factory The client factory to use
	 */
	public static void setClientFactory(ClientFactory factory) {
		MobileDirectoryService.factory = factory;
	}
	
	@Override
	public LocationCollection getTopLocationData(String currentVersion) throws Exception {
		return getLocationCollection("locations/data/top", currentVersion);
	}
	
	@Override
	public LocationCollection getLocationData(long parent, String currentVersion) throws Exception {
		String url = String.format("locations/data/within/%d", parent);
		return getLocationCollection(url, currentVersion);
	}

	private LocationCollection getLocationCollection(String url, String currentVersion) throws Exception {
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		if (currentVersion != null) {
			client.addParameter("version", currentVersion);
		}
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return LocationCollection.deserialize(root);
	}
	
	@Override
	public LocationNamesCollection searchLocations(String query) throws Exception {
		JsonClient client = factory.makeJsonClient(HOST, PORT, "locations/names");
		client.addParameter("s", query);
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return LocationNamesCollection.deserialize(root);
	}
	
	@Override
	public DirectionsResponse getDirections(long from, long to) throws Exception {
		//FIXME temporary directions URL
		String url = String.format("directions/testing/directions", from, to);
		//String url = String.format("directions/fromloc/%d/toloc/%d", from, to);
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return DirectionsResponse.deserialize(root);
	}
	
	@Override
	public DirectionsResponse getTour() throws Exception {
		String url = String.format("directions/testing/tour");
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return DirectionsResponse.deserialize(root);
	}
	
	@Override
	public DirectionsResponse getDirectionsStatus(int requestId) throws Exception {
		String url = String.format("directions/status/%d", requestId);
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return DirectionsResponse.deserialize(root);
	}
}
