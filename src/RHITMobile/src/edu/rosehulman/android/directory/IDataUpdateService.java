package edu.rosehulman.android.directory;

public interface IDataUpdateService {
	
	public interface AsyncRequest {
		public void onQueued(Runnable cancelCallback);
		public void onCompleted();
	}
	
	void startUpdate();
	void abort();
	
	void requestTopLocations(AsyncRequest listener);
	void requestInnerLocation(long id, AsyncRequest listener);
	void requestTourTags(AsyncRequest listener);
	void requestCampusServices(AsyncRequest listener);
	
	boolean isUpdating();	
}
