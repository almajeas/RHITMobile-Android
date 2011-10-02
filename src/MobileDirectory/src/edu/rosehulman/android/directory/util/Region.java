package edu.rosehulman.android.directory.util;

import java.util.ArrayList;
import java.util.List;

public class Region {
	
	private List<BoundingArea> boundingAreas;
	
	/**
	 * Create a new, empty Region
	 */
	public Region() {
		boundingAreas = new ArrayList<BoundingArea>();
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
	 * Removes all areas from the region
	 */
	public void clear() {
		boundingAreas.clear();
	}
	
	/**
	 * Check if an area intersects with this region
	 * 
	 * @param area The area to check agains
	 * @return True if the area intersects the region
	 */
	public boolean intersects(BoundingArea area) {
		for (BoundingArea other : boundingAreas) {
			if (other.intersects(area))
				return true;
		}
		return false;
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
		if (intersects(area))
			return false;
		
		//we didn't intersect with any previous area, add it to the collection
		addArea(area);
		return true;
	}

}
