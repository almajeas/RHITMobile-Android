package edu.rosehulman.android.directory;

import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
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
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import edu.rosehulman.android.directory.IDataUpdateService.AsyncRequest;
import edu.rosehulman.android.directory.ServiceManager.ServiceRunnable;
import edu.rosehulman.android.directory.db.DbIterator;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.db.VersionsAdapter;
import edu.rosehulman.android.directory.maps.BuildingOverlayLayer;
import edu.rosehulman.android.directory.maps.BuildingOverlayLayer.OnBuildingSelectedListener;
import edu.rosehulman.android.directory.maps.LocationSearchLayer;
import edu.rosehulman.android.directory.maps.OverlayManager;
import edu.rosehulman.android.directory.maps.POILayer;
import edu.rosehulman.android.directory.maps.TextOverlayLayer;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.model.LocationNamesCollection;
import edu.rosehulman.android.directory.model.VersionType;
import edu.rosehulman.android.directory.service.MobileDirectoryService;

/**
 * Main entry point into MobileDirectory
 */
public class CampusMapActivity extends MapActivity {
	
    private static final String SELECTED_ID = "SelectedId";
    
    public static final String ACTION_DIRECTIONS = "edu.rosehulman.android.directory.intent.action.DIRECTIONS";

	public static final String EXTRA_BUILDING_ID = "BUILDING_ID";
	public static final String EXTRA_WAYPOINTS = "WAYPOINTS";

	public static Intent createIntent(Context context) {
		return new Intent(context, CampusMapActivity.class);
	}

	public static Intent createIntent(Context context, long buildingId) {
		Intent intent = createIntent(context);
		intent.putExtra(EXTRA_BUILDING_ID, buildingId);
		return intent;
	}
	
	public static Intent createIntent(Context context, String query) {
		Intent intent = createIntent(context);
		intent.setAction(Intent.ACTION_SEARCH);
		intent.putExtra(SearchManager.QUERY, query);
		return intent;
	}
	
	public static Intent createDirectionsIntent(Context context, long... ids) {
		Intent intent = createIntent(context);
		intent.setAction(ACTION_DIRECTIONS);
		intent.putExtra(EXTRA_WAYPOINTS, ids);
		return intent;
	}

    private MapView mapView;
    
    private String searchQuery;
    
    private LocationManager locationManager;
    private LocationListener locationListener;

    private OverlayManager overlayManager;
    private LocationSearchLayer searchOverlay;
    private POILayer poiLayer;
    private BuildingOverlayLayer buildingLayer;
    private TextOverlayLayer textLayer;
    private EventOverlay eventLayer;
    private MyLocationOverlay myLocation;
    
    private TaskManager taskManager;
    
    private ServiceManager<IDataUpdateService> updateService;
    
