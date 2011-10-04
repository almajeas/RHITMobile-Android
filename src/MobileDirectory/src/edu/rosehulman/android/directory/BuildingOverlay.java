package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import edu.rosehulman.android.directory.model.MapArea;
import edu.rosehulman.android.directory.util.BoundingBox;
import edu.rosehulman.android.directory.util.BoundingPath;
import edu.rosehulman.android.directory.util.Point;

public class BuildingOverlay extends Overlay implements Overlay.Snappable {
	
	private MapArea mapArea;
	
	private BoundingPath path;
	private BoundingBox bounds;

	private android.graphics.Point pt;
	
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
		
		List<Point> points = new ArrayList<Point>(mapArea.corners.length);
		for (int i = 0; i < mapArea.corners.length; i++) {
			points.add(new Point(mapArea.corners[i].lat, mapArea.corners[i].lon));
		}
		path = new BoundingPath(points);
		bounds = path.getBoundingBox();
	}
	
	public BoundingBox getBounds() {
		return bounds;
	}
	
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
	public boolean onSnapToItem(int x, int y, android.graphics.Point snapPoint, MapView mapView) {
		Projection proj = mapView.getProjection();
		GeoPoint geoPt = proj.fromPixels(x, y);
		
		if (bounds.contains(geoPt.getLatitudeE6(), geoPt.getLongitudeE6())) {
			proj.toPixels(mapArea.center.asGeoPoint(), snapPoint);
			return true;
		}
		return false;
	}

}
