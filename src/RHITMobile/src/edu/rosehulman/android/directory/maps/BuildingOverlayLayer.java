package edu.rosehulman.android.directory.maps;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonOverlayView;
import com.readystatesoftware.mapviewballoons.R;

import edu.rosehulman.android.directory.db.DbIterator;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.util.BoundingBox;

/**
 * An Overlay that contains building overlays
 */
public class BuildingOverlayLayer extends Overlay implements ManageableOverlay {
	
	/**
	 * Useful event hooks for the \ref BuildingOverlayLayer
	 */
	public interface OnBuildingSelectedListener {
		/**
		 * Called when the location is selected. The overlay will
		 * respond by animating to the location and displaying a balloon
		 * with the title and description
		 * 
		 * @param location The location that was selected
		 */
		public void onSelect(Location location);
		
		/**
		 * Called when a balloon associated with a location is tapped.
		 * 
		 * @param location The location that was tapped
		 */
		public void onTap(Location location);
	}
	
	private static Map<Long, BuildingOverlay> overlays;
	private Point pt;
	private BuildingOverlay selected;
	private MapView mapView;
	private BalloonOverlayView<OverlayItem> balloon;
	
	private OverlayManagerControl manager;
	
	private OnBuildingSelectedListener listener;

	/**
	 * Initialize data that needs to be loaded by the database.
	 * 
	 * This method must be called before attempting to create an instance
	 * of \ref BuildingOverlayLayer
	 */
	public static void initializeCache() {
		if (overlays != null)
			return;
		
		synchronized (BuildingOverlayLayer.class) {
			if (overlays != null)
				return;

			overlays = new HashMap<Long, BuildingOverlay>();
			
	    	LocationAdapter locationAdapter = new LocationAdapter();
	    	locationAdapter.open();
	    	
	    	DbIterator<Location> iterator = locationAdapter.getBuildingIterator();
	    	while (iterator.hasNext()) {
	    		Location location = iterator.getNext();
	    		locationAdapter.loadMapArea(location, true);
	    		BuildingOverlay overlay = new BuildingOverlay(location);
	    		overlays.put(location.id, overlay);
	    	}
	    	
	    	locationAdapter.close();
		}
	}
	
	/**
	 * Create a new BuildingOverlayLayer
	 * 
	 * @param mapView The MapView that will contain this overlay
	 * @param listener The listener to handle events related to this overlay layer
	 */
	public BuildingOverlayLayer(MapView mapView, OnBuildingSelectedListener listener) {
		this.mapView = mapView;
		this.listener = listener;
		
		if (overlays == null) {
			throw new RuntimeException("Attempted to use overlay layer without an initialized cache");
		}
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		pt = mapView.getProjection().toPixels(p, pt);
		
		Point snapPoint = new Point();
		for (BuildingOverlay building : overlays.values()) {
			if (building.onSnapToItem(pt.x, pt.y, snapPoint, mapView)) {
				GeoPoint dest = mapView.getProjection().fromPixels(snapPoint.x, snapPoint.y);
				setSelected(building);
				moveToSelected(dest, true);
				if (listener != null) {
					listener.onSelect(building.getLocation());
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow) return;
		
		if (selected != null) {
			selected.draw(canvas, mapView, shadow);
		}
		
		/*
		for (Overlay overlay : overlays) {
			overlay.draw(canvas, mapView, shadow);
		}
		*/
	}

	/**
	 * Gets the ID of the currently selected building
	 * 
	 * @return The ID of the currently selected building, or -1
	 */
	public long getSelectedBuilding() {
		if (selected == null) {
			return -1;
		}
		
		return selected.getID();
	}
	
	/**
	 * Sets the ID of the currently selected building. 
	 * Also animates to the selected building
	 * 
	 * @param id The ID of the building to select, or -1 to clear the selection
	 * @param animate True if the building should be animated to
	 * 
	 * @return True if the building was found and selected
	 */
	public boolean focus(long id, boolean animate) {
		if (id < 0) {
			setSelected(null);
			return true;
		}
		
		BuildingOverlay building = overlays.get(id);
		if (building == null) {
			return false;
		}
		
		setSelected(building);
		moveToSelected(building.getLocation().center.asGeoPoint(), animate);
		return true;
	}
	
	private void setSelected(BuildingOverlay overlay) {
		selected = overlay;
		
		if (overlay == null) {
			if (balloon != null) {
				balloon.setVisibility(View.GONE);
			}
		} else {
			Location location = overlay.getLocation();
			boolean recycle = (balloon != null);
			if (!recycle) {
				balloon = new BalloonOverlayView<OverlayItem>(mapView.getContext(), 0);	
				balloon.findViewById(R.id.balloon_close).setVisibility(View.GONE);
				balloon.findViewById(R.id.balloon_disclosure).setVisibility(View.VISIBLE);
				balloon.findViewById(R.id.balloon_inner_layout).setOnTouchListener(balloonTapListener);
			}
			
			balloon.setVisibility(View.GONE);
			
			GeoPoint point = location.center.asGeoPoint();
			MapView.LayoutParams params = new MapView.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, point,
					MapView.LayoutParams.BOTTOM_CENTER);
			params.mode = MapView.LayoutParams.MODE_MAP;
			balloon.setData(new OverlayItem(null, location.name, location.description));
			balloon.setVisibility(View.VISIBLE);
			
			if (recycle) {
				balloon.setLayoutParams(params);
			} else {
				mapView.addView(balloon, params);
			}
			
			if (manager != null) {
				manager.markSelected();
			}
		}
		
	}
	
	private void moveToSelected(GeoPoint dest, boolean animate) {
		BoundingBox bounds = selected.getBounds();
		int spanLat = bounds.right - bounds.left;
		int spanLon = bounds.top - bounds.bottom;
		ViewController controller = new ViewController(mapView);
		Point center = new Point(mapView.getWidth() / 2, mapView.getHeight() / 4 * 3);
		controller.animateTo(dest, center, spanLat, spanLon, animate);
		mapView.invalidate();
	}

	@Override
	public void clearSelection() {
		setSelected(null);
	}

	@Override
	public void setManager(OverlayManagerControl manager) {
		this.manager = manager;
	}
	
	
	private OnTouchListener balloonTapListener = new OnTouchListener() {

		private void dispatchTap() {
			Location loc = selected.getLocation();
			
			if (listener != null) {
				listener.onTap(loc);
			}
		}
		
		float startX;
		float startY;
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			View l =  ((View)v.getParent()).findViewById(R.id.balloon_main_layout);
			Drawable d = l.getBackground();
			
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				int[] states = {android.R.attr.state_pressed};
				if (d.setState(states)) {
					d.invalidateSelf();
				}
				startX = event.getX();
				startY = event.getY();
				return true;
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				int newStates[] = {};
				if (d.setState(newStates)) {
					d.invalidateSelf();
				}
				if (Math.abs(startX - event.getX()) < 40 && 
						Math.abs(startY - event.getY()) < 40 ) {
					// call overridden method
					dispatchTap();
				}
				return true;
			} else {
				return false;
			}
			
		}
	};

}
