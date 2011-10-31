package edu.rosehulman.android.directory.maps;

import java.util.ArrayList;
import java.util.List;

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
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.util.BoundingBox;

/**
 * An Overlay that contains building overlays
 */
public class BuildingOverlayLayer extends Overlay implements ManageableOverlay {
	
	private List<BuildingOverlay> overlays;
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
		overlays = new ArrayList<BuildingOverlay>();
		this.mapView = mapView;
	}
	
	/**
	 * Add a new location to the overlay
	 * 
	 * @param area The Location to add
	 */
	public void addMapArea(Location area) {
		BuildingOverlay overlay = new BuildingOverlay(area);
		overlays.add(overlay);
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		pt = mapView.getProjection().toPixels(p, pt);
		
		Point snapPoint = new Point();
		for (BuildingOverlay building : overlays) {
			if (building.onSnapToItem(pt.x, pt.y, snapPoint, mapView)) {
				GeoPoint dest = mapView.getProjection().fromPixels(snapPoint.x, snapPoint.y);
				setSelected(building);
				moveToSelected(dest);
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
	 * @return True if the ID was found; false otherwise
	 */
	public boolean setSelectedBuilding(long id) {
		if (id < 0) {
			setSelected(null);
			return true;
		}
		
		for (BuildingOverlay building : overlays) {
			if (building.getID() == id) {
				setSelected(building);
				moveToSelected(building.getLocation().center.asGeoPoint());
				return true;
			}
		}
		
		return false;
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
				balloon.setOnTapListener(new OnTapListener() {

					final Location loc = selected.getLocation();
					
					@Override
					public boolean onTap(View v) {
						new PopulateLocation(new Runnable() {
							
							@Override
							public void run() {
								Context context = mapView.getContext();
								context.startActivity(LocationActivity.createIntent(context, loc));
							}
						}).execute(loc);
						
						return true;
					}
				});
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
	
	private void moveToSelected(GeoPoint dest) {
		BoundingBox bounds = selected.getBounds();
		int spanLat = bounds.right - bounds.left;
		int spanLon = bounds.top - bounds.bottom;
		ViewController controller = new ViewController(mapView);
		Point center = new Point(mapView.getWidth() / 2, mapView.getHeight() / 4 * 3);
		controller.animateTo(dest, center, spanLat, spanLon);
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

}
