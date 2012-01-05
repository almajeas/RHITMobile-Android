package edu.rosehulman.android.directory.service;

import edu.rosehulman.android.directory.model.DirectionsResponse;
import edu.rosehulman.android.directory.model.LocationCollection;
import edu.rosehulman.android.directory.model.LocationNamesCollection;

/**
 * Interface for communicating with the mobile directory web service
 */
public interface IMobileDirectoryService {

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
