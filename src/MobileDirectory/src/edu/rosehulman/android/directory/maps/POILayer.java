package edu.rosehulman.android.directory.maps;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

import edu.rosehulman.android.directory.LocationActivity;
import edu.rosehulman.android.directory.model.Location;

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
	
	private List<PointOfInterest> poi;

	/**
	 * Create a new POILayer
	 * 
	 * @param defaultMarker A Drawable to use to render each point of interest
	 * @param mapView The MapView that this overlay will be rendered on
	 */
	public POILayer(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
		poi = new ArrayList<PointOfInterest>();
	}
	
	/**
	 * Add a new location to the overlay
	 * 
	 * @param location The Location to render
	 */
	public void add(Location location) {
		OverlayItem overlay = new OverlayItem(location.center.asGeoPoint(), location.name, location.description);
		poi.add(new PointOfInterest(location, overlay));
		populate();
	}
	
	/**
	 * Focus a particular POI
	 * 
	 * @param id The ID of the POI to focus
	 * @return True if the POI was found; false otherwise
	 */
	public boolean setFocus(long id) {
		for (int i = 0; i < poi.size(); i++) {
			PointOfInterest poi = this.poi.get(i);
			if (poi.location.id == id) {
				this.onTap(i);
				return true;
			}
		}
		
		return false;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return poi.get(i).poi;
	}

	@Override
	public int size() {
		return poi.size();
	}
	
	@Override
	protected boolean onBalloonTap(int index, OverlayItem item) {
		final Location loc = poi.get(index).location;
		
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
		controller.animateTo(center, pt, MIN_ZOOM_LEVEL + 1);
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (mapView.getZoomLevel() < MIN_ZOOM_LEVEL)
			return;
		
		super.draw(canvas, mapView, shadow);
	}

	@Override
	public void clearSelection() {
		this.setFocus(null);
	}

	@Override
	public void setManager(OverlayManagerControl manager) {
		this.manager = manager;
	}

}
