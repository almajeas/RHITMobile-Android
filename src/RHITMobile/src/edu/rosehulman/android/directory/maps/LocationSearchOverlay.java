package edu.rosehulman.android.directory.maps;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

import edu.rosehulman.android.directory.C;
import edu.rosehulman.android.directory.model.Location;

public class LocationSearchOverlay extends BalloonItemizedOverlay<OverlayItem> implements ManageableOverlay{

	private OverlayManagerControl manager;
	
	private List<SearchItem> items;
	
	private class SearchItem {
		public Location location;
		public OverlayItem overlay;
		
		public SearchItem(Location location, OverlayItem overlay) {
			this.location = location;
			this.overlay = overlay;
		}
	}
	
	public LocationSearchOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenterBottom(defaultMarker), mapView);
		items = new ArrayList<SearchItem>();
	}
	
	public void add(Location location) {
		OverlayItem overlay = new OverlayItem(location.center.asGeoPoint(), location.name, location.description);
		items.add(new SearchItem(location, overlay));
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return items.get(i).overlay;
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	public void clearSelection() {
		Log.d(C.TAG, "Clearing selection");
		this.setFocus(null);
	}

	@Override
	public void setManager(OverlayManagerControl manager) {
		this.manager = manager;
	}

}
