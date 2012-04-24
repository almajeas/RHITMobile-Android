package edu.rosehulman.android.directory.service;

import java.io.IOException;

import org.json.JSONException;

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

/**
 * Interface for communicating with the mobile directory web service
 */
public interface IMobileDirectoryService {
	
	/**
	 * Retrieve the current versions of all server data
	 * 
	 * @return The current version of server data
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public VersionResponse getVersions() throws ClientException, ServerException, JSONException, IOException;
	
	/**
	 * Retrieve campus services links
	 * 
	 * @param currentVersion The current version data, or null if not known
	 * @return A collection of categories
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public CampusServicesResponse getCampusServicesData(String currentVersion) throws ClientException, ServerException, JSONException, IOException;
	
	/**
	 * Retrieve tour categories
	 * 
	 * @param currentVersion The current version data, or null if not known
	 * @return A collection of groups
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public TourTagsResponse getTourTagData(String currentVersion) throws ClientException, ServerException, JSONException, IOException;

	/**
	 * Retrieve top-level location data contained on the server
	 * 
	 * @param currentVersion The current version data, or null if not known
	 * @return A collection of locations
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public LocationCollection getTopLocationData(String currentVersion) throws ClientException, ServerException, JSONException, IOException;
	
	/**
	 * Retrieve location data within the given location id
	 * 
	 * @param parent The id of the parent location
	 * @param currentVersion The current version data, or null if not known
	 * @return A collection of locations
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public LocationCollection getLocationData(long parent, String currentVersion) throws ClientException, ServerException, JSONException, IOException;
	
	/**
	 * Retrieve ids of locations that match the given query
	 * 
	 * @param query The search query
	 * @return A collection of LocationName objects
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public LocationNamesCollection searchLocations(String query) throws ClientException, ServerException, JSONException, IOException;
	
	/**
	 * Start a directions request from a location to another
	 * 
	 * @param from The id of the departing location
	 * @param to The id of the destination location
	 * @return A DirectionsResponse with the id of the request and possibly results
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public DirectionsResponse getDirections(long from, long to) throws ClientException, ServerException, JSONException, IOException;
	
	/**
	 * Start a tour generation request, starting at a location
	 * 
	 * @param startId The id of the start location
	 * @param tagIds The selected tags of interest
	 * @return A DirectionsResponse with the id of the request and possibly results
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public DirectionsResponse getTour(long startId, long[] tagIds) throws ClientException, ServerException, JSONException, IOException;
	
	/**
	 * Start a tour generation request, offsite
	 * 
	 * @param tagIds The selected tags of interest
	 * @return A DirectionsResponse with the id of the request and possibly results
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public LocationIdsResponse getTour(long[] tagIds) throws ClientException, ServerException, JSONException, IOException;
	
	/**
	 * Checks the status of a directions request
	 * @param requestId The ID of the request
	 * @return The updated status
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public DirectionsResponse getDirectionsStatus(int requestId) throws ClientException, ServerException, JSONException, IOException;
	
	/**
	 * Checks the status of an on-campus tour request
	 * @param requestId The ID of the request
	 * @return The updated status
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public DirectionsResponse getOncampusTourStatus(int requestId) throws ClientException, ServerException, JSONException, IOException;
	
	/**
	 * Authenticates the user
	 * @param username The user's username
	 * @param password The user's password
	 * @return An auth token, or null if invalid credentials were provided
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public AuthenticationResponse login(String username, String password) throws ClientException, ServerException, JSONException, IOException;
	
	/**
	 * Gets information about a specific user
	 * @param authToken The valid, active auth token
	 * @param username The username to lookup
	 * @return user data
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public UserDataResponse getUser(String authToken, String username) throws ClientException, ServerException, JSONException, IOException;
	
	/**
	 * Gets lightweight information about a group of users
	 * @param authToken The valid, active auth token
	 * @param username The search query
	 * @return lightweight user data list
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public UsersResponse searchUsers(String authToken, String search) throws ClientException, ServerException, JSONException, IOException;
	
	/**
	 * Looks up a specific user's schedule
	 * @param authToken The valid, active auth token
	 * @param username The user's username
	 * @return The courses the user is currently taking
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public CoursesResponse getUserSchedule(String authToken, String username) throws ClientException, ServerException, JSONException, IOException;

	/**
	 * Looks up information about a specific course
	 * @param authToken The valid, active auth token
	 * @param term The term the course exists in
	 * @param crn The CRN of the course
	 * @return Information about the course
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public Course getCourse(String authToken, String term, int crn) throws ClientException, ServerException, JSONException, IOException;
	
	/**
	 * Searches through all courses 
	 * @param authToken The valid, active auth token
	 * @param search The search query
	 * @return Matching courses
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public CoursesResponse searchCourses(String authToken, String search) throws ClientException, ServerException, JSONException, IOException;
	
	/**
	 * Gets the schedule for a specific room
	 * @param authToken The valid, active auth token
	 * @param room The name of the room
	 * @return The schedule for the found room, or null if no room is found
	 * @throws ClientException If the request was invalid. Change the request before retrying
	 * @throws ServerException If the server had an error. Change the request or try again later
	 * @throws JSONException Likely out of date client. Update client
	 * @throws IOException General network connectivity issue
	 */
	public CoursesResponse getRoomSchedule(String authToken, String room) throws ClientException, ServerException, JSONException, IOException;
}
