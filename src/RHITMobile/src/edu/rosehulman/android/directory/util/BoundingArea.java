package edu.rosehulman.android.directory.util;

/**
 * Common functionality between different bounding areas
 * 
 * Primary implementations include BoundingBox and BoundingPath
 */
public interface BoundingArea {
	
	/**
	 * Determines if this bounding area intersects with another bounding area
	 * 
	 * @param other the bounding area to compare this object with
	 * @return True if the two bounding areas intersect; otherwise, False
	 */
	public boolean intersects(BoundingArea other);

}
