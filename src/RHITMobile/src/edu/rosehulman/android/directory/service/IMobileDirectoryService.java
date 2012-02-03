package edu.rosehulman.android.directory.service;

import edu.rosehulman.android.directory.model.CampusServicesResponse;
import edu.rosehulman.android.directory.model.DirectionsResponse;
import edu.rosehulman.android.directory.model.LocationCollection;
import edu.rosehulman.android.directory.model.LocationIdsResponse;
import edu.rosehulman.android.directory.model.LocationNamesCollection;
import edu.rosehulman.android.directory.model.TourTagsResponse;
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
	 * Start a tour generation request
	 * 
	 * @return A DirectionsResponse with the id of the request and possibly results
	 * @throws Exception On error
	 */
	@Deprecated
	public DirectionsResponse getTour() throws Exception;
	
	/**
	 * Checks the status of a directions request
	 * @param requestId
	 * @return
	 * @throws Exception
	 */
	public DirectionsResponse getDirectionsStatus(int requestId) throws Exception;

}
