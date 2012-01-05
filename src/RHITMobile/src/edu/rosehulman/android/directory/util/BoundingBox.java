package edu.rosehulman.android.directory.util;

/**
 * Represents a bounding area in the form of a rectangle that 
 * can be intersected with other bounding areas.
 */
public class BoundingBox implements BoundingArea {
	
	/** The left coordinate */
	public int left;
	/** The right coordinate */
	public int right;
	/** The top coordinate */
	public int top;
	/** The bottom coordinate */
	public int bottom;
	
	/**
	 * Creates a new BoundingBox with the given coordinates
	 * 
	 * @param left min of two x parameters
	 * @param right max of two x parameters
	 * @param top max of two y parameters
	 * @param bottom min of two y parameters
	 */
	public BoundingBox(int left, int right, int top, int bottom) {
		assert(left <= right);
		assert(bottom <= top);
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}
	
	private boolean doesIntersect(int r1, int r2, int s1, int s2) {
		// Positive intersection
		// A: |--------|            |--------|
		// B:     |--------|          |----|
				
		// Negative intersection
		// A: |--------|         
		// B:          |--------|
		return !((s1 <= r1) && (s2 <= r1)) ^ ((s1 >= r2) && (s2 >= r2));
	}
	
	private boolean intersects(BoundingBox o) {
		return doesIntersect(left, right, o.left, o.right) &&
				doesIntersect(bottom, top, o.bottom, o.top);
	}
	
	public boolean intersects(BoundingArea other) {
		if (other instanceof BoundingPath) {
			//offload the comparison to the BoundingPath
			return other.intersects(this);
		}
		
		return this.intersects((BoundingBox)other);
	}
	
	/**
	 * Determine if this instance is equal to a BoundingBox
	 *  
	 * @param o The BoundingBox to compare to
	 * @return True if equal; false otherwise
	 */
	public boolean equals(BoundingBox o) {
		return (left == o.left) && (right == o.right) && 
			(top == o.top) && (bottom == o.bottom); 
	}
	
	@Override
	public boolean equals(Object other) {
		return this.equals((BoundingBox)other);
	}
	
	/**
	 * Determines if the given point is contained within this bounding box
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @return True if the point is contained within the bounding box
	 */
	public boolean contains(int x, int y) {
		return (x >= left) && (x <= right) && (y >= bottom) && (y <= top);
	}
	
	/**
	 * Computes the center point of the bounding box
	 * 
	 * @return The center of the region
	 */
	public Point getCenter() {
		return new Point((left + right)/2, (top + bottom) / 2);
	}

	/**
	 * Computes the width of the bounding box
	 * 
	 * @return The width of the bounding box
	 */
	public int getWidth() {
		return right - left;
	}

	/**
	 * Computes the height of the bounding box
	 * 
	 * @return The height of the bounding box
	 */
	public int getHeight() {
		return top - bottom;
	}
}
