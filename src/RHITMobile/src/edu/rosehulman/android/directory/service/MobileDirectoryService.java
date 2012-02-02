package edu.rosehulman.android.directory.service;

import org.json.JSONObject;

import edu.rosehulman.android.directory.model.CampusServicesResponse;
import edu.rosehulman.android.directory.model.DirectionsResponse;
import edu.rosehulman.android.directory.model.LocationCollection;
import edu.rosehulman.android.directory.model.LocationNamesCollection;
import edu.rosehulman.android.directory.model.TourTag;
import edu.rosehulman.android.directory.model.TourTagsGroup;
import edu.rosehulman.android.directory.model.TourTagsResponse;
import edu.rosehulman.android.directory.model.VersionResponse;

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
		
		if (currentVersion == "") {
			//FIXME remove
			TourTagsResponse res = new TourTagsResponse();
			res.version = "0";
			res.root = new TourTagsGroup(null, new TourTag[] {
					new TourTag(0, "General")
			}, new TourTagsGroup[] {
					new TourTagsGroup("Academic", new TourTag[] {
							new TourTag(1, "General"),
					}, new TourTagsGroup[] {
							new TourTagsGroup("Majors", new TourTag[] {
									new TourTag(2, "Computer Science"),
									new TourTag(3, "Software Engineering"),
									new TourTag(4, "Civil Engineering"),
									new TourTag(5, "Mechanical Engineering")
							}, new TourTagsGroup[] {})
					}),
					new TourTagsGroup("Athletics", new TourTag[] {
							new TourTag(6, "General"),
					}, new TourTagsGroup[] {
							new TourTagsGroup("Sports", new TourTag[] {
									new TourTag(7, "Soccer"),
									new TourTag(8, "Football"),
									new TourTag(9, "Tennis")
							}, new TourTagsGroup[] {})
					})
			});
			return res;
		}
		
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