    private Bundle savedInstanceState;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campus_map);
        
        taskManager = new TaskManager();
        
        mapView = (MapView)findViewById(R.id.mapview);
        
        overlayManager = new OverlayManager();
        myLocation = new MyLocationOverlay(this, mapView);
        eventLayer = new EventOverlay();
        
        if (savedInstanceState == null) {
        	
        	Intent intent = getIntent();
        	
        	if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
    			searchQuery = intent.getStringExtra(SearchManager.QUERY);
    			SearchLocations task = new SearchLocations();
    			taskManager.addTask(task);
    			task.execute(searchQuery);
    			setTitle("Search: " + searchQuery);
    		
        	} else if (ACTION_DIRECTIONS.equals(intent.getAction())) {
        		StringBuilder idMsg = new StringBuilder();
        		long[] ids = intent.getLongArrayExtra(EXTRA_WAYPOINTS);
        		for (long id : ids) {
        			idMsg.append(id);
        			idMsg.append(' ');
        		}
        		
    			Toast.makeText(this, "TODO: show directions for location ids: " + idMsg.toString(), Toast.LENGTH_LONG).show();
    			
    			LoadDirections task = new LoadDirections(ids);
    			task.execute();
    		}

	        mapView.setSatellite(true);
	        
	        //center the map
	        MapController controller = mapView.getController();
	        GeoPoint center = new GeoPoint(39483760, -87325929);
	        controller.setCenter(center);
	        controller.zoomToSpan(5000, 10000);
	        
	    } else {
	    	this.savedInstanceState = savedInstanceState;
	    	
	    	//restore state
	    }

        mapView.setBuiltInZoomControls(true);
        
        rebuildOverlays();
        
		updateService = new ServiceManager<IDataUpdateService>(getApplicationContext(),
				DataUpdateService.createIntent(getApplicationContext()));
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	updateService.run(new ServiceRunnable<IDataUpdateService>() {

			@Override
			public void run(IDataUpdateService service) {
				final ProgressDialog dialog = new ProgressDialog(CampusMapActivity.this);
				service.requestTopLocations(new AsyncRequest() {
					
					@Override
					public void onQueued(Runnable cancelCallback) {
						dialog.setTitle("");
						dialog.setMessage("Loading...");
						dialog.setIndeterminate(true);
						dialog.show();
					}
					
					@Override
					public void onCompleted() {
						if (dialog.isShowing()) {
							dialog.cancel();
						}
						
						generateText();
						
						//don't generate buildings or POI if we are searching
						if (searchQuery != null) {
							return;
						}
						
						generateBuildings();
						generatePOI();
						rebuildOverlays();
						
						Intent intent = getIntent();
						
						//set a selected location
				    	if (savedInstanceState != null) {
				    		focusLocation(savedInstanceState.getLong(SELECTED_ID), false);
				    	} else if (intent.hasExtra(EXTRA_BUILDING_ID)) {
				    		long id = intent.getLongExtra(EXTRA_BUILDING_ID, -1);
				    		focusLocation(id, false);
				    	}
					}
				});
			}
		});
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
    	menu.setGroupEnabled(R.id.location_items, myLocation.getMyLocation() != null);
        return true;
    }
    
    @Override
    public boolean onSearchRequested() {
    	if (updateService.get().isUpdating())
    		return false;
    	
    	return super.onSearchRequested();
    }
    
    private void showTopLocations() {
    	TopLocations task = new TopLocations();
    	taskManager.addTask(task);
    	task.execute();
    }
    
    private void focusLocation(final long id, boolean animate) {
    	if (buildingLayer.focus(id, animate)) {
    		updateService.run(new ServiceRunnable<IDataUpdateService>() {
				@Override
				public void run(IDataUpdateService service) {
					service.requestInnerLocation(id, null);
				}
			});
    	} else {
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
        case R.id.location:
        	mapView.getController().animateTo(myLocation.getMyLocation());
        	return true;
        case R.id.top_level:
        	showTopLocations();
        	return true;
        case R.id.search:
        	onSearchRequested();
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
    	
    	if (searchOverlay != null) {
    		overlays.add(searchOverlay);
    		overlayManager.addOverlay(searchOverlay);
    	}
    	
    	//Remove any old overlays
    	overlayManager.prune(overlays);
    	
    	mapView.invalidate();
    }
    
    private OnBuildingSelectedListener buildingSelectedListener = new OnBuildingSelectedListener() {

		@Override
		public void onSelect(Location location) {
			final long id = location.id;
			
			//request this location to be loaded ASAP
    		updateService.run(new ServiceRunnable<IDataUpdateService>() {
				@Override
				public void run(IDataUpdateService service) {
					service.requestInnerLocation(id, null);
				}
			});
		}
		
		@Override
		public void onTap(final Location location) {
			
			updateService.run(new ServiceRunnable<IDataUpdateService>() {
				@Override
				public void run(IDataUpdateService service) {
					final ProgressDialog dialog = new ProgressDialog(CampusMapActivity.this);
					
					service.requestInnerLocation(location.id, new AsyncRequest() {
						
						@Override
						public void onQueued(final Runnable cancelCallback) {
							dialog.setTitle("");
							dialog.setMessage("Loading...");
							dialog.setIndeterminate(true);
							dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel(DialogInterface dialog) {
									cancelCallback.run();
								}
							});
							dialog.show();
						}
						
						@Override
						public void onCompleted() {
							if (dialog.isShowing()) {
								dialog.cancel();
							}
							
							//populate the location
							PopulateLocation task = new PopulateLocation(new Runnable() {
								
								@Override
								public void run() {
									//run the activity
									Context context = mapView.getContext();
									context.startActivity(LocationActivity.createIntent(context, location));
								}
							});
							taskManager.addTask(task);
							task.execute(location);
						}
					});
				}
			});
		}
    };
    
    private class EventOverlay extends Overlay {
    	
    	@Override
    	public boolean onTap(GeoPoint p, MapView mapView) {
    		//tap not handled by any other overlay
    		overlayManager.clearSelection();
    		
    		return true;
    	}
    }
    

    

    private void generateBuildings() {
    	BuildingOverlayLayer.initializeCache();
    	
    	BuildingOverlayLayer buildings = new BuildingOverlayLayer(mapView, buildingSelectedListener);
    	buildingLayer = buildings;
    }
    
    private void generatePOI() {
    	Drawable marker = getResources().getDrawable(R.drawable.map_marker);
    	POILayer poi = new POILayer(marker, mapView, taskManager);

    	LocationAdapter buildingAdapter = new LocationAdapter();
    	buildingAdapter.open();
    	
    	DbIterator<Location> iterator = buildingAdapter.getPOIIterator();
    	while (iterator.hasNext()) {
    		Location location = iterator.getNext();
    		poi.add(location);
    	}
    	
    	buildingAdapter.close();
    	
    	poiLayer = poi;
    }

	private void generateText() {
		TextOverlayLayer.initializeCache();
		
        textLayer = new TextOverlayLayer();
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
    
	private class SearchLocations extends AsyncTask<String, Void, LocationSearchLayer> {
		
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(CampusMapActivity.this);
			dialog.setTitle(null);
			dialog.setMessage("Searching...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.show();
		}
		
		@Override
		protected LocationSearchLayer doInBackground(String... params) {
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
			
	    	Drawable marker = getResources().getDrawable(R.drawable.map_search_marker);
			LocationSearchLayer overlay = new LocationSearchLayer(marker, mapView);
			LocationAdapter locationAdapter = new LocationAdapter();
			locationAdapter.open();
			for (int i = 0; i < names.locations.length; i++) {
				Location loc = locationAdapter.getLocation(names.locations[i].id);
				
				if (loc == null) {
					//Just ignore locations we do not yet have
					continue;
				}
				
				overlay.add(loc);
			}
			locationAdapter.close();
			
			return overlay;
		}

		@Override
		protected void onPostExecute(LocationSearchLayer res) {
			dialog.dismiss();
			CampusMapActivity.this.searchOverlay = res;
			rebuildOverlays();
		}
		
		@Override
		protected void onCancelled() {
			dialog.dismiss();
		}
		
	}

	private class LoadDirections extends AsyncTask<Void, Void, Void> {
		
		private long[] ids;
		private ProgressDialog dialog;
		
		public LoadDirections(long[] ids) {
			this.ids = ids;
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(CampusMapActivity.this);
			dialog.setTitle(null);
			dialog.setMessage("Getting Directions...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) { }
			return null;
		}

		@Override
		protected void onPostExecute(Void res) {
			dialog.dismiss();
		
		}
		
		@Override
		protected void onCancelled() {
			dialog.dismiss();
		}
		
	}
	
	
}
