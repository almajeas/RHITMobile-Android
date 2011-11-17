package edu.rosehulman.android.directory.maps;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.readystatesoftware.mapviewballoons.BalloonOverlayView;
import com.readystatesoftware.mapviewballoons.BalloonOverlayView.OnTapListener;

import edu.rosehulman.android.directory.LocationActivity;
import edu.rosehulman.android.directory.db.DbIterator;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.util.BoundingBox;

/**
 * An Overlay that contains building overlays
 */
public class BuildingOverlayLayer extends Overlay implements ManageableOverlay {
	
	private static Map<Long, BuildingOverlay> overlays;
	private Point pt;
	private BuildingOverlay selected;
	private MapView mapView;
	private BalloonOverlayView balloon;
	
	private OverlayManagerControl manager;
	
	/**
	 * Create a new BuildingOverlayLayer
	 * 
	 * @param mapView The MapView that will contain this overlay
	 */
	public BuildingOverlayLayer(MapView mapView) {
		this.mapView = mapView;
		
		if (overlays == null) {
			throw new RuntimeException("Attempted to use overlay layer without an initialized cache");
		}
	}
	
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
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		pt = mapView.getProjection().toPixels(p, pt);
		
		Point snapPoint = new Point();
		for (BuildingOverlay building : overlays.values()) {
			if (building.onSnapToItem(pt.x, pt.y, snapPoint, mapView)) {
				GeoPoint dest = mapView.getProjection().fromPixels(snapPoint.x, snapPoint.y);
				setSelected(building);
				moveToSelected(dest, true);
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
	public boolean setSelectedBuilding(long id, boolean animate) {
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
				balloon = new BalloonOverlayView(mapView.getContext(), 0);	
				balloon.setOnTapListener(balloonTapListener);
			}
			
			balloon.setVisibility(View.GONE);
			
			GeoPoint point = location.center.asGeoPoint();
			MapView.LayoutParams params = new MapView.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, point,
					MapView.LayoutParams.BOTTOM_CENTER);
			params.mode = MapView.LayoutParams.MODE_MAP;
			balloon.setText(location.name, location.description);
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
	
	private OnTapListener balloonTapListener = new OnTapListener() {
		
		@Override
		public boolean onTap(View v) {
			
			final Location loc = selected.getLocation();
			
			new PopulateLocation(new Runnable() {
				
				@Override
				public void run() {
					Context context = mapView.getContext();
					context.startActivity(LocationActivity.createIntent(context, loc));
				}
			}).execute(loc);
			
			return true;
		}
	};

}
