package edu.rosehulman.android.directory;

public interface IDataUpdateService {
	
	void startUpdate();
	void abort();
	
	//boolean areLocationsUpdated();
	//boolean requestLocation(long id, Runnable listener);

}
