package edu.rosehulman.android.directory;

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

public class TextOverlayLayer extends Overlay {
	
	private static final boolean DRAW_BOUNDS = false; 
	
	private List<TextOverlay> overlays;
	//TODO store outlines of buildings to check for collisions
	
	public TextOverlayLayer() {
		overlays = new ArrayList<TextOverlay>();
	}
	
	public void addOverlay(TextOverlay overlay) {
		overlays.add(overlay);
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		
		Projection projection = mapView.getProjection();
		
		for (TextOverlay overlay : overlays) {
			Rect bounds = overlay.getBounds(projection);
			
			if (DRAW_BOUNDS) {
				Paint blue = new Paint();
				blue.setColor(Color.BLUE);
				blue.setAlpha(200);
				blue.setStrokeWidth(2.0f);
				blue.setStyle(Style.STROKE);
				canvas.drawRect(bounds, blue);				
			}
			
			overlay.draw(canvas, mapView, shadow);
		}
	}

}
