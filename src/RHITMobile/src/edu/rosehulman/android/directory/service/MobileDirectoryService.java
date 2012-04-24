package edu.rosehulman.android.directory.service;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import edu.rosehulman.android.directory.model.AuthenticationResponse;
import edu.rosehulman.android.directory.model.CampusServicesResponse;
import edu.rosehulman.android.directory.model.Course;
import edu.rosehulman.android.directory.model.CoursesResponse;
import edu.rosehulman.android.directory.model.DirectionsResponse;
import edu.rosehulman.android.directory.model.LocationCollection;
import edu.rosehulman.android.directory.model.LocationIdsResponse;
import edu.rosehulman.android.directory.model.LocationNamesCollection;
import edu.rosehulman.android.directory.model.TourTagsResponse;
import edu.rosehulman.android.directory.model.UserDataResponse;
import edu.rosehulman.android.directory.model.UsersResponse;
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
	private static final int PORT = 5601;
	
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
	public AuthenticationResponse login(String username, String password) throws ClientException, ServerException, JSONException, IOException {
		JsonClient client = factory.makeJsonClient(HOST, PORT, "/banner/authenticate");
		client.addHeader("Login-Username", username);
		client.addHeader("Login-Password", password);
		
		JSONObject root = client.execute();
		
		if (root == null) {
			return null;
		}
		
		return AuthenticationResponse.deserialize(root);
	}

	@Override
	public UserDataResponse getUser(String authToken, String username) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("/banner/user/data/%s", username);
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
		String url = String.format("/banner/user/search/%s", search);
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		client.addHeader("Auth-Token", authToken);
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return UsersResponse.deserialize(root);
	}

	@Override
	public CoursesResponse getUserSchedule(String authToken, String username) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("/banner/user/schedule/%s", username);
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
		client.addParameter("getschedule", "true");
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
	public CoursesResponse searchCourses(String authToken, String search) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("/banner/course/search/%s", search);
		JsonClient client = factory.makeJsonClient(HOST, PORT, url);
		client.addHeader("Auth-Token", authToken);
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return CoursesResponse.deserialize(root);
	}

	@Override
	public CoursesResponse getRoomSchedule(String authToken, String room) throws ClientException, ServerException, JSONException, IOException {
		String url = String.format("/banner/room/schedule/%s", room);
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
