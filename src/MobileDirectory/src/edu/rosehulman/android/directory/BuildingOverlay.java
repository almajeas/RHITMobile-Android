package edu.rosehulman.android.directory;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import edu.rosehulman.android.directory.model.MapArea;

public class BuildingOverlay extends Overlay implements Overlay.Snappable {
	
	private MapArea mapArea;
	
	//private BoundingPath path;
	//private BoundingBox bounds;
	
	
	private static Paint paintFill;
	private static Paint paintStroke;
	
	static {
		paintFill = new Paint();
		paintFill.setColor(Color.WHITE);
		paintFill.setAlpha(50);
		paintFill.setStrokeJoin(Join.ROUND);
		paintFill.setStrokeWidth(5.0f);
		paintFill.setStyle(Style.FILL);	
		
		paintStroke = new Paint(paintFill);
		paintStroke.setAlpha(150);
		paintStroke.setStyle(Style.STROKE);		
	}
	
	public BuildingOverlay(MapArea mapArea) {
		this.mapArea = mapArea;
		
		//List<Point> points = new ArrayList<Point>(mapArea.corners.length);
		//for (int i = 0; i < mapArea.corners.length; i++) {
		//	points.add(new Point(mapArea.corners[i].lat, mapArea.corners[i].lon));
		//}
		//path = new BoundingPath(points);
		//bounds = path.getBoundingBox();
	}
	
	private Point pt;
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow) return;
		
		Projection proj = mapView.getProjection();
		
		Path path = new Path();
		pt = proj.toPixels(mapArea.corners[0].asGeoPoint(), pt);
		path.moveTo(pt.x, pt.y);
		
		for (int i = 1; i < mapArea.corners.length; i++) {
			proj.toPixels(mapArea.corners[i].asGeoPoint(), pt);
			path.lineTo(pt.x, pt.y);
		}
		proj.toPixels(mapArea.corners[0].asGeoPoint(), pt);
		path.lineTo(pt.x, pt.y);
	
		canvas.drawPath(path, paintFill);		
		canvas.drawPath(path, paintStroke);
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		Point pt = mapView.getProjection().toPixels(p, null);
		Point snapPoint = new Point();
		boolean snap = onSnapToItem(pt.x, pt.y, snapPoint, mapView);
		
		if (snap) {
			mapView.getController().animateTo(mapView.getProjection().fromPixels(snapPoint.x, snapPoint.y));
		}
		
		return snap;
	}

	@Override
	public boolean onSnapToItem(int x, int y, Point snapPoint, MapView mapView) {
		//Point pt1 = mapView.getProjection().toPixels(topLeft, null);
		//Point pt2 = mapView.getProjection().toPixels(bottomRight, null);
		
		//boolean snap = (pt1.x <= x) && (x <= pt2.x) && (pt1.y <= y) && (y <= pt2.y);
		
		//snapPoint.x = (pt1.x + pt2.x) / 2;
		//snapPoint.y = (pt1.y + pt2.y) / 2;
		
		//return snap;
		return false;
	}

}
