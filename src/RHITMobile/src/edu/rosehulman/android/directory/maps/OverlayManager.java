package edu.rosehulman.android.directory.maps;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.android.maps.Overlay;

/**
 * Manage common attributes and functionality between different overlays
 */
public class OverlayManager {
	
	private List<ManageableOverlay> overlays;
		
	/**
	 * Create a new OverlayManager
	 */
	public OverlayManager() {
		overlays = new ArrayList<ManageableOverlay>();
	}
	
	/**
	 * Clear the list of overlays that we are managing
	 */
	public void clear() {
		for (ManageableOverlay overlay : overlays) {
			overlay.setManager(null);
		}
		overlays.clear();
	}
	
	/**
	 * Adds an overlay to the manager
	 * 
	 * @param overlay The overlay to manage
	 */
	public void addOverlay(ManageableOverlay overlay) {
		this.overlays.add(overlay);
		overlay.setManager(new OverlayController(overlay));
	}
	
	/**
	 * Prune any overlays that are managed but not in the provided list
	 * 
	 * @param overlays The list of current overlays
	 */
	public void prune(List<Overlay> overlays) {
		List<ManageableOverlay> old = new LinkedList<ManageableOverlay>();
		for (ManageableOverlay overlay : this.overlays) {
			if (!overlays.contains(overlay)) {
				overlay.clearSelection();
				old.add(overlay);
			}
		}
		
		this.overlays.removeAll(old);		
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
