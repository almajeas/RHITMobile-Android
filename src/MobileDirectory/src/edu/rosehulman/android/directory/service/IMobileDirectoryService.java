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
	 * @return The collection of locations
	 * @throws Exception On error
	 */
	public LocationCollection getAllLocationData(String currentVersion) throws Exception;
	
	/**
	 * Retrieve all map areas
	 * 
	 * @param currentVersion The current version data, or null if not known
	 * @return The collection of locations
	 * @throws Exception On error
	 */
	@Deprecated
	public LocationCollection getMapAreas(String currentVersion) throws Exception;

}
