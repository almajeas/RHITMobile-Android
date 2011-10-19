package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.readystatesoftware.mapviewballoons.BalloonOverlayView;

import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.util.BoundingBox;

public class BuildingOverlayLayer extends Overlay {
	
	private List<BuildingOverlay> overlays;
	private Point pt;
	private BuildingOverlay selected;
	private MapView mapView;
	private BalloonOverlayView balloon;
	
	public BuildingOverlayLayer(MapView mapView) {
		overlays = new ArrayList<BuildingOverlay>();
		this.mapView = mapView;
	}
	
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

	public long getSelectedBuilding() {
		if (selected == null) {
			return -1;
		}
		
		return selected.getID();
	}
	
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

}
