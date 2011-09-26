package edu.rosehulman.android.directory;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Join;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class TextOverlay extends Overlay {
	
	private static Paint outlinePaint;
	private static Paint textPaint;

	
	private GeoPoint location;
	private String text;
	
	public TextOverlay(GeoPoint loc, String text) {
		this.location = loc;
		this.text = text;
	}
	
	static {
		//copy the format of text labels on the MapView
		Typeface typeface = Typeface.DEFAULT;
 
		outlinePaint = new Paint();
		outlinePaint.setAntiAlias(true);
		outlinePaint.setSubpixelText(true);
		outlinePaint.setTextAlign(Align.CENTER);
		outlinePaint.setStrokeJoin(Join.ROUND);
		outlinePaint.setTypeface(typeface);
		outlinePaint.setTextSize(20.0f);
		outlinePaint.setColor(Color.BLACK);
		outlinePaint.setStyle(Paint.Style.STROKE);
	    outlinePaint.setStrokeWidth(6);
	    outlinePaint.setAlpha(200);
		
		textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setSubpixelText(true);
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setColor(Color.WHITE);
		textPaint.setTypeface(typeface);
		textPaint.setTextSize(20.0f);
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		//we don't have a shadow
		if (shadow) return;

		Point pt = mapView.getProjection().toPixels(location, null);
		canvas.drawText(text, pt.x, pt.y, outlinePaint);
		canvas.drawText(text, pt.x, pt.y, textPaint);
	}

}
