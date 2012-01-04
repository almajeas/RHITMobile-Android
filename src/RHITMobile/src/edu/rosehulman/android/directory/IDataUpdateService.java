package edu.rosehulman.android.directory;

public interface IDataUpdateService {
	
	public interface AsyncRequest {
		public void onQueued();
		public void onCompleted();
	}
	
	void startUpdate();
	void abort();
	
	void requestTopLocations(AsyncRequest listener);
	
	//boolean areLocationsUpdated();
	//boolean requestLocation(long id, Runnable listener);

}
