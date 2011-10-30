package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import edu.rosehulman.android.directory.db.DbIterator;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.db.VersionsAdapter;
import edu.rosehulman.android.directory.maps.BoundingMapArea;
import edu.rosehulman.android.directory.maps.BuildingOverlayLayer;
import edu.rosehulman.android.directory.maps.OverlayManager;
import edu.rosehulman.android.directory.maps.POILayer;
import edu.rosehulman.android.directory.maps.TextOverlay;
import edu.rosehulman.android.directory.maps.TextOverlayLayer;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.model.LocationCollection;
import edu.rosehulman.android.directory.model.VersionType;
import edu.rosehulman.android.directory.service.MobileDirectoryService;

/**
 * Main entry point into MobileDirectory
 */
public class MainActivity extends MapActivity {
	
    private static final String BUILDING_SELECTED_ID = "BuildingSelectedId";

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
        setContentView(R.layout.main);
        
        taskManager = new TaskManager();
        betaManager = new BetaManagerManager(this);
        
        mapView = (MapView)findViewById(R.id.mapview);
        
        overlayManager = new OverlayManager();
        myLocation = new MyLocationOverlay(this, mapView);
        eventLayer = new EventOverlay();
        
        if (savedInstanceState == null) {
        	
		    if (betaManager.hasBetaManager() && betaManager.isBetaEnabled()) {
		       	if (betaManager.isBetaRegistered()) {
		       		betaManager.launchBetaActivity(BetaManagerManager.ACTION_SHOW_STARTUP);	
		       	} else {
		       		betaManager.launchBetaActivity(BetaManagerManager.ACTION_SHOW_REGISTER);
		       	}
	        }

	        mapView.setSatellite(true);
	        
	        //center the map
	        GeoPoint center = new GeoPoint(39483760, -87325929);
	        mapView.getController().setCenter(center);
	        mapView.getController().zoomToSpan(6241, 13894);    
	    } else {
	    	this.savedInstanceState = savedInstanceState;
	    	
	    	//restore state
	    }

        mapView.setBuiltInZoomControls(true);
        
        rebuildOverlays();
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	if (textLayer == null) {
    		LoadOverlays loadOverlays = new LoadOverlays(savedInstanceState == null);
            taskManager.addTask(loadOverlays);
            loadOverlays.execute();
    	}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle bundle) {
    	super.onSaveInstanceState(bundle);
    	//TODO save our state
    	if (buildingLayer != null) {
    		bundle.putLong(BUILDING_SELECTED_ID, buildingLayer.getSelectedBuilding());
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
    			buildingLayer.setSelectedBuilding(-1);
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
	    	AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
	    		.setTitle("Top Locations")
	    		.setItems(names, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!buildingLayer.setSelectedBuilding(ids[which])) {
							poiLayer.setFocus(ids[which]);	
						}
						
					}
				})
				.create();
	    	dialog.show();
		}
    	
    }
    
	private class LoadOverlays extends AsyncTask<Void, Void, Void> {
		
		private boolean refreshData;
		
	    private POILayer poiLayer;
	    private BuildingOverlayLayer buildingLayer;
	    private TextOverlayLayer textLayer;

		public LoadOverlays(boolean refreshData) {
			this.refreshData = refreshData;
		}
		
	    private void generateBuildings() {
	    	BuildingOverlayLayer buildings = new BuildingOverlayLayer(mapView);
	    	
	    	LocationAdapter buildingAdapter = new LocationAdapter();
	    	buildingAdapter.open();
	    	
	    	DbIterator<Location> iterator = buildingAdapter.getBuildingIterator();
	    	while (iterator.hasNext()) {
	    		Location area = iterator.getNext();
	    		buildingAdapter.loadMapArea(area, true);
	    		buildings.addMapArea(area);
	    	}
	    	
	    	buildingAdapter.close();

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
			//get our db
	        LocationAdapter buildingAdapter = new LocationAdapter();
	        textLayer = new TextOverlayLayer();
	        buildingAdapter.open();
	        
	        //build out text overlays
	        Cursor buildingOverlays = buildingAdapter.getBuildingOverlayCursor(false);
	        int iId = buildingOverlays.getColumnIndex("_Id");
	        int iName = buildingOverlays.getColumnIndex("Name");
	        int iLat = buildingOverlays.getColumnIndex("CenterLat");
	        int iLon = buildingOverlays.getColumnIndex("CenterLon");
	        int iMinZoomLevel = buildingOverlays.getColumnIndex("MinZoomLevel");
	        while (buildingOverlays.moveToNext()) {
	        	String name = buildingOverlays.getString(iName);
	        	int minZoomLevel = buildingOverlays.getInt(iMinZoomLevel);
	        	GeoPoint pt = new GeoPoint(buildingOverlays.getInt(iLat), buildingOverlays.getInt(iLon));
	        	textLayer.addOverlay(new TextOverlay(pt, name, minZoomLevel));
	        } while (buildingOverlays.moveToNext());
	        buildingOverlays.close();
	        
	        //add our building obstacles to the text layer
	        buildingOverlays = buildingAdapter.getBuildingOverlayCursor(true);
	        iId = buildingOverlays.getColumnIndex("_Id");
	        while (buildingOverlays.moveToNext()) {
	        	int buildingId = buildingOverlays.getInt(iId);
	        	Cursor buildingPoints = buildingAdapter.getBuildingCornersCursor(buildingId);
	            iLat = buildingPoints.getColumnIndex("Lat");
	            iLon = buildingPoints.getColumnIndex("Lon");
	        	List<GeoPoint> pts = new ArrayList<GeoPoint>(buildingPoints.getCount());
	        	while (buildingPoints.moveToNext()) {
	        		int lat = buildingPoints.getInt(iLat);
	        		int lon = buildingPoints.getInt(iLon);
	        		pts.add(new GeoPoint(lat, lon));
	        	}
	        	buildingPoints.close();
	        	BoundingMapArea boundingMapArea = new BoundingMapArea(pts);
	        	textLayer.addObstacle(boundingMapArea);
	        }
	        buildingOverlays.close();
	        
	        buildingAdapter.close();
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
			
			if (!refreshData) {
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
	        	collection = service.getAllLocationData(version);
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

			//replace the building data with the new data
	        LocationAdapter buildingAdapter = new LocationAdapter();
	        buildingAdapter.open();
	        buildingAdapter.replaceBuildings(collection.mapAreas);
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
			MainActivity.this.setProgressBarIndeterminateVisibility(true);
		}
		
		private void updateOverlays() {
			MainActivity.this.poiLayer = poiLayer;
			MainActivity.this.buildingLayer = buildingLayer;
			MainActivity.this.textLayer = textLayer;
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
			
	    	if (savedInstanceState != null) {
	    		MainActivity.this.buildingLayer.setSelectedBuilding(savedInstanceState.getLong(BUILDING_SELECTED_ID));
	    	}
			
	        MainActivity.this.setProgressBarIndeterminateVisibility(false);
		}
		
		@Override
		protected void onCancelled() {
			MainActivity.this.setProgressBarIndeterminateVisibility(false);
		}
		
	}
	
}
