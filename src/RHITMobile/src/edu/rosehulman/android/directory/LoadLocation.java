package edu.rosehulman.android.directory;

import android.os.AsyncTask;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.model.Location;

/**
 * Load a \ref model.Location
 */
public class LoadLocation extends AsyncTask<Void, Void, Location> {
	
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
	
	private long id;
	private String name;
	
	/**
	 * Creates a new LoadLocation task
	 * 
	 * @param listener The task to run once the location is successfully loaded
	 * @param id The id of the location to load
	 */
	public LoadLocation(long id, OnLocationLoadedListener listener) {
		this.listener = listener;
		this.id = id;
		this.name = null;
	}
	
	/**
	 * Creates a new LoadLocation task
	 * 
	 * @param listener The task to run once the location is successfully loaded
	 * @param name A search term to use to find the location
	 */
	public LoadLocation(String name, OnLocationLoadedListener listener) {
		this.listener = listener;
		this.id = -1;
		this.name = name;
	}

	@Override
	protected Location doInBackground(Void... params) {
		LocationAdapter locationAdapter = new LocationAdapter();
		locationAdapter.open();

		if (name != null) {
			id = locationAdapter.findExactLocation(name, false);
		}
		
		if (id == -1) {
			//location not found
			return null;
		}
		
		Location loc = locationAdapter.getLocation(id);
		if (loc == null) {
			//invalid id
			return null;
		}
		
		locationAdapter.loadHyperlinks(loc);
		locationAdapter.loadAlternateNames(loc);
		locationAdapter.close();
		
		return loc;
	}
	
	public static Location loadLocationSynchronously(long id) {
		LocationAdapter locationAdapter = new LocationAdapter();
		locationAdapter.open();
		try {
			Location loc = locationAdapter.getLocation(id);
			
			if (loc == null) {
				//invalid id
				return null;
			}

			locationAdapter.loadHyperlinks(loc);
			locationAdapter.loadAlternateNames(loc);

			return loc;
			
		} finally {
			locationAdapter.close();
		}
	}
	
	@Override
	protected void onPostExecute(Location res) {
		listener.onLocationLoaded(res);
	}

}
