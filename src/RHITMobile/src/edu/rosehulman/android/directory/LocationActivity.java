package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import edu.rosehulman.android.directory.db.DbIterator;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.model.Hyperlink;
import edu.rosehulman.android.directory.model.LightLocation;
import edu.rosehulman.android.directory.model.Location;

public class LocationActivity extends Activity {

	public static final String EXTRA_LOCATION = "LOCATION";
	
    private TaskManager taskManager;
    
    private Location location;
    
    private TextView name;
    private TextView description;
    
    private View linksGroup;
    private View childrenGroup;
    
    private ListView linksList;
    private ListView childrenList;
    
    private LightLocation[] children;
    
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
        linksList = (ListView)findViewById(R.id.links);
        childrenList = (ListView)findViewById(R.id.children);
        
        linksGroup = findViewById(R.id.linksGroup);
        childrenGroup = findViewById(R.id.childrenGroup);
        
        View btnShowOnMap = findViewById(R.id.btnShowOnMap);
        View btnDirections = findViewById(R.id.btnDirections);
        
        linksList.setOnItemClickListener(linkClickListener);
        childrenList.setOnItemClickListener(childClickListener);
        btnShowOnMap.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnShowOnMap_clicked();
			}
		});
        btnDirections.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnDirections_clicked();
			}
		});
        
        
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
    	
    	LoadLocationExtras loadExtras = new LoadLocationExtras();
    	taskManager.addTask(loadExtras);
    	loadExtras.execute(location);
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
    
    private void btnShowOnMap_clicked() {
    	Intent intent = CampusMapActivity.createIntent(this, location.id);
    	startActivity(intent);
    }
    
    private void btnDirections_clicked() {
    	final CharSequence[] locations = {"Inside", "Outside"};

    	new AlertDialog.Builder(this)
    		.setTitle("Where are you")
    		.setItems(locations, new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int item) {
	    	    	switch (item) {
	    	    	case 0: //inside
	    	    		new UITask<String, Long>() {

							@Override
							public void getInput(int attempt) {
								if (attempt > 0) {
									Toast.makeText(LocationActivity.this, "Location not found. Try again.", Toast.LENGTH_SHORT).show();
								}
								
								final EditText roomView = new EditText(LocationActivity.this);
			    	    		new AlertDialog.Builder(LocationActivity.this)
			    	    			.setTitle("What room are you near")
			    	    			.setView(roomView)
			    	    			.setPositiveButton("Get Directions", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											String room = roomView.getText().toString();
											setInput(room);
										}
									})
									.setOnCancelListener(new DialogInterface.OnCancelListener() {
										@Override
										public void onCancel(DialogInterface dialog) {
											cancel();
										}
									})
									.setCancelable(true)
			    	    			.show();
							}

							@Override
							public void processInput(String name) {
								LocationAdapter locationAdapter = new LocationAdapter();
								locationAdapter.open();
								
								long id = locationAdapter.findBuilding(name);
								
								locationAdapter.close();
								
								if (id >= 0)
									setResult(id);
							}

							@Override
							public void taskCompleted(Long res) {
								Intent intent = CampusMapActivity.createDirectionsIntent(LocationActivity.this, location.id, res);
								startActivity(intent);
							}

	    	    		}.start();
	    	    		
	    	    		break;
	    	    	case 1: //outside
	    	    		//TODO get location
	    	    		Toast.makeText(getApplicationContext(), "Outside directions are not yet implemented", Toast.LENGTH_SHORT).show();
	    	    		break;
	    	    	}
	    	    }
	    	})
	    	.show();
    }
    
    private void updateLinks() {
    	if (location.links.length > 0) {
	    	List<Map<String, ?>> data = new ArrayList<Map<String, ?>>();
	    	for (Hyperlink link : location.links) {
	    		Map<String, String> row = new HashMap<String, String>();
				row.put("name", link.name);
				data.add(row);
			}
	    	String[] from = new String[] {"name"};
	    	int[] to = new int[] {R.id.name};
	        linksList.setAdapter(new SimpleAdapter(this, data, R.layout.hyperlink_item, from, to));
	        linksGroup.setVisibility(View.VISIBLE);
    	} else {
    		linksGroup.setVisibility(View.GONE);
    	}
    }
    
    private void updateInnerLocations() {
    	if (children != null && children.length > 0) {
	    	List<Map<String, ?>> data = new ArrayList<Map<String, ?>>();
	    	for (LightLocation child : children) {
	    		Map<String, String> row = new HashMap<String, String>();
				row.put("name", child.name);
				data.add(row);
			}
	    	String[] from = new String[] {"name"};
	    	int[] to = new int[] {R.id.name};
	        childrenList.setAdapter(new SimpleAdapter(this, data, R.layout.inner_item, from, to));
	        childrenGroup.setVisibility(View.VISIBLE);
    	} else {
    		childrenGroup.setVisibility(View.GONE);
    	}
    }
    
    private void updateLocation() {
    	name.setText(location.name);
    	description.setText(location.description);
    	
    	updateLinks();
    	updateInnerLocations();
    }
    
    private OnItemClickListener linkClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Hyperlink link = location.links[position];
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(link.url));
			startActivity(intent);
		}
	};
	
    private OnItemClickListener childClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			LightLocation child = children[position];
			LoadChildLocation loadChild = new LoadChildLocation();
			taskManager.addTask(loadChild);
			loadChild.execute(child.id);
		}
	};
	
	private class LoadLocationExtras extends AsyncTask<Location, Void, Void> {

		private LightLocation[] children;
		
		@Override
		protected Void doInBackground(Location... params) {
			Location loc = params[0];
			
			LocationAdapter locationAdapter = new LocationAdapter();
			locationAdapter.open();
			
			DbIterator<LightLocation> locations = locationAdapter.getChildren(loc.id);
			children = new LightLocation[locations.getCount()];
			for (int i = 0; locations.hasNext(); i++) {
				children[i] = locations.getNext();
			}
			
			locationAdapter.close();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void res) {
			LocationActivity.this.children = children;
			
			updateLocation();
		}
		
	}
	
	private class LoadChildLocation extends AsyncTask<Long, Void, Location> {

		@Override
		protected Location doInBackground(Long... params) {
			
			long id = params[0];
					
			LocationAdapter locationAdapter = new LocationAdapter();
			locationAdapter.open();
			
			Location location = locationAdapter.getLocation(id);
			locationAdapter.loadAlternateNames(location);
			locationAdapter.loadHyperlinks(location);
			
			locationAdapter.close();
			return location;
		}
		
		@Override
		protected void onPostExecute(Location res) {
			Intent intent = createIntent(LocationActivity.this, res);
			LocationActivity.this.startActivity(intent);
		}
		
	}
	
}
