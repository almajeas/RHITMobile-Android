package edu.rosehulman.android.directory.util;

/**
 * Represents a bounding area in the form of a rectangle that 
 * can be intersected with other bounding areas.
 */
public class BoundingBox implements BoundingArea {
	
	private int left;
	private int right;
	private int top;
	private int bottom;
	
	/**
	 * Creates a new BoundingBox with the given coordinates
	 * 
	 * @param left min of two x parameters
	 * @param right max of two x parameters
	 * @param top max of two y parameters
	 * @param bottom min of two y parameters
	 */
	public BoundingBox(int left, int right, int top, int bottom) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}

	private boolean doesIntersect(int r1, int r2, int s1, int s2) {
		// Positive intersection
		// A: |--------|           |--------|           |--------|
		// B:     |--------|    |--------|                |----|
				
		// Negative intersection
		// A: |--------|                      |--------|
		// B:          |--------|    |--------|
		return !((s1 <= r1) && (s2 <= r1)) ^ ((s1 >= r2) && (s2 >= r2));
	}
	
	private boolean intersects(BoundingBox o) {
		return doesIntersect(left, right, o.left, o.right) &&
				doesIntersect(top, bottom, o.top, o.bottom);
	}
	
	public boolean intersects(BoundingArea other) {
		//TODO intersect with a path (by offloading to path)
		return this.intersects((BoundingBox)other);
	}

}
