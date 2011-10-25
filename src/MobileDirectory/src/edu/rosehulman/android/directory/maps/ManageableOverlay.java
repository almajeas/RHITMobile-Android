package edu.rosehulman.android.directory.maps;

/**
 * An overlay, that is manageable by an OverlayManager
 */
public interface ManageableOverlay {

	/**
	 * Notifies the overlay that it should remove any notion of a selection 
	 */
	public void clearSelection();
	
	/**
	 * Set the OverlayManager to the given instance.
	 * 
	 * @param manager The OverlayManager hooks
	 */
	public void setManager(OverlayManagerControl manager);
	
}
