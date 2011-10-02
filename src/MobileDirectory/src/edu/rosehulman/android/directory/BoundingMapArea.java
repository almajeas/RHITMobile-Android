package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;

import edu.rosehulman.android.directory.util.BoundingPath;
import edu.rosehulman.android.directory.util.Point;

/**
 * Implementation of BoundingPath that accepts GeoPoints
 * as parameters that can later be converted to XY points 
 * for a given Projection
 */
public class BoundingMapArea extends BoundingPath {

	private List<GeoPoint> geoPoints;
	
	/**
	 * Creates a new BoundingMapArea
	 * 
	 * Note that update must be called before the points
	 * contained in this bounding are are valid
	 * 
	 * @param points The GeoPoints to use when applying a projection
	 */
	public BoundingMapArea(List<GeoPoint> points) {
		super(null);
		geoPoints = points;
		this.points = new ArrayList<Point>(points.size());
		for (int i = 0; i < points.size(); i++) {
			this.points.add(new Point(0, 0));
		}
	}
	
	/**
	 * Update the XY coordinates for the given projection
	 * 
	 * @param proj The projection to apply to the GeoPoint objects
	 */
	public void update(Projection proj) {
		android.graphics.Point pt = new android.graphics.Point();
		for (int i = 0; i < geoPoints.size(); i++) {
			Point p = points.get(i);
			proj.toPixels(geoPoints.get(i), pt);
			p.x = pt.x;
			p.y = pt.y;
		}
	}

}
