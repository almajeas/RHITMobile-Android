package edu.rosehulman.android.directory.maps;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

import edu.rosehulman.android.directory.C;
import edu.rosehulman.android.directory.LocationActivity;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.tasks.PopulateLocation;

/**
 * Overlay containing search results
 */
public class LocationSearchLayer extends BalloonItemizedOverlay<OverlayItem> implements ManageableOverlay{

	private static final int MIN_ZOOM_LEVEL = 19;
	
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
	
	/**
	 * Creates a new search layer
	 * 
	 * @param defaultMarker The marker to use for a search result
	 * @param mapView The associated MapView
	 */
	public LocationSearchLayer(Drawable defaultMarker, MapView mapView) {
		super(boundCenterBottom(defaultMarker), mapView);
		setShowClose(false);
		setShowDisclosure(true);
		
		items = new ArrayList<SearchItem>();
	}
	
	/**
	 * Add a new location to the layer
	 * 
	 * @param location The location to add
	 */
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
	protected void animateTo(int index, GeoPoint center) {
		if (manager != null) {
			manager.markSelected();
		}
		MapView mapView = getMapView();
		ViewController controller = new ViewController(mapView);
		Point pt = new Point(mapView.getWidth() / 2, mapView.getHeight() / 4 * 3);
		controller.animateTo(center, pt, MIN_ZOOM_LEVEL + 1, true);
	}

	@Override
	protected boolean onBalloonTap(int index, OverlayItem item) {
		final Location loc = items.get(index).location;
		
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
	public void clearSelection() {
		Log.d(C.TAG, "Clearing selection");
		this.setFocus(null);
	}

	@Override
	public void setManager(OverlayManagerControl manager) {
		this.manager = manager;
	}

}
