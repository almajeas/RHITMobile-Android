package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
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
import edu.rosehulman.android.directory.maps.DirectionsLayer;
import edu.rosehulman.android.directory.maps.LocationSearchLayer;
import edu.rosehulman.android.directory.maps.OffsiteTourLayer;
import edu.rosehulman.android.directory.maps.OverlayManager;
import edu.rosehulman.android.directory.maps.POILayer;
import edu.rosehulman.android.directory.maps.TextOverlayLayer;
import edu.rosehulman.android.directory.maps.ViewController;
import edu.rosehulman.android.directory.model.DirectionPath;
import edu.rosehulman.android.directory.model.Directions;
import edu.rosehulman.android.directory.model.DirectionsResponse;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.model.LocationIdsResponse;
import edu.rosehulman.android.directory.model.LocationNamesCollection;
import edu.rosehulman.android.directory.model.VersionType;
import edu.rosehulman.android.directory.service.MobileDirectoryService;
import edu.rosehulman.android.directory.util.ArrayUtil;
import edu.rosehulman.android.directory.util.BoundingBox;
import edu.rosehulman.android.directory.util.Point;

/**
 * Main entry point into MobileDirectory
 */
public class CampusMapActivity extends SherlockMapActivity {
	
    private static final String STATE_SELECTED_ID = "SelectedId";
    private static final String STATE_SELECTED_STEP = "SelectedStep";
    private static final String STATE_LOCATIONS = "Locations";
    private static final String STATE_DIRECTIONS = "Directions";
    
    public static final String ACTION_DIRECTIONS = "edu.rosehulman.android.directory.intent.action.DIRECTIONS";
    public static final String ACTION_TOUR = "edu.rosehulman.android.directory.intent.action.TOUR";

	public static final String EXTRA_BUILDING_ID = "BUILDING_ID";
	public static final String EXTRA_WAYPOINTS = "WAYPOINTS";
	public static final String EXTRA_TOUR_START_ID = "TOUR_START_ID";
	public static final String EXTRA_TOUR_TAGS = "TOUR_TAGS";
	public static final String EXTRA_DIRECTIONS_FOCUS_INDEX = "DIRECTIONS_FOCUS_INDEX";
	
	private static final int MIN_ZOOM_LEVEL = 17;
	private static final int MAX_ZOOM_LEVEL = 22;

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

	public static Intent createTourIntent(Context context, long[] tags) {
		Intent intent = createIntent(context);
		intent.setAction(ACTION_TOUR);
		intent.putExtra(EXTRA_TOUR_TAGS, tags);
		return intent;
	}
	
	public static Intent createTourIntent(Context context, long startId, long[] tags) {
		Intent intent = createIntent(context);
		intent.setAction(ACTION_TOUR);
		intent.putExtra(EXTRA_TOUR_START_ID, startId);
		intent.putExtra(EXTRA_TOUR_TAGS, tags);
		return intent;
	}
	
