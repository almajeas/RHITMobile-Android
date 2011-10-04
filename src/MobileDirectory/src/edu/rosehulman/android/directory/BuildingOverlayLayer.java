package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import edu.rosehulman.android.directory.model.MapArea;

public class BuildingOverlayLayer extends Overlay {
	
	private List<Overlay> overlays;
	
	public BuildingOverlayLayer() {
		overlays = new ArrayList<Overlay>();
	}
	
	public void addMapArea(MapArea area) {
		BuildingOverlay overlay = new BuildingOverlay(area);
		
		overlays.add(overlay);
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow) return;
		
		for (Overlay overlay : overlays) {
			overlay.draw(canvas, mapView, shadow);
		}
	}

}
