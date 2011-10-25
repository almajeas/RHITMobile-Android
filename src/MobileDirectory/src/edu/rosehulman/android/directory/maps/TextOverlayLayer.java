package edu.rosehulman.android.directory.maps;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import edu.rosehulman.android.directory.MyApplication;
import edu.rosehulman.android.directory.util.BoundingArea;
import edu.rosehulman.android.directory.util.BoundingBox;
import edu.rosehulman.android.directory.util.Region;

/**
 * Contains multiple text overlays, disallowing any to intersect
 * when rendered.
 */
public class TextOverlayLayer extends Overlay {
	
	private boolean drawBounds = false; 
	
	private List<TextOverlay> overlays;
	private List<BoundingMapArea> boundingMapAreas;
	private Region obstacleRegion;
	private Region textRegion;
	
	/**
	 * Creates a new TextOverlayLayer
	 */
	public TextOverlayLayer() {
		overlays = new ArrayList<TextOverlay>();
		boundingMapAreas = new ArrayList<BoundingMapArea>();
		obstacleRegion = new Region();
		textRegion = new Region();
		drawBounds = MyApplication.getInstance().betaManagerManager.shouldDrawDebug();
	}
	
	/**
	 * Adds an obstacle to the map where text should not be rendered
	 * 
	 * @param area The area to avoid
	 */
	public void addObstacle(BoundingMapArea area) {
		obstacleRegion.addArea(area);
		boundingMapAreas.add(area);
	}
	
	/**
	 * Adds a new text overlay to the layer
	 * @param overlay The overlay to add
	 */
	public void addOverlay(TextOverlay overlay) {
		overlays.add(overlay);
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
