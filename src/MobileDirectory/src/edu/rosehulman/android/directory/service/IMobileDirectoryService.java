package edu.rosehulman.android.directory.service;

import edu.rosehulman.android.directory.model.MapAreaCollection;

public interface IMobileDirectoryService {
	
	public MapAreaCollection getMapAreas(String currentVersion) throws Exception;

}
