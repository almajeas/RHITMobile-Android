package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

import edu.rosehulman.android.directory.model.Location;

public class POILayer extends BalloonItemizedOverlay<OverlayItem> {
	
	private List<OverlayItem> poi;

	public POILayer(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
		poi = new ArrayList<OverlayItem>();
	}
	
	public void add(Location location) {
		String snippet = location.description + " " + location.description + "\n";
		snippet = snippet + snippet + snippet + snippet;
		OverlayItem overlay = new OverlayItem(location.center.asGeoPoint(), location.name, snippet);
		poi.add(overlay);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return poi.get(i);
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
		controller.animateTo(center, pt, 0, 0);
	}

}
