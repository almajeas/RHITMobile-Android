package edu.rosehulman.android.directory.service;

import edu.rosehulman.android.directory.model.LocationCollection;

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
	 * @param currentVersion The current version data, or null if not known
	 * @return A collection of locations
	 * @throws Exception On error
	 */
	public LocationCollection getLocationData(long parent, String currentVersion) throws Exception;

}
