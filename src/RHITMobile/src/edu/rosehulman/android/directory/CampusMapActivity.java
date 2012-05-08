package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import edu.rosehulman.android.directory.db.DbIterator;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.db.VersionsAdapter;
import edu.rosehulman.android.directory.maps.BuildingOverlayLayer;
import edu.rosehulman.android.directory.maps.OverlayManager;
import edu.rosehulman.android.directory.maps.POILayer;
import edu.rosehulman.android.directory.maps.TextOverlayLayer;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.model.LocationCollection;
import edu.rosehulman.android.directory.model.VersionType;
import edu.rosehulman.android.directory.service.MobileDirectoryService;

/**
 * Main entry point into MobileDirectory
 */
public class CampusMapActivity extends MapActivity {
	
    private static final String SELECTED_ID = "SelectedId";

    public static final String EXTRA_IS_INTERNAL = "IS_INTERNAL";
	public static final String EXTRA_BUILDING_ID = "BUILDING_ID";

	private static final int REQUEST_STARTUP_CODE = 4;

	public static Intent createIntent(Context context, long buildingId) {
		Intent intent = new Intent(context, CampusMapActivity.class);
		intent.putExtra(EXTRA_IS_INTERNAL, true);
		intent.putExtra(EXTRA_BUILDING_ID, buildingId);
		return intent;
	}
	
	private BetaManagerManager betaManager;

    private MapView mapView;
    
    private LocationManager locationManager;
    private LocationListener locationListener;

    private OverlayManager overlayManager;
    private POILayer poiLayer;
    private BuildingOverlayLayer buildingLayer;
    private TextOverlayLayer textLayer;
    private EventOverlay eventLayer;
    private MyLocationOverlay myLocation;
    
    private TaskManager taskManager;
    
