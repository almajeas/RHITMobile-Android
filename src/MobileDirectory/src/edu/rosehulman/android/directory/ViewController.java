package edu.rosehulman.android.directory;

import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

/**
 * Animate to various locations and zoom levels on a MapView
 */
public class ViewController {
	
	private MapView mapView;
	private MapController controller;

	/**
	 * Creates a new ViewController
	 * 
	 * @param mapView The MapView to interface with
	 */
	public ViewController(MapView mapView) {
		this.mapView = mapView;
		this.controller = mapView.getController();
	}
	
	/**
	 * Animate to a location on the map
	 * 
	 * @param center The GeoPoint that we want to 'center'
	 * @param pt The location on the screen center should be located
	 * @param spanLat How much latitude should be visible on the screen
	 * @param spanLon How much longitude should be visible on the screen
	 */
	public void animateTo(GeoPoint center, Point pt, int spanLat, int spanLon) {
		final int level = mapView.getZoomLevel();
		controller.zoomToSpan(spanLat, spanLon);
		final int newLevel = mapView.getZoomLevel();
		controller.setZoom(level);
		
		animateTo(center, pt, newLevel);
	}
	
	/**
	 * Animate to a location on the map
	 * 
	 * @param center The GeoPoint that we want to 'center'
	 * @param pt The location on the screen center should be located
	 * @param zoomLevel The zoom level to approach
	 */
	public void animateTo(GeoPoint center, Point pt, final int zoomLevel) {
		Projection projection = mapView.getProjection();
		int centerX = mapView.getWidth() / 2;
		int centerY = mapView.getHeight() / 2;

		//@assert projection.toPixels(center, null).equals(pt);
		
		GeoPoint currentCenter = projection.fromPixels(centerX, centerY);
		GeoPoint destCenter = projection.fromPixels(pt.x, pt.y);
		int dLat = destCenter.getLatitudeE6() - currentCenter.getLatitudeE6();
		int dLon = destCenter.getLongitudeE6() - currentCenter.getLongitudeE6();
		
		GeoPoint destPoint = new GeoPoint(center.getLatitudeE6() - dLat, center.getLongitudeE6() - dLon);
		
		final int currentZoomLevel = mapView.getZoomLevel();
		final Point p = pt;
		
		controller.animateTo(destPoint, new Runnable() {
			@Override
			public void run() {
				if (currentZoomLevel < zoomLevel - 1) {
					controller.zoomInFixing(p.x, p.y);
				} else if (currentZoomLevel > zoomLevel) {
					controller.zoomOutFixing(p.x, p.y);
				}
			}
		});
	}
	
	/**
	 * Cancel all animations
	 */
	public void cancel() {
		controller.stopAnimation(false);
	}
	
}
