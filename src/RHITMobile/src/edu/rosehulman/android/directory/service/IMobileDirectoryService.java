package edu.rosehulman.android.directory.service;

import edu.rosehulman.android.directory.model.LocationCollection;
import edu.rosehulman.android.directory.model.LocationNamesCollection;

/**
 * Interface for communicating with the mobile directory web service
 */
public interface IMobileDirectoryService {
	
	/**
	 * Retrieve all location data contained on the server
	 * 
	 * @param currentVersion The current version data, or null if not known
	 * @return A collection of locations
	 * @throws Exception On error
	 */
	@Deprecated
	public LocationCollection getAllLocationData(String currentVersion) throws Exception;

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
	 * @return A collection of \ref LocationName objects
	 * @throws Exception On error
	 */
	public LocationNamesCollection searchLocations(String query) throws Exception;

}
