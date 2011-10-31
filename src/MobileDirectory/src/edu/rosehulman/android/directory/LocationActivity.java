package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import edu.rosehulman.android.directory.model.Hyperlink;
import edu.rosehulman.android.directory.model.Location;

public class LocationActivity extends Activity {

	public static final String EXTRA_LOCATION = "LOCATION";
	
    private TaskManager taskManager;
    
    private Location location;
    
    private TextView name;
    private TextView description;
    
    private ListView links;
    
    public static Intent createIntent(Context context, Location location) {
		Intent intent = new Intent(context, LocationActivity.class);
		intent.putExtra(LocationActivity.EXTRA_LOCATION, location);
		return intent;
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.location);
        
        taskManager = new TaskManager();
        
        name = (TextView)findViewById(R.id.name);
        description = (TextView)findViewById(R.id.description);
        links = (ListView)findViewById(R.id.links);
        
        location = getIntent().getParcelableExtra(EXTRA_LOCATION);
        
        if (savedInstanceState == null) {
        	   
	    } else {
	    	//restore state
	    }

    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	updateLocation();
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

    	List<Map<String, ?>> data = new ArrayList<Map<String, ?>>();
    	for (Hyperlink link : location.links) {
    		Map<String, String> row = new HashMap<String, String>();
			row.put("name", link.name);
			row.put("url", link.url);
			data.add(row);
		}
    	String[] from = new String[] {"name"};
    	int[] to = new int[] {R.id.name};
        links.setAdapter(new SimpleAdapter(this, data, R.layout.hyperlink_item, from, to));
    }
}
