package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import edu.rosehulman.android.directory.model.MapArea;
import edu.rosehulman.android.directory.util.BoundingBox;

public class BuildingOverlayLayer extends Overlay {
	
	private List<BuildingOverlay> overlays;
	private Point pt;
	private BuildingOverlay selected;
	
	public BuildingOverlayLayer() {
		overlays = new ArrayList<BuildingOverlay>();
	}
	
	public void addMapArea(MapArea area) {
		BuildingOverlay overlay = new BuildingOverlay(area);
		
		overlays.add(overlay);
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		pt = mapView.getProjection().toPixels(p, pt);
		
		Point snapPoint = new Point();
		for (BuildingOverlay building : overlays) {
			if (building.onSnapToItem(pt.x, pt.y, snapPoint, mapView)) {
				BoundingBox bounds = building.getBounds();
				int spanLat = bounds.right - bounds.left;
				int spanLon = bounds.top - bounds.bottom;
				//TODO zoom to building
				//mapView.getController().zoomToSpan(spanLat, spanLon);
				mapView.getController().animateTo(mapView.getProjection().fromPixels(pt.x, pt.y));
				selected = building;
				mapView.invalidate();
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
		
		for (Overlay overlay : overlays) {
			overlay.draw(canvas, mapView, shadow);
		}
	}

}
