package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Join;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * MapView overlay that renders a string, wrapped in a reasonable way
 */
public class TextOverlay extends Overlay {
	
	private static Paint outlinePaint;
	private static Paint textPaint;

	private GeoPoint location;
	
	private List<String> lines;
	private float offsets[];
	
	private Rect bounds;
	private Rect offsetBounds;
	
	private int minZoomLevel;
	
	/**
	 * Creates a new TextOverlay
	 * 
	 * @param loc The lat/long that the text should appear
	 * @param text The text to render
	 * @param minZoomLevel The minimum zoom level to render the text at
	 */
	public TextOverlay(GeoPoint loc, String text, int minZoomLevel) {
		this.location = loc;
		this.minZoomLevel = minZoomLevel;
		
		bounds = new Rect();
		offsetBounds = new Rect();
		
		lines = splitText(text);
		offsets = calcOffsets(lines, bounds);
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
	
	/**
	 * Determine the bounds of rendered text with a given projection
	 * 
	 * @param projection The projection to use when computing the bounds
	 * @return a new Rect with the bounds of the rendered text
	 */
	public Rect getBounds(Projection projection) {
		Point pt = projection.toPixels(location, null);
		offsetBounds.left = bounds.left + pt.x;
		offsetBounds.right = bounds.right + pt.x;
		offsetBounds.top = bounds.top + pt.y;
		offsetBounds.bottom = bounds.bottom + pt.y;
		return offsetBounds;
	}
	
	/**
	 * Determine if the text will be visible on the given MapView
	 * 
	 * @param mapView The MapView to check against
	 * @return True if the overlay will be visible
	 */
	public boolean isVisible(MapView mapView) {
		return mapView.getZoomLevel() >= minZoomLevel;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		//we don't have a shadow
		if (shadow) return;
		
		if (!isVisible(mapView))
			return;
		
		Point pt = mapView.getProjection().toPixels(location, null);
		for (int i = 0; i < lines.size(); i++) {
			float y = pt.y + offsets[i];
			String line = lines.get(i);
			canvas.drawText(line, pt.x, y, outlinePaint);
			canvas.drawText(line, pt.x, y, textPaint);
		}
	}
	
	private static List<String> splitText(String text) {
		String words[] = text.split(" ");
		List<String> lines = new ArrayList<String>();
		String line = null;
		int charCount = 0;
		for (String word : words) {
			if (charCount == 0) {
				//always include at least 1 word in a line
				line = word;
				charCount = word.length();
			} else if (charCount + 1 + word.length() > 12) {
				//if adding the word would make the line too long, 
				//save off what we have first
				lines.add(line);
				line = word;
				charCount = 0;
			} else {
				//append the word to the line
				line += " " + word;
				charCount += 1 + word.length();
			}
		}
		//store the last line
		lines.add(line);
		
		return lines;
	}
	
	private static float[] calcOffsets(List<String> lines, Rect bounds) {
		Rect lineBounds = new Rect();
		float offsets[] = new float[lines.size()];
		int i = 0;
		float offset = 0.0f;
		for (String line : lines) {
			//calculate the height of this line
			textPaint.getTextBounds(line, 0, line.length(), lineBounds);
			float height = lineBounds.bottom - lineBounds.top + textPaint.descent();
			
			//update our bounding box
			bounds.right = Math.max(bounds.right, lineBounds.right - lineBounds.left);
			if (i == 0) {
				bounds.top = (int)lineBounds.bottom - lineBounds.top;
			}
			
			//store our previous offset, and remember the running total
			offsets[i] = offset;
			offset = offsets[i] + height;
			
			i++;
		}
		
		//offset each offset by half the total height of the text
		float totalHeight = offset;
		offset = offset / 2;
		for (i = 0; i < offsets.length; i++) {
			offsets[i] -= offset;
		}
		
		//center our bounds
		bounds.left = -bounds.right / 2;
		bounds.right += bounds.left;
		bounds.top = -(int)(totalHeight / 2) - bounds.top;
		bounds.bottom = (int)(totalHeight + bounds.top);
		
		//add some padding
		bounds.left -= 5;
		bounds.right += 5;
		bounds.top -= 5;
		bounds.bottom += 5;
		
		return offsets;
	}

}
