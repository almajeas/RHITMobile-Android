package edu.rosehulman.android.directory.util;

import java.util.List;

public class BoundingPath implements BoundingArea {
	
	protected List<Point> points;
	
	public BoundingPath(List<Point> points) {
		this.points = points;
	}
	
	public BoundingBox getBoundingBox() {
		if (points == null || points.size() == 0) {
			return null;
		}
		Point first = points.get(0);
		int left = first.x;
		int right = first.x;
		int top = first.y;
		int bottom = first.y;
		
		for (Point pt : points) {
			if (pt.x < left)
				left = pt.x;
			else if (pt.x > right)
				right = pt.x;
			
			if (pt.y < bottom)
				bottom = pt.y;
			else if (pt.y > top)
				top = pt.y;
		}
		
		BoundingBox res = new BoundingBox(left, right, top, bottom);
		return res;
	}
	
	private boolean intersects(BoundingBox o) {
		BoundingBox box = getBoundingBox();
		if (box == null)
			return false;
		return box.intersects(o);
	}
	
	private boolean intersects(BoundingPath o) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean intersects(BoundingArea other) {
		if (other instanceof BoundingBox) {
			return this.intersects((BoundingBox)other);
		} else {
			return this.intersects((BoundingPath)other);
		}
	}

}
