package edu.rosehulman.android.directory.maps;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

import edu.rosehulman.android.directory.CampusToursOffCampusActivity;
import edu.rosehulman.android.directory.LocationActivity;
import edu.rosehulman.android.directory.R;
import edu.rosehulman.android.directory.TaskManager;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.util.BoundingBox;

/**
 * Overlay containing markers for tour nodes
 */
public class OffsiteTourLayer extends BalloonItemizedOverlay<OverlayItem> implements ManageableOverlay {
	
	public static final int REQUEST_DIRECTIONS_LIST = 2;
	
	public interface UIListener {
		public void setPrevButtonEnabled(boolean enabled);
		public void setNextButtonEnabled(boolean enabled);
		void startActivityForResult(Intent intent, int requestCode);
	}

	private static Drawable transparent;
	
	private OverlayManagerControl manager;
	private UIListener uiListener;
	
	public Location[] locations;
	public BoundingBox bounds = null;
	
	private boolean animate = true;

	public OffsiteTourLayer(MapView mapView, TaskManager taskManager, Location[] locations, UIListener uiListener) {
		super(boundCenter(mapView.getResources().getDrawable(R.drawable.tour_node_marker)), mapView);
		
		this.locations = locations;
		this.uiListener = uiListener;

		if (transparent == null) {
			transparent = getMapView().getResources().getDrawable(android.R.color.transparent);
		}
		
		for (Location location : locations) {
			BoundingBox b;
			
			if (location.mapData == null) {
				int lon = location.center.lon;
				int lat= location.center.lat;
				b = new BoundingBox(lat, lat, lon, lon);
			} else {
				b = location.mapData.getBounds();
			}

			if (bounds == null) {
				bounds = b;
			} else {
				bounds = bounds.union(b);
			}
		}

		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		OverlayItem overlay;
		
		Location loc = locations[i];
		overlay = new OverlayItem(loc.center.asGeoPoint(), loc.name, loc.description);

		return overlay;
	}

	@Override
	public int size() {
		return locations.length;
	}
	
	@Override
	protected boolean onBalloonTap(int index, OverlayItem item) {
		Context context = getMapView().getContext();
		Intent intent = LocationActivity.createIntent(context, locations[index]);
		context.startActivity(intent);
		
		return true;
	}
	
	@Override
	protected void animateTo(int index, GeoPoint center) {
		if (manager != null) {
			manager.markSelected();
		}
		MapView mapView = getMapView();
		ViewController controller = new ViewController(mapView);
		Point pt = new Point(mapView.getWidth() / 2, mapView.getHeight() / 2);
		int spanLat = bounds.top - bounds.bottom;
		int spanLon = bounds.left - bounds.right;
		controller.animateTo(center, pt, spanLat, spanLon, animate);
	}
	
	@Override
	public void clearSelection() {
		this.setFocus(null);
	}

	@Override
	public void setManager(OverlayManagerControl manager) {
		this.manager = manager;
	}
	
	public void stepNext() {
		int maxStep = locations.length-1;
		int currentStep = getLastFocusedIndex();
		currentStep = Math.min(currentStep+1, maxStep);
		if (currentStep == maxStep) {
			uiListener.setNextButtonEnabled(false);
		}
		uiListener.setPrevButtonEnabled(true);
		focus(currentStep, true);
	}
	
	public void stepPrevious() {
		int minStep = 0;
		int currentStep = getLastFocusedIndex();
		currentStep = Math.max(currentStep-1, minStep);
		if (currentStep == minStep) {
			uiListener.setPrevButtonEnabled(false);
		}
		uiListener.setNextButtonEnabled(true);
		focus(currentStep, true);
	}
	
	/**
	 * Focus a particular step
	 * 
	 * @param step The index of the step to focus
	 * @param animate Whether the focus operation should animate or not
	 * 
	 * @return True if the step was focused
	 */
	public boolean focus(int step, boolean animate) {
		if (step == -1) {
			this.setFocus(null);
			return true;
		}
		
		this.animate = animate;
		this.setLastFocusedIndex(step);
		this.onTap(step);
		this.animate = true;
		
		return true;
	}
	
	/**
	 * Shows the list of directions.
	 * 
	 * @param step The step to move to, or -1
	 */
	public void showDirectionsList(int step) {
		Context context = getMapView().getContext();
		Intent intent = CampusToursOffCampusActivity.createIntent(context, locations);
		context.startActivity(intent);
	}
	
}
