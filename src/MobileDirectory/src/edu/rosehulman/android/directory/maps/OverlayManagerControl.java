package edu.rosehulman.android.directory.maps;

/**
 * Hook into an OverlayManager provided to ManageableOverlay objects
 * once added to an OverlayManager
 */
public interface OverlayManagerControl {
	
	/**
	 * Notify the OverlayManager that the overlay has taken focus
	 */
	public void markSelected();
}
