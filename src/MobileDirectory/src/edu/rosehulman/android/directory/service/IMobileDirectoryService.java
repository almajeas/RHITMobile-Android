package edu.rosehulman.android.directory.service;

import edu.rosehulman.android.directory.model.LocationCollection;

public interface IMobileDirectoryService {
	
	public LocationCollection getAllLocationData(String currentVersion) throws Exception;
	
	@Deprecated
	public LocationCollection getMapAreas(String currentVersion) throws Exception;

}
