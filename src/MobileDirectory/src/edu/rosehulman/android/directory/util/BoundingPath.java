package edu.rosehulman.android.directory.util;

import java.util.List;

import edu.rosehulman.android.directory.model.LatLon;

public class BoundingPath implements BoundingArea {
	
	private List<LatLon> points;

	@Override
	public boolean intersects(BoundingArea other) {
		// TODO Auto-generated method stub
		return false;
	}

}