	public static Intent createResultIntent(int directionsFocusIndex) {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_DIRECTIONS_FOCUS_INDEX, directionsFocusIndex);
		return intent;
	}
	
	public CampusMapActivity() {
		super();
	}

	private Intent intent;
    private MapView mapView;
    
    private String searchQuery;
    
    private LocationManager locationManager;
    private LocationListener locationListener;

    private OverlayManager overlayManager;
    private DirectionsLayer directionsLayer;
    private OffsiteTourLayer offsiteTourLayer;
    private LocationSearchLayer searchOverlay;
    private POILayer poiLayer;
    private BuildingOverlayLayer buildingLayer;
    private TextOverlayLayer textLayer;
    private EventOverlay eventLayer;
    private MyLocationOverlay myLocation;
    
    private View btnZoomIn;
    private View btnZoomOut;
    private View btnPrev;
    private View btnNext;
    private View btnListDirections;
    
    private TaskManager taskManager;
    
    private ServiceManager<IDataUpdateService> updateService;
    
    private Bundle savedInstanceState;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campus_map);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        intent = getIntent();
        
        taskManager = new TaskManager();
        
        mapView = (MapView)findViewById(R.id.mapview);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnListDirections = findViewById(R.id.btnListDirections);
        
        overlayManager = new OverlayManager();
        myLocation = new MyLocationOverlay(this, mapView);
        eventLayer = new EventOverlay();
        
        //basic initialization
        if (savedInstanceState == null) {
	        mapView.setSatellite(true);
	        
	        //center the map
	        moveToCampus(false);
	        
	    } else {
	    	this.savedInstanceState = savedInstanceState;
	    }
        mapView.setBuiltInZoomControls(true);

        //intent specific initialization
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			searchQuery = intent.getStringExtra(SearchManager.QUERY);
			SearchLocations task = new SearchLocations();
			taskManager.addTask(task);
			task.execute(searchQuery);
			actionBar.setSubtitle(searchQuery);
		
    	} else if (ACTION_DIRECTIONS.equals(intent.getAction())) {
			setTitle("Directions");
			
        	if (savedInstanceState != null && 
        			savedInstanceState.containsKey(STATE_DIRECTIONS) &&
        			savedInstanceState.containsKey(STATE_LOCATIONS)) {
				Directions directions = savedInstanceState.getParcelable(STATE_DIRECTIONS);
				Parcelable[] pLocations = savedInstanceState.getParcelableArray(STATE_LOCATIONS);
				Location[] locations = ArrayUtil.cast(pLocations, new Location[pLocations.length]);
				
				generateDirectionsLayer(directions, locations);
				if (savedInstanceState.containsKey(STATE_SELECTED_STEP)) {
					directionsLayer.focus(savedInstanceState.getInt(STATE_SELECTED_STEP, -1), false);
				}
        	} else {
	    		long[] ids = intent.getLongArrayExtra(EXTRA_WAYPOINTS);
	
				LoadDirections task = new LoadDirections(ids);
				taskManager.addTask(task);
				task.execute();
        	}
			btnListDirections.setVisibility(View.VISIBLE);
			btnPrev.setVisibility(View.VISIBLE);
			btnNext.setVisibility(View.VISIBLE);
			
		} else if (ACTION_TOUR.equals(intent.getAction())) {
			long startId = intent.getLongExtra(EXTRA_TOUR_START_ID, -1);
			
			setTitle("Campus Tour");
			
			if (savedInstanceState != null && 
        			savedInstanceState.containsKey(STATE_DIRECTIONS) &&
        			savedInstanceState.containsKey(STATE_LOCATIONS) &&
        			startId >= 0) {
				//restore on-campus tour
				Directions directions = savedInstanceState.getParcelable(STATE_DIRECTIONS);
				Parcelable[] pLocations = savedInstanceState.getParcelableArray(STATE_LOCATIONS);
				Location[] locations = ArrayUtil.cast(pLocations, new Location[pLocations.length]);
				
				generateDirectionsLayer(directions, locations);
				if (savedInstanceState.containsKey(STATE_SELECTED_STEP)) {
					directionsLayer.focus(savedInstanceState.getInt(STATE_SELECTED_STEP, -1), false);
				}
			} else if (savedInstanceState != null && 
        			savedInstanceState.containsKey(STATE_LOCATIONS) &&
        			startId == -1) {
				//restore off-campus tour
				Parcelable[] pLocations = savedInstanceState.getParcelableArray(STATE_LOCATIONS);
				Location[] locations = ArrayUtil.cast(pLocations, new Location[pLocations.length]); 
				
				generateOffsiteTourLayer(locations);
				if (savedInstanceState.containsKey(STATE_SELECTED_STEP)) {
					offsiteTourLayer.focus(savedInstanceState.getInt(STATE_SELECTED_STEP, -1), false);
				}
        	} else {
				long[] tagIds = intent.getLongArrayExtra(EXTRA_TOUR_TAGS);
				
				if (startId == -1) {
					LoadOffsiteTour task = new LoadOffsiteTour(tagIds);
	    			taskManager.addTask(task);
	    			task.execute();
				} else {
	    			LoadTour task = new LoadTour(startId, tagIds);
	    			taskManager.addTask(task);
	    			task.execute();
				}
        	}
			btnListDirections.setVisibility(View.VISIBLE);
			btnPrev.setVisibility(View.VISIBLE);
			btnNext.setVisibility(View.VISIBLE);
		}
        
		btnZoomIn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnZoomIn_clicked();
			}
		});
		btnZoomOut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnZoomOut_clicked();
			}
		});
		btnPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnPrev_clicked();
			}
		});
		btnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnNext_clicked();
			}
		});
		btnListDirections.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnListDirections_clicked();
			}
		});
		
    	mapView.setBuiltInZoomControls(false);
    	
        rebuildOverlays();
        
        mapView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					updateZoomControls();
				}
				return false;
			}
		});
        
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
						LoadOverlays task = new LoadOverlays(dialog);
						taskManager.addTask(task);
						task.execute();
					}
				});
			}
		});
    }
    
    @Override
    protected void onSaveInstanceState(Bundle bundle) {
    	super.onSaveInstanceState(bundle);
    	
    	if (buildingLayer != null) {
    		bundle.putLong(STATE_SELECTED_ID, getFocusedLocation());
    	}
    	
    	if (directionsLayer != null) {
    		bundle.putParcelable(STATE_DIRECTIONS, directionsLayer.directions);
    		bundle.putParcelableArray(STATE_LOCATIONS, directionsLayer.locations);
    		bundle.putInt(STATE_SELECTED_STEP, directionsLayer.getLastFocusedIndex());
		}
    	
    	if (offsiteTourLayer != null) {
    		bundle.putParcelableArray(STATE_LOCATIONS, offsiteTourLayer.locations);
    		bundle.putInt(STATE_SELECTED_STEP, offsiteTourLayer.getLastFocusedIndex());
		}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.campus_map, menu);
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
        
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		}
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		}
        
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
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case DirectionsLayer.REQUEST_DIRECTIONS_LIST:
			switch (resultCode) {
			case Activity.RESULT_OK:
				//focus the requested step
				int step = data.getIntExtra(EXTRA_DIRECTIONS_FOCUS_INDEX, -1);
				directionsLayer.focus(step, false);
				break;
			}
			break;
		case OffsiteTourLayer.REQUEST_DIRECTIONS_LIST:
			switch (resultCode) {
			case Activity.RESULT_OK:
				//focus the requested step
				int step = data.getIntExtra(EXTRA_DIRECTIONS_FOCUS_INDEX, -1);
				offsiteTourLayer.focus(step, false);
				break;
			}
			break;
		}
	}
    
    private void moveToCampus(boolean animate) {
        MapController controller = mapView.getController();
        controller.setZoom(MIN_ZOOM_LEVEL+1);
        
        GeoPoint center = new GeoPoint(39483760, -87325929);
        if (animate) {
        	controller.animateTo(center);
        } else {
        	controller.setCenter(center);
        }
    }
    
    private void updateZoomControls() {
    	int zoomLevel = mapView.getZoomLevel();
    	
    	if (zoomLevel == MIN_ZOOM_LEVEL) {
    		btnZoomOut.setEnabled(false);
    	} else if (zoomLevel == MAX_ZOOM_LEVEL) {
    		btnZoomIn.setEnabled(false);
    	} else {
    		btnZoomIn.setEnabled(true);
    		btnZoomOut.setEnabled(true);
    	}
    }
    
    private void btnZoomIn_clicked() {
    	mapView.getController().zoomIn();
    	updateZoomControls();
    }

    private void btnZoomOut_clicked() {
    	mapView.getController().zoomOut();
    	updateZoomControls();
    }

    private void btnPrev_clicked() {
    	if (directionsLayer != null)
    		directionsLayer.stepPrevious();
    	else if (offsiteTourLayer != null)
    		offsiteTourLayer.stepPrevious();
    }

    private void btnNext_clicked() {
    	if (directionsLayer != null)
    		directionsLayer.stepNext();
    	else if (offsiteTourLayer != null)
    		offsiteTourLayer.stepNext();
    }

    private void btnListDirections_clicked() {
    	if (directionsLayer != null)
    		directionsLayer.showDirectionsList(-1);
    	else if (offsiteTourLayer != null)
    		offsiteTourLayer.showDirectionsList(-1);
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
    	
    	if (id >= 0 || poiLayer == null)
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
        case R.id.goto_campus:
        	moveToCampus(true);
        	return true;
        case android.R.id.home:
        	finish();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	@Override
	protected boolean isRouteDisplayed() {
		return ACTION_DIRECTIONS.equals(getIntent().getAction());
	}
	
	@Override
	protected boolean isLocationDisplayed() {
		return true;
	}
	
    private void rebuildOverlays() {
    	List<Overlay> overlays = mapView.getOverlays();
    	overlays.clear();
    	
    	overlays.add(eventLayer);
    	
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
    	
    	if (directionsLayer != null) {
    		overlays.add(directionsLayer);
    		overlayManager.addOverlay(directionsLayer);
    	}
    	
    	if (offsiteTourLayer != null) {
    		overlays.add(offsiteTourLayer);
    		overlayManager.addOverlay(offsiteTourLayer);
    	}

    	overlays.add(myLocation);
    	
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
    	
    	@Override
    	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
    		if (mapView.getZoomLevel() < MIN_ZOOM_LEVEL) {
    			mapView.getController().zoomIn();
    		}
    		super.draw(canvas, mapView, shadow);
    	}
    }

    private void generateBuildingsLayer() {
    	BuildingOverlayLayer buildings = new BuildingOverlayLayer(mapView, buildingSelectedListener);
    	buildingLayer = buildings;
    }
    
    private void generatePOILayer() {
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

	private void generateTextLayer() {
        textLayer = new TextOverlayLayer();
	}
	
	private void generateDirectionsLayer(Directions directions, Location[] locations) {

		directionsLayer = new DirectionsLayer(mapView, taskManager, directions, locations, new DirectionsLayer.UIListener() {
			@Override
			public void setPrevButtonEnabled(boolean enabled) {
				btnPrev.setEnabled(enabled);
			}
			@Override
			public void setNextButtonEnabled(boolean enabled) {
				btnNext.setEnabled(enabled);
			}
			@Override
			public void startActivityForResult(Intent intent, int requestCode) {
				CampusMapActivity.this.startActivityForResult(intent, requestCode);
			}
		});

	}
	
	private void generateOffsiteTourLayer(Location[] locations) {

		offsiteTourLayer = new OffsiteTourLayer(mapView, taskManager, locations, new OffsiteTourLayer.UIListener() {
			@Override
			public void setPrevButtonEnabled(boolean enabled) {
				btnPrev.setEnabled(enabled);
			}
			@Override
			public void setNextButtonEnabled(boolean enabled) {
				btnNext.setEnabled(enabled);
			}
			@Override
			public void startActivityForResult(Intent intent, int requestCode) {
				CampusMapActivity.this.startActivityForResult(intent, requestCode);
			}
		});
		
	}
	
	private class LoadOverlays extends AsyncTask<Void, Void, Void> {
		
		private ProgressDialog dialog;
		
		public LoadOverlays(ProgressDialog dialog) {
			this.dialog = dialog;
		}

		@Override
		protected Void doInBackground(Void... params) {
			
			BuildingOverlayLayer.initializeCache();
	    	
			TextOverlayLayer.initializeCache();

			//don't generate POI if we are searching or showing directions
			if (ACTION_DIRECTIONS.equals(intent.getAction()) ||
					ACTION_TOUR.equals(intent.getAction()) ||
					Intent.ACTION_SEARCH.equals(intent.getAction())) {
				return null;
			}
			
			generatePOILayer();
			
	    	return null;
		}
		
		@Override
		protected void onCancelled() {
			if (dialog.isShowing()) {
				dialog.cancel();
			}
		}
		
		@Override
		protected void onPostExecute(Void res) {
			if (dialog.isShowing()) {
				dialog.cancel();
			}

			generateTextLayer();
			
			//don't generate buildings if we are searching
			if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
				return;
			}
			
			generateBuildingsLayer();
			rebuildOverlays();

			//don't focus anything if we are showing directions
			if (ACTION_DIRECTIONS.equals(intent.getAction())) {
				return;
			}
			
			//set a selected location
	    	if (savedInstanceState != null) {
	    		focusLocation(savedInstanceState.getLong(STATE_SELECTED_ID), false);
	    	} else if (intent.hasExtra(EXTRA_BUILDING_ID)) {
	    		long id = intent.getLongExtra(EXTRA_BUILDING_ID, -1);
	    		focusLocation(id, false);
	    	}
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
	    		.setItems(names, new DialogInterface.OnClickListener() {
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
	
private class LoadDirections extends ProcessDirections {
		
		private long ids[];
		
		public LoadDirections(long[] ids) {
			super("Getting Directions...");
			this.ids = ids;
		}
		
		@Override
		protected DirectionsResponse getDirections() {
			assert(ids.length == 2);
			long from = ids[0];
			long to = ids[1];
			
			DirectionsResponse response = null;
			
			do {
				try {
					response = service.getDirections(from, to);
				} catch (Exception e) {
					Log.e(C.TAG, "Failed to download initial directions");
					if (isCancelled()) {
						return null;
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException ex) {
						return null;
					}
				}
			} while (response == null);
			
			return response;
		}

		@Override
		protected DirectionsResponse checkStatus(int requestId) throws Exception {
			return service.getDirectionsStatus(requestId);
		}

		@Override
		protected String getError() {
			return "An error occurred while generating directions.  Try a different destination location.";
		}
		
	}
	
	private class LoadTour extends ProcessDirections {
		
		private long startId;
		private long tagIds[];
		
		public LoadTour(long startId, long[] tagIds) {
			super("Building Tour...");
			this.startId = startId;
			this.tagIds = tagIds;
		}
		
		@Override
		protected DirectionsResponse getDirections() {
			DirectionsResponse response = null;
			
			do {
				try {
					response = service.getTour(startId, tagIds);
				} catch (Exception e) {
					Log.e(C.TAG, "Failed to download tour data");
					if (isCancelled()) {
						return null;
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException ex) {
						return null;
					}
				}
			} while (response == null);
			
			return response;
		}

		@Override
		protected DirectionsResponse checkStatus(int requestId) throws Exception {
			return service.getOncampusTourStatus(requestId);
		}
		
		@Override
		protected String getError() {
			return "An error occurred while loading your tour.  Try again later or try using different tags.";
		}
		
	}

	private class LoadOffsiteTour extends AsyncTask<Void, Integer, Void> {
		
		private ProgressDialog dialog;

		private long tagIds[];
		
		private Location[] nodes;
		
		public LoadOffsiteTour(long[] tagIds) {
			this.tagIds = tagIds;
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(CampusMapActivity.this);
			dialog.setTitle(null);
			dialog.setMessage("Building Tour...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(true);
				}
			});
			dialog.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			
			MobileDirectoryService service = new MobileDirectoryService();
			
			LocationIdsResponse response = null;
			
			do {
				try {
					response = service.getTour(tagIds);
				} catch (Exception ex) {
					Log.e(C.TAG, "Failed to download tour data");
					if (isCancelled()) {
						return null;
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException ex1) {
						return null;
					}
				}
			} while (response == null);
			
			//load the relevant locations
			LocationAdapter locationAdapter = new LocationAdapter();
			locationAdapter.open();
			List<Location> nodeList = new ArrayList<Location>();
			for (long id : response.ids) {
				Location loc = locationAdapter.getLocation(id);
				locationAdapter.loadAlternateNames(loc);
				locationAdapter.loadHyperlinks(loc);
				locationAdapter.loadMapArea(loc, true);
				nodeList.add(loc);
			}
			locationAdapter.close();
			nodes = new Location[nodeList.size()];
			nodeList.toArray(nodes);
			
			return null;
		}

		@Override
		protected void onPostExecute(Void res) {
			dialog.dismiss();
			
			if (nodes != null) {
				generateOffsiteTourLayer(nodes);

				//fit the bounds of the map to the tour
				BoundingBox bounds = offsiteTourLayer.bounds;
				if (bounds != null) {
					Point center = bounds.getCenter();
					GeoPoint pt = new GeoPoint(center.x, center.y);
					new ViewController(mapView).animateTo(pt, bounds.getHeight(), bounds.getWidth(), false);
				}
				
				rebuildOverlays();
			} else {
				finish();
			}
		}
		
		@Override
		protected void onCancelled() {
			dialog.dismiss();
			finish();
		}
		
	}
	
	private abstract class ProcessDirections extends BackgroundTask<Void, Integer, Directions> {
		
		private ProgressDialog dialog;
		
		private String message;
		
		private Location[] nodes;
		
		protected MobileDirectoryService service = new MobileDirectoryService();
		
		public ProcessDirections(String message) {
			this.message = message;
		}
		
		protected abstract DirectionsResponse getDirections();
		protected abstract DirectionsResponse checkStatus(int requestId) throws Exception;
		protected abstract String getError();

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(CampusMapActivity.this);
			dialog.setTitle(null);
			dialog.setMessage(message);
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(true);
					finish();
				}
			});
			dialog.show();
		}
		
		@Override
		protected Directions doInBackground(Void... params) {
			
			DirectionsResponse response = getDirections();
			if (response == null) {
				return null;
			}
			int requestID = response.requestID;
			
			while (response.done != 100) {
				publishProgress(response.done);
				try {
					response = checkStatus(requestID);
				} catch (Exception e) {
					Log.e(C.TAG, "Failed to download directions");
					if (isCancelled()) {
						return null;
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException ex) {
						return null;
					}
				}
			}
			
			//load the relevant locations
			LocationAdapter locationAdapter = new LocationAdapter();
			locationAdapter.open();
			List<Location> nodeList = new ArrayList<Location>();
			for (DirectionPath path : response.result.paths) {
				if (path.location >= 0) {
					Location loc = locationAdapter.getLocation(path.location);
					locationAdapter.loadAlternateNames(loc);
					locationAdapter.loadHyperlinks(loc);
					locationAdapter.loadMapArea(loc, true);
					nodeList.add(loc);
				}
			}
			locationAdapter.close();
			nodes = new Location[nodeList.size()];
			nodeList.toArray(nodes);
			
			publishProgress(100);

			return response.result;
		}
		
		@Override
		protected void onProgressUpdate(Integer... args) {
			int progress = args[0];
			Log.i(C.TAG, "Directions status: " + progress);
		}

		@Override
		protected void onPostExecute(Directions directions) {
			dialog.dismiss();
			
			if (directions == null || directions.paths.length == 0) {
				Toast.makeText(CampusMapActivity.this, getError(), Toast.LENGTH_LONG).show();
				finish();
				return;
			}
			
			generateDirectionsLayer(directions, nodes);
			
			//fit the bounds of the map to the directions
			BoundingBox bounds = directionsLayer.bounds;
			Point center = bounds.getCenter();
			GeoPoint pt = new GeoPoint(center.x, center.y);
			new ViewController(mapView).animateTo(pt, bounds.getHeight(), bounds.getWidth(), false);
			
			rebuildOverlays();
		}
		
		@Override
		protected void onAbort() {
			dialog.dismiss();
		}
		
		@Override
		protected void onCancelled() {
			dialog.dismiss();
		}
		
	}
	
}
