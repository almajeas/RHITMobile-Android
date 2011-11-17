package edu.rosehulman.android.directory.maps;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import edu.rosehulman.android.directory.MyApplication;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.util.BoundingArea;
import edu.rosehulman.android.directory.util.BoundingBox;
import edu.rosehulman.android.directory.util.Region;

/**
 * Contains multiple text overlays, disallowing any to intersect
 * when rendered.
 */
public class TextOverlayLayer extends Overlay {
	
	private boolean drawBounds = false; 
	
	private static List<TextOverlay> overlays;
	private static List<BoundingMapArea> boundingMapAreas;
	private static Region obstacleRegion;
	private Region textRegion;
	
	/**
	 * Initialize data that needs to be loaded by the database.
	 * 
	 * This method must be called before attempting to create an instance
	 * of \ref TextOverlayLayer
	 */
	public static void initializeCache() {
		if (overlays != null)
			return;
		
		synchronized (TextOverlayLayer.class) {
			if (overlays != null)
				return;

			overlays = new ArrayList<TextOverlay>();
			boundingMapAreas = new ArrayList<BoundingMapArea>();
			obstacleRegion = new Region();
			
			LocationAdapter buildingAdapter = new LocationAdapter();
	        buildingAdapter.open();
	        
	        //build out text overlays
	        Cursor buildingOverlays = buildingAdapter.getBuildingOverlayCursor(false);
	        int iId = buildingOverlays.getColumnIndex("_Id");
	        int iName = buildingOverlays.getColumnIndex("Name");
	        int iLat = buildingOverlays.getColumnIndex("CenterLat");
	        int iLon = buildingOverlays.getColumnIndex("CenterLon");
	        int iMinZoomLevel = buildingOverlays.getColumnIndex("MinZoomLevel");
	        while (buildingOverlays.moveToNext()) {
	        	String name = buildingOverlays.getString(iName);
	        	int minZoomLevel = buildingOverlays.getInt(iMinZoomLevel);
	        	GeoPoint pt = new GeoPoint(buildingOverlays.getInt(iLat), buildingOverlays.getInt(iLon));
	        	overlays.add(new TextOverlay(pt, name, minZoomLevel));
	        } while (buildingOverlays.moveToNext());
	        buildingOverlays.close();
	        
	        //add our building obstacles to the text layer
	        buildingOverlays = buildingAdapter.getBuildingOverlayCursor(true);
	        iId = buildingOverlays.getColumnIndex("_Id");
	        while (buildingOverlays.moveToNext()) {
	        	int buildingId = buildingOverlays.getInt(iId);
	        	Cursor buildingPoints = buildingAdapter.getBuildingCornersCursor(buildingId);
	            iLat = buildingPoints.getColumnIndex("Lat");
	            iLon = buildingPoints.getColumnIndex("Lon");
	        	List<GeoPoint> pts = new ArrayList<GeoPoint>(buildingPoints.getCount());
	        	while (buildingPoints.moveToNext()) {
	        		int lat = buildingPoints.getInt(iLat);
	        		int lon = buildingPoints.getInt(iLon);
	        		pts.add(new GeoPoint(lat, lon));
	        	}
	        	buildingPoints.close();
	        	BoundingMapArea boundingMapArea = new BoundingMapArea(pts);
	        	obstacleRegion.addArea(boundingMapArea);
	        	boundingMapAreas.add(boundingMapArea);
	        }
	        buildingOverlays.close();
	        
	        buildingAdapter.close();
		}
	}
	
	/**
	 * Creates a new TextOverlayLayer
	 */
	public TextOverlayLayer() {
		textRegion = new Region();
		drawBounds = MyApplication.getInstance().betaManagerManager.shouldDrawDebug();
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		
		//update the bounding map areas
		Projection projection = mapView.getProjection();
		for (BoundingMapArea boundingMapArea : boundingMapAreas) {
			boundingMapArea.update(projection);
		}
		
		textRegion.clear();
		
		if (drawBounds) {
			for (BoundingMapArea boundingMapArea : boundingMapAreas) {
				BoundingBox box = boundingMapArea.getBoundingBox();
				
				if (box == null)
					continue;
				
				Paint red = new Paint();
				red.setColor(Color.RED);
				red.setAlpha(200);
				red.setStrokeWidth(2.0f);
				red.setStyle(Style.STROKE);
				canvas.drawRect(box.left, box.bottom, box.right, box.top, red);				
			}
		}
		
		for (TextOverlay overlay : overlays) {
			Rect bounds = overlay.getBounds(projection);
			
			if (drawBounds) {
				Paint blue = new Paint();
				blue.setColor(Color.BLUE);
				blue.setAlpha(200);
				blue.setStrokeWidth(2.0f);
				blue.setStyle(Style.STROKE);
				canvas.drawRect(bounds, blue);				
			}
			
			if (!overlay.isVisible(mapView))
				continue;
			
			BoundingArea textArea = new BoundingBox(bounds.left, bounds.right, bounds.top, bounds.bottom); 
			
			if (obstacleRegion.intersects(textArea))
				continue;
			if (!textRegion.intersect(textArea))
				continue;
			
			overlay.draw(canvas, mapView, shadow);
		}
	}

}
