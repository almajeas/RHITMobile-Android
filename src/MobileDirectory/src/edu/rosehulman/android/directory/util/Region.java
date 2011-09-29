package edu.rosehulman.android.directory.util;

import java.util.LinkedList;
import java.util.List;

public class Region {
	
	private List<BoundingArea> boundingAreas;
	
	public Region() {
		boundingAreas = new LinkedList<BoundingArea>();
	}
	
	public boolean intersect(BoundingArea area) {
		for (BoundingArea other : boundingAreas) {
			if (other.intersects(area))
				return false;
		}
		
		//we didn't intersect with any previous area, add it to the collection
		boundingAreas.add(area);
		return true;
	}

}
