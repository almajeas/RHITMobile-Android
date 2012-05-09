package edu.rosehulman.android.directory.maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

import edu.rosehulman.android.directory.DirectionListActivity;
import edu.rosehulman.android.directory.LocationActivity;
import edu.rosehulman.android.directory.R;
import edu.rosehulman.android.directory.TaskManager;
import edu.rosehulman.android.directory.model.DirectionPath;
import edu.rosehulman.android.directory.model.Directions;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.util.BoundingBox;

/**
 * Overlay containing Point Of Interest markers
 */
public class DirectionsLayer extends BalloonItemizedOverlay<OverlayItem> implements ManageableOverlay {
	
	public static final int REQUEST_DIRECTIONS_LIST = 1;
	
	public interface UIListener {
		public void setPrevButtonEnabled(boolean enabled);
		public void setNextButtonEnabled(boolean enabled);
		void startActivityForResult(Intent intent, int requestCode);
	}

	private static Drawable transparent;
	
	private OverlayManagerControl manager;
	private UIListener uiListener;
	
	public Directions directions;
	public Location[] locations;
	public BoundingBox bounds;
	
	private int nodeCount;
	private List<DirectionPath> pathNodes = new ArrayList<DirectionPath>();
	private Map<Long, Location> locationMap = new HashMap<Long, Location>();
	
	private boolean animate = true;

	public DirectionsLayer(MapView mapView, TaskManager taskManager, Directions directions, Location[] locations, UIListener uiListener) {
		super(boundCenter(getDirectionsDrawable(mapView.getResources(), DirectionsBitmap.NODE)), mapView);
		this.directions = directions;
		this.locations = locations;
		this.uiListener = uiListener;
		
		for (Location location : locations) {
			if (!locationMap.containsKey(location.id)){ 
				locationMap.put(location.id, location);
			}
		}

		if (transparent == null) {
			transparent = getMapView().getResources().getDrawable(android.R.color.transparent);
		}
		
		bounds = directions.getBounds();

		for (DirectionPath path : directions.paths) {
			if (path.hasDirection())
				pathNodes.add(path);
		}
		DirectionPath end = directions.paths[directions.paths.length-1];
		if (!end.hasDirection()) {
			pathNodes.add(end);
		}
		nodeCount = pathNodes.size();
		
		pathPaint = new Paint();
		pathPaint.setStyle(Style.STROKE);
		pathPaint.setColor(mapView.getResources().getColor(R.color.light_blue));
		pathPaint.setAlpha(200);
		pathPaint.setStrokeCap(Cap.ROUND);
		pathPaint.setStrokeJoin(Join.BEVEL);
		pathPaint.setStrokeWidth(10);
		
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		OverlayItem overlay;
		
		DirectionPath path = pathNodes.get(i);
		Location loc = null;
		if (path.location >= 0) {
			loc = locationMap.get(path.location);
		}
		
		if (path.dir != null && loc == null) {
			overlay = new OverlayItem(path.coord.asGeoPoint(), path.dir, "");
		} else if (path.dir != null) {
			overlay = new OverlayItem(path.coord.asGeoPoint(), path.dir, loc.name);
		} else if (loc != null) {
			overlay = new OverlayItem(path.coord.asGeoPoint(), loc.name, loc.description);
		} else {
			overlay = new OverlayItem(path.coord.asGeoPoint(), "Starting position", "");
		}

		if (path.flag || i == 0) {
			DirectionsBitmap marker = (i == 0) ? DirectionsBitmap.START : DirectionsBitmap.END;
			overlay.setMarker(boundCenterBottom(getDirectionsDrawable(getMapView().getResources(), marker)));
		}
		
		return overlay;
	}

	@Override
	public int size() {
		return nodeCount;
	}
	
	@Override
	protected boolean onBalloonTap(int index, OverlayItem item) {
		Context context = getMapView().getContext();
		DirectionPath path = pathNodes.get(index);
		
		if (path.location >= 0) {
			Intent intent = LocationActivity.createIntent(context, locationMap.get(path.location));
			context.startActivity(intent);
		} else {
			showDirectionsList(index);	
		}
		
		return true;
	}
	
	@Override
	protected void animateTo(int index, GeoPoint center) {
		if (manager != null) {
			manager.markSelected();
		}
		MapView mapView = getMapView();
		ViewController controller = new ViewController(mapView);
		Point pt = new Point(mapView.getWidth() / 2, mapView.getHeight() / 2);
		int spanLat = bounds.top - bounds.bottom;
		int spanLon = bounds.left - bounds.right;
		controller.animateTo(center, pt, spanLat, spanLon, animate);
	}
	
	private Paint pathPaint;
	private Point pt;
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow) {
			super.draw(canvas, mapView, shadow);
			return;
		}
		
		Projection proj = mapView.getProjection();
		android.graphics.Path directionsPath = new android.graphics.Path();
		
		pt = proj.toPixels(directions.paths[0].coord.asGeoPoint(), pt);
		directionsPath.moveTo(pt.x, pt.y);
		
		for (int i = 1; i < directions.paths.length; i++) {
			DirectionPath path = directions.paths[i];
			proj.toPixels(path.coord.asGeoPoint(), pt);
			directionsPath.lineTo(pt.x, pt.y);
		}
		
		canvas.drawPath(directionsPath, pathPaint);
		
		super.draw(canvas, mapView, shadow);
	}
	
	@Override
	public void clearSelection() {
		this.setFocus(null);
	}

	@Override
	public void setManager(OverlayManagerControl manager) {
		this.manager = manager;
	}
	
	public void stepNext() {
		int maxStep = pathNodes.size()-1;
		int currentStep = getLastFocusedIndex();
		currentStep = Math.min(currentStep+1, maxStep);
		if (currentStep == maxStep) {
			uiListener.setNextButtonEnabled(false);
		}
		uiListener.setPrevButtonEnabled(true);
		focus(currentStep, true);
	}
	
	public void stepPrevious() {
		int minStep = 0;
		int currentStep = getLastFocusedIndex();
		currentStep = Math.max(currentStep-1, minStep);
		if (currentStep == minStep) {
			uiListener.setPrevButtonEnabled(false);
		}
		uiListener.setNextButtonEnabled(true);
		focus(currentStep, true);
	}
	
	/**
	 * Focus a particular step
	 * 
	 * @param step The index of the step to focus
	 * @param animate Whether the focus operation should animate or not
	 * 
	 * @return True if the step was focused
	 */
	public boolean focus(int step, boolean animate) {
		if (step == -1) {
			this.setFocus(null);
			return true;
		}
		
		this.animate = animate;
		this.setLastFocusedIndex(step);
		this.onTap(step);
		this.animate = true;
		
		return true;
	}
	
	/**
	 * Shows the list of directions.
	 * 
	 * @param step The step to move to, or -1
	 */
	public void showDirectionsList(int step) {
		Context context = getMapView().getContext();
		Intent intent = DirectionListActivity.createIntent(context, directions, locations);
		uiListener.startActivityForResult(intent, REQUEST_DIRECTIONS_LIST);
	}
	
	private enum DirectionsBitmap {
		START,
		END,
		NODE
	}
	
	private static Drawable getDirectionsDrawable(Resources resources, DirectionsBitmap type) {
		switch (type) {
		case START:
			return resources.getDrawable(R.drawable.directions_depart);
		case END:
			return resources.getDrawable(R.drawable.directions_arrive);
		case NODE:
			return resources.getDrawable(R.drawable.directions_waypoint);
		}
		
		return resources.getDrawable(R.drawable.directions_waypoint);
	}
	
}
