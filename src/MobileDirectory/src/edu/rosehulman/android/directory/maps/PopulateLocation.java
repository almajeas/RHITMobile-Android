package edu.rosehulman.android.directory.maps;

import android.os.AsyncTask;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.model.Location;

public class PopulateLocation extends AsyncTask<Location, Void, Location> {
	
	private Runnable task;
	
	public PopulateLocation(Runnable task) {
		this.task = task;
	}

	@Override
	protected Location doInBackground(Location... params) {
		Location loc = params[0];
		
		LocationAdapter locationAdapter = new LocationAdapter();
		locationAdapter.open();
		locationAdapter.loadHyperlinks(loc);
		locationAdapter.loadAlternateNames(loc);
		
		return loc;
	}
	
	@Override
	protected void onPostExecute(Location res) {
		task.run();
	}

}
