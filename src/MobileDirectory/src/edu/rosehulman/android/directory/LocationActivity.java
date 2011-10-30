package edu.rosehulman.android.directory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.model.Location;

public class LocationActivity extends Activity {

	public static final String EXTRA_LOCATION_ID = "LOCATION_ID";
	
    private TaskManager taskManager;
    
    private Location location;
    
    private TextView name;
    private TextView description;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.location);
        
        taskManager = new TaskManager();
        
        name = (TextView)findViewById(R.id.name);
        description = (TextView)findViewById(R.id.description);
        
        if (savedInstanceState == null) {
        	   
	    } else {
	    	//restore state
	    }

    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	if (location == null) {
    		Intent intent = getIntent();
            long id = intent.getLongExtra(EXTRA_LOCATION_ID, -1);
    		LoadLocation loadLocation = new LoadLocation();
    		taskManager.addTask(loadLocation);
    		loadLocation.execute(id);
    	} else {
    		updateLocation();
    	}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle bundle) {
    	super.onSaveInstanceState(bundle);
    	//TODO save our state
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.location, menu);
        return true;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
        
        //stop any tasks we were running
        taskManager.abortTasks();
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle item selection
        switch (item.getItemId()) {
        
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void updateLocation() {
    	name.setText(location.name);
    	description.setText(location.description);
    }
    
    private class LoadLocation extends AsyncTask<Long, Void, Location> {
    	
    	private ProgressDialog status;
    	
    	@Override
    	protected void onPreExecute() {        	
    		String title = "Loading...";
    		String message = null;
        	status = ProgressDialog.show(LocationActivity.this, title, message, false);
    	}

		@Override
		protected Location doInBackground(Long... args) {
			
			LocationAdapter locationAdapter = new LocationAdapter();
			locationAdapter.open();
			Location location = locationAdapter.getLocation(args[0]);
			//locationAdapter.loadMapArea(location, false);
			locationAdapter.close();
			
			return location;
		}
    	

		@Override
		protected void onPostExecute(Location res) {
			LocationActivity.this.location = res;
			updateLocation();
			
			status.dismiss();
		}
		
    }
}
