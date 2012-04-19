package edu.rosehulman.android.directory.service;

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
	 * @throws Exception On error
	 */
	public VersionResponse getVersions() throws Exception;
	
	/**
	 * Retrieve campus services links
	 * 
	 * @param currentVersion The current version data, or null if not known
	 * @return A collection of categories
	 * @throws Exception On error
	 */
	public CampusServicesResponse getCampusServicesData(String currentVersion) throws Exception;
	
	/**
	 * Retrieve tour categories
	 * 
	 * @param currentVersion The current version data, or null if not known
	 * @return A collection of groups
	 * @throws Exception On error
	 */
	public TourTagsResponse getTourTagData(String currentVersion) throws Exception;

	/**
	 * Retrieve top-level location data contained on the server
	 * 
	 * @param currentVersion The current version data, or null if not known
	 * @return A collection of locations
	 * @throws Exception On error
	 */
	public LocationCollection getTopLocationData(String currentVersion) throws Exception;
	
	/**
	 * Retrieve location data within the given location id
	 * 
	 * @param parent The id of the parent location
	 * @param currentVersion The current version data, or null if not known
	 * @return A collection of locations
	 * @throws Exception On error
	 */
	public LocationCollection getLocationData(long parent, String currentVersion) throws Exception;
	
	/**
	 * Retrieve ids of locations that match the given query
	 * 
	 * @param query The search query
	 * @return A collection of LocationName objects
	 * @throws Exception On error
	 */
	public LocationNamesCollection searchLocations(String query) throws Exception;
	
	/**
	 * Start a directions request from a location to another
	 * 
	 * @param from The id of the departing location
	 * @param to The id of the destination location
	 * @return A DirectionsResponse with the id of the request and possibly results
	 * @throws Exception On error
	 */
	public DirectionsResponse getDirections(long from, long to) throws Exception;
	
	/**
	 * Start a tour generation request, starting at a location
	 * 
	 * @param startId The id of the start location
	 * @param tagIds The selected tags of interest
	 * @return A DirectionsResponse with the id of the request and possibly results
	 * @throws Exception On error
	 */
	public DirectionsResponse getTour(long startId, long[] tagIds) throws Exception;
	
	/**
	 * Start a tour generation request, offsite
	 * 
	 * @param tagIds The selected tags of interest
	 * @return A DirectionsResponse with the id of the request and possibly results
	 * @throws Exception On error
	 */
	public LocationIdsResponse getTour(long[] tagIds) throws Exception;
	
	/**
	 * Checks the status of a directions request
	 * @param requestId The ID of the request
	 * @return The updated status
	 * @throws Exception On error
	 */
	public DirectionsResponse getDirectionsStatus(int requestId) throws Exception;
	
	/**
	 * Checks the status of an on-campus tour request
	 * @param requestId The ID of the request
	 * @return The updated status
	 * @throws Exception On error
	 */
	public DirectionsResponse getOncampusTourStatus(int requestId) throws Exception;
	
	/**
	 * Authenticates the user
	 * @param username The user's username
	 * @param password The user's password
	 * @return An auth token, or null if invalid credentials were provided
	 * @throws Exception On error
	 */
	public AuthenticationResponse login(String username, String password) throws Exception;
	
	/**
	 * Gets information about a specific user
	 * @param authToken The valid, active auth token
	 * @param username The username to lookup
	 * @return user data
	 * @throws Exception On error
	 */
	public UserDataResponse getUser(String authToken, String username) throws Exception;
	
	/**
	 * Gets lightweight information about a group of users
	 * @param authToken The valid, active auth token
	 * @param username The search query
	 * @return lightweight user data list
	 * @throws Exception On error
	 */
	public UsersResponse searchUsers(String authToken, String search) throws Exception;
	
	/**
	 * Looks up a specific user's schedule
	 * @param authToken The valid, active auth token
	 * @param username The user's username
	 * @return The courses the user is currently taking
	 * @throws Exception On error
	 */
	public CoursesResponse getUserSchedule(String authToken, String username) throws Exception;

	/**
	 * Looks up information about a specific course
	 * @param authToken The valid, active auth token
	 * @param term The term the course exists in
	 * @param crn The CRN of the course
	 * @return Information about the course
	 * @throws Exception On error
	 */
	public Course getCourse(String authToken, String term, int crn) throws Exception;
	
	/**
	 * Searches through all courses 
	 * @param authToken The valid, active auth token
	 * @param search The search query
	 * @return Matching courses
	 * @throws Exception On error
	 */
	public CoursesResponse searchCourses(String authToken, String search) throws Exception;
	
	/**
	 * Gets the schedule for a specific room
	 * @param authToken The valid, active auth token
	 * @param room The name of the room
	 * @return The schedule for the found room, or null if no room is found
	 * @throws Exception On error
	 */
	public CoursesResponse getRoomSchedule(String authToken, String room) throws Exception;
}
