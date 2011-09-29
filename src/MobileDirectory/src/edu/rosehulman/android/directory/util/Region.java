package edu.rosehulman.android.directory.util;

import java.util.LinkedList;
import java.util.List;

public class Region {
	
	private List<BoundingArea> boundingAreas;
	
	/**
	 * Create a new, empty Region
	 */
	public Region() {
		boundingAreas = new LinkedList<BoundingArea>();
	}
	
	/**
	 * Add a bounding area to this region
	 * 
	 * @param area The area to add to the region
	 */
	public void addArea(BoundingArea area) {
		boundingAreas.add(area);
	}
	
	/**
	 * Check if an area intersects with this region
	 * and add it to the area if it does not.
	 * 
	 * @param area The area to check against and
	 * potentially add to this region
	 * @return True if the area did not intersect with the region
	 * and was added
	 */
	public boolean intersect(BoundingArea area) {
		for (BoundingArea other : boundingAreas) {
			if (other.intersects(area))
				return false;
		}
		
		//we didn't intersect with any previous area, add it to the collection
		addArea(area);
		return true;
	}

}
