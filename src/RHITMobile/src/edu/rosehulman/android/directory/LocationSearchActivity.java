package edu.rosehulman.android.directory;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import edu.rosehulman.android.directory.LoadLocation.OnLocationLoadedListener;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.db.VersionsAdapter;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.model.LocationName;
import edu.rosehulman.android.directory.model.LocationNamesCollection;
import edu.rosehulman.android.directory.model.VersionType;
import edu.rosehulman.android.directory.service.MobileDirectoryService;

public class LocationSearchActivity extends ListActivity {
	
	private String searchQuery;
	private TaskManager taskManager;

	private LocationInfo[] locations;
	private ArrayAdapter<LocationInfo> dataSet;
	
	private Button btnShowOnMap;
	
	private class LocationInfo {
		public long id;
		public String name;
		public String description;
		
		public LocationInfo(LocationName info) {
			id = info.id;
			name = info.name;
		}
	}
	
	private void runSearch(String query) {
		searchQuery = query;
		SearchLocations task = new SearchLocations();
		taskManager.addTask(task);
		task.execute(searchQuery);
		setTitle("Search: " + searchQuery);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
    	if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
    		//are you lost?
    		return;
    	}
    	
		runSearch(intent.getStringExtra(SearchManager.QUERY));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_search);
		
		taskManager = new TaskManager();

    	Intent intent = getIntent();
    	
    	btnShowOnMap = (Button)findViewById(R.id.btnShowOnMap);
    	btnShowOnMap.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnShowOnMap_clicked();
			}
		});
    	
    	if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
    		runSearch(intent.getStringExtra(SearchManager.QUERY));
    		
    	} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
		    Uri data = intent.getData();
		    
		    long id = Long.parseLong(data.getPath());
		    LoadLocation task = new LoadLocation(new OnLocationLoadedListener() {
				@Override
				public void onLocationLoaded(Location location) {
					finish();
					Intent newIntent = LocationActivity.createIntent(LocationSearchActivity.this, location);
					newIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					startActivity(newIntent);
				}
			});
		    taskManager.addTask(task);
		    task.execute(id);
		    
    	} else {
    		//are you lost?
    		finish();
    		return;
    	}
	}

	@Override
	protected void onPause() {
		super.onPause();
	    
	    //stop any tasks we were running
	    taskManager.abortTasks();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long rowId)
	{
		long id = locations[position].id; 
	    LoadLocation task = new LoadLocation(new OnLocationLoadedListener() {
			@Override
			public void onLocationLoaded(Location location) {
				finish();
				Intent newIntent = LocationActivity.createIntent(LocationSearchActivity.this, location);
				startActivity(newIntent);
			}
		});
	    taskManager.addTask(task);
	    task.execute(id);
	}
	
	private void btnShowOnMap_clicked() {
		Intent intent = CampusMapActivity.createIntent(this, searchQuery);
		startActivity(intent);
		finish();
	}
	
	private class SearchLocations extends AsyncTask<String, Void, LocationInfo[]> {
		
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(LocationSearchActivity.this);
			dialog.setTitle(null);
			dialog.setMessage("Searching...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.show();
		}
		
		@Override
		protected LocationInfo[] doInBackground(String... params) {
			String query = params[0];
			MobileDirectoryService service = new MobileDirectoryService();
			
			LocationNamesCollection names;
			try {
				names = service.searchLocations(query);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
			VersionsAdapter versions = new VersionsAdapter();
			versions.open();
			String version = versions.getVersion(VersionType.MAP_AREAS);
			versions.close();
			if (version == null || !version.equals(names.version)) {
				Log.e(C.TAG, "Search: data version has changed! Aborting");
				return null;
			}

			LocationInfo[] res = new LocationInfo[names.locations.length];
			
			LocationAdapter locationAdapter = new LocationAdapter();
			locationAdapter.open();
			
			for (int i = 0; i < res.length; i++) {
				res[i] = new LocationInfo(names.locations[i]);
				Location loc = locationAdapter.getLocation(res[i].id);
				if (loc != null) {
					res[i].description = loc.description;
				}
			}
			
			locationAdapter.close();
			
			return res;
		}

		@Override
		protected void onPostExecute(LocationInfo[] res) {
			dialog.dismiss();
			
			if (res == null) {
				finish();
				return;
			}
			
			locations = res;
			
			btnShowOnMap.setEnabled(res.length > 0);
			
			dataSet = new ArrayAdapter<LocationInfo>(LocationSearchActivity.this,
					R.layout.search_item, R.id.name, res) {
				
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View v = inflater.inflate(R.layout.search_item, null);
					
					TextView name = (TextView)v.findViewById(R.id.name);
					TextView info = (TextView)v.findViewById(R.id.description);
					
					name.setText(locations[position].name);
					info.setText(locations[position].description);
					
					return v;
				}
			};
			setListAdapter(dataSet);
		}
		
		@Override
		protected void onCancelled() {
			dialog.dismiss();
		}
		
	}

}
