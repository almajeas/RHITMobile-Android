package edu.rosehulman.android.directory.maps;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.MapView;

/**
 * Manage common attributes and functionality between different overlays
 */
public class OverlayManager {
	
	private MapView mapView;
	private List<ManageableOverlay> overlays;
		
	public OverlayManager(MapView mapView) {
		overlays = new ArrayList<ManageableOverlay>();
		this.mapView = mapView;
	}
	
	public void clear() {
		for (ManageableOverlay overlay : overlays) {
			overlay.setManager(null);
		}
		overlays.clear();
	}
	
	public void addOverlay(ManageableOverlay overlay) {
		this.overlays.add(overlay);
		overlay.setManager(new OverlayController(overlay));
	}
	
	private void markSelected(ManageableOverlay selected) {
		for (ManageableOverlay overlay : overlays) {
			if (overlay != selected) {
				overlay.clearSelection();
			}
		}
	}
	
	private class OverlayController implements OverlayManagerControl {
		
		private ManageableOverlay overlay;
		
		public OverlayController(ManageableOverlay overlay) {
			this.overlay = overlay;
		}

		@Override
		public void markSelected() {
			OverlayManager.this.markSelected(overlay);
		}
		
	}

}