    private Bundle savedInstanceState;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.main);
        
        taskManager = new TaskManager();
        betaManager = new BetaManagerManager(this);
        
        mapView = (MapView)findViewById(R.id.mapview);
        
        overlayManager = new OverlayManager();
        myLocation = new MyLocationOverlay(this, mapView);
        eventLayer = new EventOverlay();
        
        if (savedInstanceState == null) {
        	
        	Intent intent = getIntent();
		    if (!intent.getBooleanExtra(EXTRA_IS_INTERNAL, false) && betaManager.hasBetaManager() && betaManager.isBetaEnabled()) {
		       	if (betaManager.isBetaRegistered()) {
		       		Intent betaIntent = betaManager.getBetaIntent(BetaManagerManager.ACTION_SHOW_STARTUP); 
		       		startActivityForResult(betaIntent, REQUEST_STARTUP_CODE);	
		       	} else {
		       		betaManager.launchBetaActivity(BetaManagerManager.ACTION_SHOW_REGISTER);
		       	}
	        }

	        mapView.setSatellite(true);
	        
	        //center the map
	        MapController controller = mapView.getController();
	        GeoPoint center = new GeoPoint(39483760, -87325929);
	        controller.setCenter(center);
	        controller.zoomToSpan(6241, 13894);
	        
	    } else {
	    	this.savedInstanceState = savedInstanceState;
	    	
	    	//restore state
	    }

        mapView.setBuiltInZoomControls(true);
        
        rebuildOverlays();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode != REQUEST_STARTUP_CODE)
    		return;
    	
    	switch (resultCode) {
    		case Activity.RESULT_CANCELED:
    			//The user declined an update, exit
    			finish();
    			break;
    		case Activity.RESULT_OK:
    			//We were up to date, continue on happily
    			break;	
    	}
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
		LoadLocations loadLocations = new LoadLocations();
        taskManager.addTask(loadLocations);
        loadLocations.execute();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle bundle) {
    	super.onSaveInstanceState(bundle);
    	//TODO save our state
    	if (buildingLayer != null) {
    		bundle.putLong(SELECTED_ID, getFocusedLocation());
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
        //Start the location services
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(android.location.Location location) {
				Log.v(C.TAG, location.toString());
			}

			@Override
			public void onProviderDisabled(String provider) {
				//TODO yell at user
			}

			@Override
			public void onProviderEnabled(String provider) {
				//TODO stop yelling at user
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				if (status != LocationProvider.AVAILABLE) {
					//TODO handle appropriately
					//TEMPORARILY_UNAVAILABLE
					//AVAILABLE
				}
			}
		};
        
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        
        //enable the location overlay
        myLocation.enableCompass();
        myLocation.enableMyLocation();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	locationManager.removeUpdates(locationListener);
    	
        //disable the location overlay
        myLocation.disableCompass();
        myLocation.disableMyLocation();
        
        //stop any tasks we were running
        taskManager.abortTasks();
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.setGroupVisible(R.id.beta_channel, betaManager.hasBetaManager());
    	menu.setGroupEnabled(R.id.location_items, myLocation.getMyLocation() != null);
        return true;
    }
    
    private void showTopLocations() {
    	TopLocations task = new TopLocations();
    	taskManager.addTask(task);
    	task.execute();
    }
    
    private void focusLocation(long id, boolean animate) {
    	if (!buildingLayer.focus(id, animate)) {
    		poiLayer.focus(id, animate);
    	}
    }
    
    private long getFocusedLocation() {
    	long id = buildingLayer.getSelectedBuilding();
    	
    	if (id >= 0)
    		return id;
    	
    	return poiLayer.getFocusId();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle item selection
        switch (item.getItemId()) {
        case R.id.beta_manager:
            betaManager.launchBetaActivity(BetaManagerManager.ACTION_SHOW_BETA_MANAGER);
            return true;
        case R.id.location:
        	mapView.getController().animateTo(myLocation.getMyLocation());
        	return true;
        case R.id.top_level:
        	showTopLocations();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	@Override
	protected boolean isRouteDisplayed() {
		//FIXME update when we start displaying route information
		return false;
	}
	
    private void rebuildOverlays() {
    	List<Overlay> overlays = mapView.getOverlays();
    	overlays.clear();
    	
    	overlays.add(eventLayer);
    	overlays.add(myLocation);
    	
    	if (buildingLayer != null) {
    		overlays.add(buildingLayer);
    		overlayManager.addOverlay(buildingLayer);
    	}
    	
    	if (textLayer != null) {
    		overlays.add(textLayer);
    	}

    	if (poiLayer != null) {
    		overlays.add(poiLayer);
    		overlayManager.addOverlay(poiLayer);
    	}
    	
    	mapView.invalidate();
    }
    
    private class EventOverlay extends Overlay {
    	
    	@Override
    	public boolean onTap(GeoPoint p, MapView mapView) {
    		//tap not handled by any other overlay
    		if (buildingLayer != null) {
    			buildingLayer.focus(-1, false);
    		}
    		
    		if (poiLayer != null) {
    			poiLayer.setFocus(null);
    		}
    		
    		return true;
    	}
    }
    
    private class TopLocations extends AsyncTask<Void, Void, Void> {
    	private int[] ids;
		private String[] names;
    	
		@Override
		protected Void doInBackground(Void... params) {
			LocationAdapter locationAdapter = new LocationAdapter();
			locationAdapter.open();
			
			Cursor cursor = locationAdapter.getQuickListCursor();
			
			int iId = cursor.getColumnIndex(LocationAdapter.KEY_ID);
			int iName = cursor.getColumnIndex(LocationAdapter.KEY_NAME);
			
			ids = new int[cursor.getCount()];
			names = new String[cursor.getCount()];
			
			for (int i = 0; cursor.moveToNext(); i++) {
				ids[i] = cursor.getInt(iId);
				names[i] = cursor.getString(iName);
			}
			
			locationAdapter.close();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void res) {
	    	AlertDialog dialog = new AlertDialog.Builder(CampusMapActivity.this)
	    		.setTitle("Top Locations")
	    		.setItems(names, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						focusLocation(ids[which], true);
					}
				})
				.create();
	    	dialog.show();
		}
    	
    }
    
    private static boolean topLocationsRefreshed = false;
    private static boolean innerLocationsRefreshed = false;
	private class LoadLocations extends AsyncTask<Void, Void, Void> {
		
	    private POILayer poiLayer;
	    private BuildingOverlayLayer buildingLayer;
	    private TextOverlayLayer textLayer;
	    
	    private String newVersion;

		public LoadLocations() {
		}
		
	    private void generateBuildings() {
	    	BuildingOverlayLayer.initializeCache();
	    	
	    	BuildingOverlayLayer buildings = new BuildingOverlayLayer(mapView);
	    	this.buildingLayer = buildings;
	    }
	    
	    private void generatePOI() {
	    	Drawable marker = getResources().getDrawable(R.drawable.map_marker);
	    	POILayer poi = new POILayer(marker, mapView);

	    	LocationAdapter buildingAdapter = new LocationAdapter();
	    	buildingAdapter.open();
	    	
	    	DbIterator<Location> iterator = buildingAdapter.getPOIIterator();
	    	while (iterator.hasNext()) {
	    		Location location = iterator.getNext();
	    		poi.add(location);
	    	}
	    	
	    	buildingAdapter.close();
	    	
	    	this.poiLayer = poi;
	    }

		private void generateText() {
			TextOverlayLayer.initializeCache();
			
	        textLayer = new TextOverlayLayer();
		}
		
		private void buildLayers() {
			generateText();
			publishProgress();
			generateBuildings();
			publishProgress();
			generatePOI();	
		}
		
		@Override
		protected Void doInBackground(Void... args) {
			if (isCancelled()) {
				return null;
			}
			
			if (topLocationsRefreshed) {
				//skip update
				buildLayers();
				return null;
			}

			//check for updated map areas
			VersionsAdapter versionsAdapter = new VersionsAdapter();
			versionsAdapter.open();
	        
			MobileDirectoryService service = new MobileDirectoryService();
	    	String version = versionsAdapter.getVersion(VersionType.MAP_AREAS);
	    	versionsAdapter.close();
			
	        LocationCollection collection = null;
	        try {
	        	collection = service.getTopLocationData(version);
			} catch (Exception e) {
				Log.e(C.TAG, "Failed to download new map areas", e);
				//just use our old data, it is likely up to date
				buildLayers();
				return null;
			}
			if (isCancelled()) {
				return null;
			}
			
			if (collection == null) {
				//data was up to date
				buildLayers();
				return null;
			}
			
			//remember our top level ids
			newVersion = collection.version;

			//replace the building data with the new data
	        LocationAdapter buildingAdapter = new LocationAdapter();
	        buildingAdapter.open();
	        buildingAdapter.replaceLocations(collection.mapAreas);
	        buildingAdapter.close();
	        
	        versionsAdapter.open();
	        versionsAdapter.setVersion(VersionType.MAP_AREAS, collection.version);
	        versionsAdapter.close();
	        if (isCancelled()) {
				return null;
			}
	        
	        buildLayers();
	        return null;
		}
		
		@Override
		protected void onPreExecute() {
			CampusMapActivity.this.setProgressBarIndeterminateVisibility(true);
			CampusMapActivity.this.setProgressBarVisibility(true);
		}
		
		private void updateOverlays() {
			CampusMapActivity.this.poiLayer = poiLayer;
			CampusMapActivity.this.buildingLayer = buildingLayer;
			CampusMapActivity.this.textLayer = textLayer;
			rebuildOverlays();
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
	    	updateOverlays();
		}
		
		@Override
		protected void onPostExecute(Void res) {
			//add the overlay to the map;
			updateOverlays();
			
			topLocationsRefreshed = true;
			
			Intent intent = getIntent();
			
	    	if (savedInstanceState != null) {
	    		focusLocation(savedInstanceState.getLong(SELECTED_ID), false);
	    	} else if (intent.hasExtra(EXTRA_BUILDING_ID)) {
	    		long id = intent.getLongExtra(EXTRA_BUILDING_ID, -1);
	    		focusLocation(id, false);
	    	}

			setProgressBarIndeterminateVisibility(false);
			if (!innerLocationsRefreshed) {
	    		LoadInnerLocations task = new LoadInnerLocations(newVersion);
	    		taskManager.addTask(task);
	    		task.execute();
	    	} else {
	    		setProgressBarVisibility(false);
	    	}
		}
		
		@Override
		protected void onCancelled() {
    		setProgressBarIndeterminateVisibility(false);
    		setProgressBarVisibility(false);
		}
		
	}
	
	private class LoadInnerLocations extends AsyncTask<Void, Integer, Void> {
		
		private Set<Long> topIds;
		
		private int totalItems;
		private List<Long> ids;
		private String newVersion;
		
		public LoadInnerLocations(String version) {
			newVersion = version;
		}
		
		//TODO use these methods to prioritize which locations we load
		//TODO do not tie this asynctask to this activity (or restart it later if needed)
		public void requestLocation(long id) {
			synchronized (ids) {
				if (ids.remove(id)) {
					ids.add(id);
				}
			}
		}
		
		public boolean hasLocation(long id) {
			synchronized (ids) {
				return !ids.contains(id);
			}
		}
		
		private long getNextId() {
			synchronized(ids) {
				if (ids.isEmpty())
					return -1;
				return ids.remove(ids.size() - 1);
			}
		}
		
		@Override
		protected void onPreExecute() {
			setProgress(0);
		}

		@Override
		protected Void doInBackground(Void... params) {
	        
			MobileDirectoryService service = new MobileDirectoryService();
	        LocationAdapter buildingAdapter = new LocationAdapter();
	        buildingAdapter.open();
	        
	        //Get the ids to load
	        this.ids = new ArrayList<Long>();
			topIds = new HashSet<Long>();
	        long[] ids = buildingAdapter.getUnloadedParents();
	        for (long id : ids) {
	        	this.ids.add(id);
	        	this.topIds.add(id);
	        }
			totalItems = ids.length;
			publishProgress(0);
			
			if (ids.length == 0) {
				return null;
			}
	        
	        int processed = 0;
			
	        try {
				for (long id = getNextId(); id >= 0; id = getNextId()) {
					if (isCancelled()) {
						return null;
					}
					
					LocationCollection collection = null;
			        try {
			        	collection = service.getLocationData(id, null);
					} catch (Exception e) {
						Log.e(C.TAG, "Failed to download locations within a parent", e);
						synchronized(this.ids) {
							this.ids.add(0, id);
							totalItems++;
						}
						continue;
					}

			        buildingAdapter.startTransaction();
			        for (Location location : collection.mapAreas) {
			        	if (topIds.contains(location.id))
			        		continue;
			        	
			        	buildingAdapter.addLocation(location);
			        }
			        newVersion = collection.version;
			        buildingAdapter.setChildrenLoaded(id, true);
			        
					buildingAdapter.commitTransaction();
		        	buildingAdapter.finishTransaction();
			        
			        processed++;
			        publishProgress(processed);
		        }
				
	        } finally {
		        buildingAdapter.close();
	        }
			
			//mark our data set as up to date
			VersionsAdapter versionsAdapter = new VersionsAdapter();
	        versionsAdapter.open();
	        versionsAdapter.setVersion(VersionType.MAP_AREAS, newVersion);
	        versionsAdapter.close();
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			if (totalItems == 0) {
				setProgress(0);
			} else {
				int progress = values[0] * 10000 / totalItems;
				setProgress(progress);
			}
		}
		
		@Override
		protected void onPostExecute(Void res) {
			setProgressBarVisibility(false);
			innerLocationsRefreshed = true;
		}
		
		@Override
		protected void onCancelled() {
			setProgressBarVisibility(false);
		}
		
	}
	
}
