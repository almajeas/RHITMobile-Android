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

public class TextOverlay extends Overlay {
	
	private static Paint outlinePaint;
	private static Paint textPaint;

	private GeoPoint location;
	
	private List<String> lines;
	private float offsets[];
	
	public TextOverlay(GeoPoint loc, String text) {
		this.location = loc;
		
		lines = splitText(text);
		offsets = calcOffsets(lines);
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
	
	private static float[] calcOffsets(List<String> lines) {
		Rect bounds = new Rect();
		float offsets[] = new float[lines.size()];
		int i = 0;
		float offset = 0.0f;
		for (String line : lines) {
			//calculate the height of this line
			textPaint.getTextBounds(line, 0, line.length(), bounds);
			float height = bounds.bottom - bounds.top + textPaint.descent();
			//store our previous offset, and remember the running total
			offsets[i] = offset;
			offset = offsets[i] + height;
			
			i++;
		}
		
		//offset each offset by half the total height of the text
		offset = offset / 2;
		for (int j = 0; j < offsets.length; j++) {
			offsets[j] -= offset;
		}
		
		return offsets;
	}

}
