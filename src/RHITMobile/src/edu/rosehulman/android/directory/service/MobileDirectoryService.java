package edu.rosehulman.android.directory.service;

import org.json.JSONObject;

import edu.rosehulman.android.directory.model.CampusServicesResponse;
import edu.rosehulman.android.directory.model.DirectionsResponse;
import edu.rosehulman.android.directory.model.LocationCollection;
import edu.rosehulman.android.directory.model.LocationIdsResponse;
import edu.rosehulman.android.directory.model.LocationNamesCollection;
import edu.rosehulman.android.directory.model.TourTagsResponse;
import edu.rosehulman.android.directory.model.VersionResponse;
import edu.rosehulman.android.directory.util.ArrayUtil;

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
	public VersionResponse getVersions() throws Exception {
		JsonClient client = factory.makeJsonClient(HOST, PORT, "");
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return VersionResponse.deserialize(root);
	}
	
	@Override
	public CampusServicesResponse getCampusServicesData(String currentVersion) throws Exception {
		JsonClient client = factory.makeJsonClient(HOST, PORT, "services");
		if (currentVersion != null) {
			client.addParameter("version", currentVersion);
		}
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return CampusServicesResponse.deserialize(root);
	}
	
	@Override
	public TourTagsResponse getTourTagData(String currentVersion) throws Exception {
		JsonClient client = factory.makeJsonClient(HOST, PORT, "tours/tags");
		if (currentVersion != null) {
			client.addParameter("version", currentVersion);
		}
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return TourTagsResponse.deserialize(root);
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
		String url = String.format("directions/fromloc/%d/toloc/%d", from, to);
		return getDirectionsResponse(url);
	}
	
	@Override
	public DirectionsResponse getTour(long startId, long[] tagIds) throws Exception {
		String url = String.format("tours/oncampus/fromloc/%d/%s", 
				startId, ArrayUtil.join(tagIds, "/"));
		return getDirectionsResponse(url);
	}

	@Override
	public LocationIdsResponse getTour(long[] tagIds) throws Exception {
		String url = String.format("tours/offcampus/%s", ArrayUtil.join(tagIds, "/"));
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return LocationIdsResponse.deserialize(root);
	}
	
	@Override
	public DirectionsResponse getDirectionsStatus(int requestId) throws Exception {
		String url = String.format("directions/status/%d", requestId);
		return getDirectionsResponse(url);
	}
	
	@Override
	public DirectionsResponse getOncampusTourStatus(int requestId) throws Exception {
		String url = String.format("tours/oncampus/status/%d", requestId);
		return getDirectionsResponse(url);
	}
	
	private DirectionsResponse getDirectionsResponse(String url) throws Exception {
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return DirectionsResponse.deserialize(root);
	}
}
