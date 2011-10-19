package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

import edu.rosehulman.android.directory.model.Location;

public class POILayer extends BalloonItemizedOverlay<OverlayItem> {

	private static final int MIN_ZOOM_LEVEL = 19;
	
	private class PointOfInterest {
		public Location location;
		public OverlayItem poi;
		
		public PointOfInterest(Location location, OverlayItem poi) {
			this.location = location;
			this.poi = poi;
		}
	}
	
	private List<PointOfInterest> poi;

	public POILayer(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
		poi = new ArrayList<PointOfInterest>();
	}
	
	public void add(Location location) {
		OverlayItem overlay = new OverlayItem(location.center.asGeoPoint(), location.name, location.description);
		poi.add(new PointOfInterest(location, overlay));
		populate();
	}
	
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
		Log.d(C.TAG, "Tapped: " + index);
		return true;
	}
	
	@Override
	protected void animateTo(int index, GeoPoint center) {
		MapView mapView = getMapView();
		ViewController controller = new ViewController(mapView);
		Point pt = new Point(mapView.getWidth() / 2, mapView.getHeight() / 4 * 3);
		//TODO determine correct span
		controller.animateTo(center, pt, 0, 0);
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (mapView.getZoomLevel() < MIN_ZOOM_LEVEL)
			return;
		
		super.draw(canvas, mapView, shadow);
	}

}
