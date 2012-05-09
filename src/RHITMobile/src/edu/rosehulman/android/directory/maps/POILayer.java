package edu.rosehulman.android.directory.maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

import edu.rosehulman.android.directory.LocationActivity;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.tasks.PopulateLocation;
import edu.rosehulman.android.directory.tasks.TaskManager;

/**
 * Overlay containing Point Of Interest markers
 */
public class POILayer extends BalloonItemizedOverlay<OverlayItem> implements ManageableOverlay {

	private static final int MIN_ZOOM_LEVEL = 19;
	
	private OverlayManagerControl manager;
	
	private class PointOfInterest {
		public Location location;
		public OverlayItem poi;
		
		public PointOfInterest(Location location, OverlayItem poi) {
			this.location = location;
			this.poi = poi;
		}
	}
	
	private static Drawable transparent;
	private TaskManager taskManager;
	private Map<Long, PointOfInterest> poiMap;
	private List<PointOfInterest> pois;
	private boolean animate = true;

	/**
	 * Create a new POILayer
	 * 
	 * @param defaultMarker A Drawable to use to render each point of interest
	 * @param mapView The MapView that this overlay will be rendered on
	 * @param taskManager The task manager that can be used to abort tasks if the activity is paused
	 */
	public POILayer(Drawable defaultMarker, MapView mapView, TaskManager taskManager) {
		super(boundCenterBottom(defaultMarker), mapView);
		this.taskManager = taskManager;
		poiMap = new HashMap<Long, PointOfInterest>();
		pois = new ArrayList<PointOfInterest>();
		
		if (transparent == null) {
			transparent = getMapView().getResources().getDrawable(android.R.color.transparent);
		}
	}
	
	/**
	 * Add a new location to the overlay
	 * 
	 * @param location The Location to render
	 */
	public void add(Location location) {
		OverlayItem overlay = new OverlayItem(location.center.asGeoPoint(), location.name, location.description);
		PointOfInterest poi = new PointOfInterest(location, overlay);
		poiMap.put(location.id, poi);
		pois.add(poi);
		populate();
	}
	
	/**
	 * Focus a particular POI
	 * 
	 * @param id The ID of the POI to focus
	 * @param animate Whether the focus operation should animate or not
	 * 
	 * @return True if the POI was found immediately; false otherwise
	 */
	public boolean focus(long id, boolean animate) {
		PointOfInterest poi = poiMap.get(id);
		if (poi == null) {
			ShowLocation task = new ShowLocation(animate);
			taskManager.addTask(task);
			task.execute(id);
			
			//hide what is visible for now
			this.setFocus(null);
			return false;
		}
		this.animate = animate;
		this.setLastFocusedIndex(pois.indexOf(poi));
		this.onTap(pois.indexOf(poi));
		this.animate = true;
		
		return true;
	}
	
	/**
	 * Determines the id of the focused poi
	 * 
	 * @return the id of the focused poi, or -1 if none
	 */
	public long getFocusId() {
		int index = getLastFocusedIndex();
		if (index == -1)
			return -1;
		
		return pois.get(index).location.id;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return pois.get(i).poi;
	}

	@Override
	public int size() {
		return pois.size();
	}
	
	@Override
	protected boolean onBalloonTap(int index, OverlayItem item) {
		final Location loc = pois.get(index).location;
		
		new PopulateLocation(new Runnable() {
			
			@Override
			public void run() {
				Context context = getMapView().getContext();
				context.startActivity(LocationActivity.createIntent(context, loc));
			}
		}).execute(loc);
		
		return true;
	}
	
	@Override
	protected void animateTo(int index, GeoPoint center) {
		if (manager != null) {
			manager.markSelected();
		}
		MapView mapView = getMapView();
		ViewController controller = new ViewController(mapView);
		Point pt = new Point(mapView.getWidth() / 2, mapView.getHeight() / 4 * 3);
		controller.animateTo(center, pt, MIN_ZOOM_LEVEL + 1, animate);
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (mapView.getZoomLevel() < MIN_ZOOM_LEVEL)
			return;
		
		super.draw(canvas, mapView, shadow);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		if (mapView.getZoomLevel() < MIN_ZOOM_LEVEL)
			return false;
		
		return super.onTouchEvent(event, mapView);
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event, MapView mapView) {
		if (mapView.getZoomLevel() < MIN_ZOOM_LEVEL)
			return false;
		
		return super.onTrackballEvent(event, mapView);
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		if (mapView.getZoomLevel() < MIN_ZOOM_LEVEL)
			return false;
		
		return super.onTap(p, mapView);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event, MapView mapView) {
		if (mapView.getZoomLevel() < MIN_ZOOM_LEVEL)
			return false;

		return super.onKeyDown(keyCode, event, mapView);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event, MapView mapView) {
		if (mapView.getZoomLevel() < MIN_ZOOM_LEVEL)
			return false;
		
		return super.onKeyUp(keyCode, event, mapView);
	}
	
	@Override
	public void clearSelection() {
		this.setFocus(null);
	}

	@Override
	public void setManager(OverlayManagerControl manager) {
		this.manager = manager;
	}
	
	private class ShowLocation extends AsyncTask<Long, Void, Location> {
		
		private boolean animate;
		
		public ShowLocation(boolean animate) {
			this.animate = animate;
		}

		@Override
		protected Location doInBackground(Long... ids) {
			LocationAdapter locationAdapter = new LocationAdapter();
			locationAdapter.open();
			
			Location loc = locationAdapter.getLocation(ids[0]);
			
			locationAdapter.close();
			
			return loc;
		}
		
		@Override
		protected void onPostExecute(Location loc) {
			//add our overlay
			OverlayItem overlay = new OverlayItem(loc.center.asGeoPoint(), loc.name, loc.description);
			overlay.setMarker(transparent);
			PointOfInterest poi = new PointOfInterest(loc, overlay);
			poiMap.put(loc.id, poi);
			pois.add(poi);
			populate();
			
			//and select it
			POILayer.this.animate = animate;
			setLastFocusedIndex(pois.indexOf(poi));
			onTap(pois.indexOf(poi));
			POILayer.this.animate = true;
		}
		
	}

}
