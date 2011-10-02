package edu.rosehulman.android.directory.tests.util;

import junit.framework.TestCase;
import edu.rosehulman.android.directory.util.BoundingBox;

public class BoundingBoxTests extends TestCase {
	
	private BoundingBox left;
	private BoundingBox right;
	
	private BoundingBox cornerLeft;	
	private BoundingBox touchingLeft;
	
	private BoundingBox straddleBoth;
	private BoundingBox insideLeft;
	
	@Override
	public void setUp() {
		left = new BoundingBox(-10, 0, 10, 0);
		right = new BoundingBox(1, 5, 5, -10);
		cornerLeft = new BoundingBox(-2, 5, 15, 8);
		touchingLeft = new BoundingBox(0, 5, 0, -5);
		
		straddleBoth = new BoundingBox(-5, 3, 4, 1);
		insideLeft = new BoundingBox(-7, -3, 9, 1);
	}
	
	private void showNoIntersection(BoundingBox o1, BoundingBox o2) {
		assertFalse(o1.intersects(o2));
		assertFalse(o2.intersects(o1));
	}
	
	private void showIntersection(BoundingBox o1, BoundingBox o2) {
		assertTrue(o1.intersects(o2));
		assertTrue(o2.intersects(o1));
	}
	
	public void testNoIntersection() {
		showNoIntersection(left, right);
		showNoIntersection(left, touchingLeft);
		showNoIntersection(insideLeft, right);
	}
	
	public void testIntersection() {
		showIntersection(left, insideLeft);
		showIntersection(left, straddleBoth);
		showIntersection(right, straddleBoth);
		showIntersection(left, cornerLeft);
	}
	
	public void testSelfIntersection() {
		assertTrue(left.intersects(left));
		assertTrue(right.intersects(right));
		assertTrue(touchingLeft.intersects(touchingLeft));
		assertTrue(straddleBoth.intersects(straddleBoth));
		assertTrue(insideLeft.intersects(insideLeft));
	}

}
