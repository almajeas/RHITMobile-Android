package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import edu.rosehulman.android.directory.db.MapAreaAdapter;
import edu.rosehulman.android.directory.model.MapAreaCollection;
import edu.rosehulman.android.directory.service.MobileDirectoryService;

/**
 * Main entry point into MobileDirectory
 */
public class MainActivity extends MapActivity {
	
	private BetaManagerManager betaManager;

    private MapView mapView;
    
    private LocationManager locationManager;
    private LocationListener locationListener;

    private MyLocationOverlay myLocation;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        betaManager = new BetaManagerManager(this);
        
	    if (betaManager.hasBetaManager() && betaManager.isBetaEnabled()) {
	       	if (betaManager.isBetaRegistered()) {
	       		betaManager.launchBetaActivity(BetaManagerManager.ACTION_SHOW_STARTUP);	
	       	} else {
	       		betaManager.launchBetaActivity(BetaManagerManager.ACTION_SHOW_REGISTER);
	       	}
	    }
        
        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        mapView.setSatellite(true);
        
        //center the map
        GeoPoint center = new GeoPoint(39483760, -87325929);
        mapView.getController().setCenter(center);
        mapView.getController().zoomToSpan(6241, 13894);

        //draw something
        //mapView.getOverlays().add(new BuildingOverlay(this, center));
        
        //display our location indicator (and compass)
        myLocation = new MyLocationOverlay(this, mapView);
        mapView.getOverlays().add(myLocation);
        
        MobileDirectoryService service = new MobileDirectoryService();
        MapAreaCollection collection = null;
        try {
			collection = service.getMapAreas(null);
		} catch (Exception e) {
			Log.e(C.TAG, "Failed to download the new map areas", e);
			return;
		}
        
        MapAreaAdapter buildingAdapter = new MapAreaAdapter();
        buildingAdapter.open();
        buildingAdapter.replaceBuildings(collection.mapAreas);
        
        TextOverlayLayer textLayer = new TextOverlayLayer();
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
        mapView.getOverlays().add(textLayer);        
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
			public void onLocationChanged(Location location) {
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
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.setGroupVisible(R.id.beta_channel, betaManager.hasBetaManager());
    	menu.setGroupEnabled(R.id.location_items, myLocation.getMyLocation() != null);
        return true;
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
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	@Override
	protected boolean isRouteDisplayed() {
		//FIXME update when we start displaying route information
		return false;
	}
    
}