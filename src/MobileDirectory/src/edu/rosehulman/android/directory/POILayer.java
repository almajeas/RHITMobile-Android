package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

import edu.rosehulman.android.directory.model.Location;

public class POILayer extends ItemizedOverlay<OverlayItem> {
	
	private List<OverlayItem> poi;

	public POILayer(Drawable defaultMarker) {
		super(boundCenter(defaultMarker));
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
	protected boolean onTap(int index) {
		Log.d(C.TAG, "Tapped: " + index);
		return true;
	}

}
