package edu.rosehulman.android.directory;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.rosehulman.android.directory.LoadLocation.OnLocationLoadedListener;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.model.LocationIdsResponse;
import edu.rosehulman.android.directory.model.TourTag;
import edu.rosehulman.android.directory.service.MobileDirectoryService;

public class CampusToursOffCampusActivity extends ListActivity {
	
    public static final String ACTION_TOUR = "edu.rosehulman.android.directory.intent.action.TOUR";

	public static final String EXTRA_TOUR_TAGS = "TOUR_TAGS";
	
	public static Intent createIntent(Context context, long[] tags) {
		Intent intent = new Intent(context, CampusToursOffCampusActivity.class);
		intent.setAction(ACTION_TOUR);
		intent.putExtra(EXTRA_TOUR_TAGS, tags);
		return intent;
	}
	
	public static Intent createIntent(Context context, TourTag[] tags) {
		long[] ids = new long[tags.length];
		for (int i = 0; i < tags.length; i++) {
			ids[i] = tags[i].id;
		}
		return createIntent(context, ids);
	}
	
	private TaskManager taskManager;

	private LocationInfo[] locations;
	private ArrayAdapter<LocationInfo> dataSet;
	
	private class LocationInfo {
		public long id;
		public String name;
		public String description;
		
		public LocationInfo(long id) {
			this.id = id;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tour_off_campus);
		
		taskManager = new TaskManager();

    	Intent intent = getIntent();
    	
    	if (!ACTION_TOUR.equals(intent.getAction())) {
    		//are you lost?
    		finish();
    		return;
    	}
    	
    	if (!intent.hasExtra(EXTRA_TOUR_TAGS)) {
    		finish();
    		return;
    	}
    	
    	long[] tagIds = intent.getLongArrayExtra(EXTRA_TOUR_TAGS);
    	
    	LoadTour task = new LoadTour(tagIds);
		taskManager.addTask(task);
		task.execute();
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
	    LoadLocation task = new LoadLocation(id, new OnLocationLoadedListener() {
			@Override
			public void onLocationLoaded(Location location) {
				Intent newIntent = LocationActivity.createIntent(CampusToursOffCampusActivity.this, location);
				startActivity(newIntent);
			}
		});
	    taskManager.addTask(task);
	    task.execute();
	}
	
	private class LoadTour extends AsyncTask<String, Void, LocationInfo[]> {
		
		ProgressDialog dialog;
		
		private long[] tagIds;

		public LoadTour(long[] tagIds) {
			this.tagIds = tagIds;
		}
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(CampusToursOffCampusActivity.this);
			dialog.setTitle(null);
			dialog.setMessage("Building Tour...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(true);
				}
			});
			dialog.show();
		}
		
		@Override
		protected LocationInfo[] doInBackground(String... params) {
			MobileDirectoryService service = new MobileDirectoryService();
			
			LocationIdsResponse response;
			do {
				try {
					response = service.getTour(tagIds);
				} catch (Exception e) {
					Log.e(C.TAG, "Failed to download offsite tour");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {}
					return null;
				}
			} while (response == null);
		
			LocationInfo[] res = new LocationInfo[response.ids.length];
			
			LocationAdapter locationAdapter = new LocationAdapter();
			locationAdapter.open();
			
			for (int i = 0; i < res.length; i++) {
				res[i] = new LocationInfo(response.ids[i]);
				Location loc = locationAdapter.getLocation(res[i].id);
				if (loc != null) {
					res[i].name = loc.name;
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
			
			dataSet = new ArrayAdapter<LocationInfo>(CampusToursOffCampusActivity.this,
					R.layout.tour_off_campus_item, R.id.name, res) {
				
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View v = inflater.inflate(R.layout.tour_off_campus_item, null);
					
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
