package edu.rosehulman.android.directory;

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

import edu.rosehulman.android.directory.R;
import edu.rosehulman.android.directory.db.BuildingAdapter;
import edu.rosehulman.android.directory.model.Building;

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
        
        boolean useBeta = !getIntent().getBooleanExtra("DisableBeta", false);
        if (useBeta) {
	        betaManager = new BetaManagerManager(this);
	        
	        if (betaManager.hasBetaManager()) {
	        	if (betaManager.isBetaRegistered()) {
	        		betaManager.launchBetaActivity(BetaManagerManager.ACTION_SHOW_STARTUP);	
	        	} else {
	        		betaManager.launchBetaActivity(BetaManagerManager.ACTION_SHOW_REGISTER);
	        	}
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
        mapView.getOverlays().add(new BuildingOverlay(this, center));
        //mapView.getOverlays().add(new TextOverlay(center, "Hulman Memorial Union"));
        
        //display our location indicator (and compass)
        myLocation = new MyLocationOverlay(this, mapView);
        mapView.getOverlays().add(myLocation);
        
        BuildingAdapter buildingAdapter = new BuildingAdapter(this);
        buildingAdapter.open();
        buildingAdapter.replaceBuildings(new Building[] {
        		new Building("Hatfield Hall", 39482209, -87322206),
        		new Building("Hadley Hall", 39482894, -87324076),
        		new Building("Olin Hall", 39482784, -87324894),
        		new Building("Moench Hall", 39483429, -87323777),
        		new Building("Crapo Hall", 39483731, -87324465),
        		new Building("Logan Library", 39483421, -87324848),
        		new Building("Rotz Mechanical Engineering Lab", 39483670, -87323245),
        		new Building("Myers Hall", 39483866, -87323072),
        		new Building("Facilities Operations", 39484955, -87321872),
        		new Building("Recycling Center", 39484621, -87320085),
        		new Building("Alpha Tau Omega", 39484156, -87321158),
        		new Building("Triangle", 39483609, -87321132),
        		new Building("Lambda Chi Alpha Theta Kappa", 39483057, -87321081),
        		new Building("Skinner Hall", 39482385, -87320735),
        		new Building("Circle K", 39481963, -87320947),
        		new Building("Public Safety", 39481928, -87320409),
        		new Building("BSB Hall", 39482470, -87325753),
        		new Building("Deming Hall", 39483435, -87325790),
        		new Building("Hulman Memorial Union", 39483558, -87326812),
        		new Building("Speed Hall", 39482137, -87326702),
        		new Building("Percopo Hall", 39482164, -87328147),
        		new Building("Mees Hall", 39483542, -87327770),
        		new Building("Scharpenberg Hall", 39483639, -87328123),
        		new Building("Blumberg Hall", 39483385, -87328352),
        		new Building("Apartments", 39483616, -87329272),
        		new Building("SRC", 39484708, -87327324),
        		new Building("White Chapel", 39482499, -87329427)
        		}
        );
        
        
        Cursor buildingOverlays = buildingAdapter.getBuildingOverlayCursor();
        buildingOverlays.moveToFirst();
        int iName = buildingOverlays.getColumnIndex("name");
        int iLat = buildingOverlays.getColumnIndex("centerLat");
        int iLon = buildingOverlays.getColumnIndex("centerLon");
        do {
        	String name = buildingOverlays.getString(iName);
        	GeoPoint pt = new GeoPoint(buildingOverlays.getInt(iLat), buildingOverlays.getInt(iLon));
        	mapView.getOverlays().add(new TextOverlay(pt, name));
        } while (buildingOverlays.moveToNext());
        
        		buildingAdapter.close();
//        DatabaseHelper dbHelper = new DatabaseHelper(this);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("name", "TestBuilding");
//        db.insert("buildings", null, values);
//        db.close();
        
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
    	menu.setGroupVisible(R.id.location_items, myLocation.getMyLocation() != null);
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