package edu.rosehulman.android.directory.tests.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import edu.rosehulman.android.directory.util.BoundingBox;
import edu.rosehulman.android.directory.util.BoundingPath;
import edu.rosehulman.android.directory.util.Point;

public class BoundingPathTests extends TestCase {
	
	private BoundingPath empty;
	private BoundingPath point;
	private BoundingPath line;
	private BoundingPath triangle;
	private BoundingPath quad;
	
	@Override
	public void setUp() {
		Point origin = new Point(0, 0);
		Point up = new Point(0, 5);
		Point diag = new Point(6, 6);
		Point right = new Point(4, -1);
		
		List<Point> points = new ArrayList<Point>();
		empty = new BoundingPath(new ArrayList<Point>(points));
		
		points.add(origin);
		point = new BoundingPath(new ArrayList<Point>(points));
		
		points.add(up);
		line = new BoundingPath(new ArrayList<Point>(points));
		
		points.add(diag);
		triangle = new BoundingPath(new ArrayList<Point>(points));
		
		points.add(right);
		quad = new BoundingPath(new ArrayList<Point>(points));
	}
	
	public void testGetBoundingBox() {
		assertEquals(null, empty.getBoundingBox());
		assertEquals(new BoundingBox(0, 0, 0, 0), point.getBoundingBox());
		assertEquals(new BoundingBox(0, 0, 5, 0), line.getBoundingBox());
		assertEquals(new BoundingBox(0, 6, 6, 0), triangle.getBoundingBox());
		assertEquals(new BoundingBox(0, 6, 6, -1), quad.getBoundingBox());
	}
	
	public void testIntersectsWithBoundingBox() {
		assertFalse(empty.intersects(new BoundingBox(-10, 10, 10, -10)));
		assertTrue(point.intersects(new BoundingBox(-1, 1, 1, -1)));
	}

}
