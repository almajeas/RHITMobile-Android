package edu.rosehulman.android.directory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import edu.rosehulman.android.directory.LoadLocation.OnLocationLoadedListener;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.util.ArrayUtil;

public class CampusToursOffCampusActivity extends SherlockListActivity {
	
    public static final String ACTION_TOUR = "edu.rosehulman.android.directory.intent.action.TOUR";

	public static final String EXTRA_TOUR_LOCATIONS = "TOUR_LOCATIONS";

	public static Intent createIntent(Context context, Location[] locations) {
		Intent intent = new Intent(context, CampusToursOffCampusActivity.class);
		intent.setAction(ACTION_TOUR);
		intent.putExtra(EXTRA_TOUR_LOCATIONS, locations);
		return intent;
	}
	
	private TaskManager taskManager;

	private Location[] locations;
	private ArrayAdapter<Location> dataSet;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tour_off_campus);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
		
		taskManager = new TaskManager();

    	Intent intent = getIntent();
    	
    	if (!ACTION_TOUR.equals(intent.getAction())) {
    		//are you lost?
    		finish();
    		return;
    	}
    
    	if (intent.hasExtra(EXTRA_TOUR_LOCATIONS)) {
    		Parcelable[] locs = intent.getParcelableArrayExtra(EXTRA_TOUR_LOCATIONS);
	    	locations = new Location[locs.length];
    		ArrayUtil.cast(locs, locations);
    		setLocations(locations);
    	} else {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle item selection
        switch (item.getItemId()) {
        case android.R.id.home:
        	finish();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
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
	
	private void setLocations(Location[] data) {
		locations = data;
		
		dataSet = new ArrayAdapter<Location>(CampusToursOffCampusActivity.this,
				R.layout.tour_off_campus_item, R.id.name, locations) {
			
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

}
