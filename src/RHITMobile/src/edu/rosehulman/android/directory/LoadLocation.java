package edu.rosehulman.android.directory;

import android.os.AsyncTask;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.model.Location;

/**
 * Load a \ref .model.Location
 */
public class LoadLocation extends AsyncTask<Long, Void, Location> {
	
	/**
	 * Listener for when a Location is loaded
	 */
	public interface OnLocationLoadedListener {
		
		/**
		 * Called when a Location is loaded
		 * 
		 * @param location The loaded Location
		 */
		public void onLocationLoaded(Location location);
	}
	
	private OnLocationLoadedListener listener;
	
	/**
	 * Creates a new LoadLocation task
	 * 
	 * @param listener The task to run once the location is successfully loaded
	 */
	public LoadLocation(OnLocationLoadedListener listener) {
		this.listener = listener;
	}

	@Override
	protected Location doInBackground(Long... params) {
		Long id = params[0];
		
		LocationAdapter locationAdapter = new LocationAdapter();
		locationAdapter.open();
		Location loc = locationAdapter.getLocation(id);
		locationAdapter.loadHyperlinks(loc);
		locationAdapter.loadAlternateNames(loc);
		locationAdapter.close();
		
		return loc;
	}
	
	@Override
	protected void onPostExecute(Location res) {
		listener.onLocationLoaded(res);
	}

}
