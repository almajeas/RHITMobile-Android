package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.util.Log;

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
		OverlayItem overlay = new OverlayItem(location.center.asGeoPoint(), location.name, location.description);
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

}
