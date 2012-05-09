package edu.rosehulman.android.directory.service;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import edu.rosehulman.android.directory.model.*;
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
	private static final int PORT = 5601;
	
//	private static final String DEBUG_HOST = "rhitmobile-authtest.herokuapp.com";
//	private static final int DEBUG_PORT = 443;

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
	public VersionResponse getVersions() throws ClientException, ServerException, JSONException, IOException {
		JsonClient client = factory.makeJsonClient(HOST, PORT, "");
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return VersionResponse.deserialize(root);
	}
	
	@Override
	public CampusServicesResponse getCampusServicesData(String currentVersion) throws ClientException, ServerException, JSONException, IOException {
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
	public TourTagsResponse getTourTagData(String currentVersion) throws ClientException, ServerException, JSONException, IOException {
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
	public LocationCollection getTopLocationData(String currentVersion) throws ClientException, ServerException, JSONException, IOException {
		return getLocationCollection("locations/data/top", currentVersion);
	}
	
	@Override
	public LocationCollection getLocationData(long parent, String currentVersion) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("locations/data/within/%d", parent);
		return getLocationCollection(url, currentVersion);
	}

	private LocationCollection getLocationCollection(String url, String currentVersion) throws ClientException, ServerException, JSONException, IOException {
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
	public LocationNamesCollection searchLocations(String query) throws ClientException, ServerException, JSONException, IOException {
		JsonClient client = factory.makeJsonClient(HOST, PORT, "locations/names");
		client.addParameter("s", query);
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return LocationNamesCollection.deserialize(root);
	}
	
	@Override
	public LocationIdResponse lookupLocation(String name) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("locations/withname/%s", URLEncoder.encode(name));
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return LocationIdResponse.deserialize(root);
	}
	
	public DirectionsResponse getDirections(LatLon from, long to) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("directions/fromgps/%f/%f/toloc/%d", from.getLatitude(), from.getLongitude(), to);
		return getDirectionsResponse(url);
	}
	
	@Override
	public DirectionsResponse getDirections(long from, long to) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("directions/fromloc/%d/toloc/%d", from, to);
		return getDirectionsResponse(url);
	}
	
	@Override
	public DirectionsResponse getTour(long startId, long[] tagIds) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("tours/oncampus/fromloc/%d/%s", 
				startId, ArrayUtil.join(tagIds, "/"));
		return getDirectionsResponse(url);
	}
	
	@Override
	public DirectionsResponse getTour(LatLon start, long[] tagIds) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("tours/oncampus/fromgps/%f/%f/%s", 
				start.getLatitude(), start.getLongitude(), ArrayUtil.join(tagIds, "/"));
		return getDirectionsResponse(url);
	}

	@Override
	public LocationIdsResponse getTour(long[] tagIds) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("tours/offcampus/%s", ArrayUtil.join(tagIds, "/"));
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return LocationIdsResponse.deserialize(root);
	}
	
	@Override
	public DirectionsResponse getDirectionsStatus(int requestId) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("directions/status/%d", requestId);
		return getDirectionsResponse(url);
	}
	
	@Override
	public DirectionsResponse getOncampusTourStatus(int requestId) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("tours/oncampus/status/%d", requestId);
		return getDirectionsResponse(url);
	}
	
	private DirectionsResponse getDirectionsResponse(String url) throws ClientException, ServerException, JSONException, IOException {
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return DirectionsResponse.deserialize(root);
	}

	@Override
	public BannerAuthResponse login(String username, String password) throws ClientException, ServerException, JSONException, IOException {
		JsonClient client = factory.makeJsonClient(HOST, PORT, "/banner/authenticate");
		client.addHeader("Login-Username", username);
		client.addHeader("Login-Password", password);
		
		JSONObject root = client.execute();
		
		if (root == null) {
			return null;
		}
		
		return BannerAuthResponse.deserialize(root);
	}

	@Override
	public UserDataResponse getUser(String authToken, String username) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("/banner/user/data/%s", URLEncoder.encode(username));
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		client.addHeader("Auth-Token", authToken);
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return UserDataResponse.deserialize(root);
	}

	@Override
	public UsersResponse searchUsers(String authToken, String search) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("/banner/user/search/%s", URLEncoder.encode(search));
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		client.addHeader("Auth-Token", authToken);
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return UsersResponse.deserialize(root);
	}

	@Override
	public CoursesResponse getUserSchedule(String authToken, String term, String username) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("/banner/user/schedule/%s/%s", term, URLEncoder.encode(username));
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		client.addHeader("Auth-Token", authToken);
		client.addParameter("getschedule", "true");
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return CoursesResponse.deserialize(root);
	}

	@Override
	public Course getCourse(String authToken, String term, int crn) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("/banner/course/data/%s/%d", term, crn);
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		client.addHeader("Auth-Token", authToken);
		client.addParameter("getenrolled", "true");
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		CoursesResponse response = CoursesResponse.deserialize(root);
		
		assert(response.courses.length != 1);
		
		return response.courses[0]; 
	}

	@Override
	public CoursesResponse searchCourses(String authToken, String term, String search) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("/banner/course/search/%s/%s", term, URLEncoder.encode(search));
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		client.addHeader("Auth-Token", authToken);
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return CoursesResponse.deserialize(root);
	}

	@Override
	public CoursesResponse getRoomSchedule(String authToken, String term, String room) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("/banner/room/schedule/%s/%s", term, URLEncoder.encode(room));
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		client.addHeader("Auth-Token", authToken);
		client.addParameter("getschedule", "true");
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return CoursesResponse.deserialize(root);
	}
}
