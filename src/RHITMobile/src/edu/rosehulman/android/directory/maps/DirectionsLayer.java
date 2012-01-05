package edu.rosehulman.android.directory.maps;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

import edu.rosehulman.android.directory.TaskManager;
import edu.rosehulman.android.directory.model.Directions;
import edu.rosehulman.android.directory.model.Path;
import edu.rosehulman.android.directory.util.BoundingBox;

/**
 * Overlay containing Point Of Interest markers
 */
public class DirectionsLayer extends BalloonItemizedOverlay<OverlayItem> implements ManageableOverlay {

	private static Drawable transparent;
	
	private OverlayManagerControl manager;
	private TaskManager taskManager;
	
	private Directions directions;
	public BoundingBox bounds;
	
	private boolean animate = true;

	public DirectionsLayer(Drawable defaultMarker, MapView mapView, TaskManager taskManager, Directions directions) {
		super(boundCenter(defaultMarker), mapView);
		this.taskManager = taskManager;
		this.directions = directions;

		if (transparent == null) {
			transparent = getMapView().getResources().getDrawable(android.R.color.transparent);
		}
		
		bounds = directions.getBounds();

		populate();
	}
	
	/**
	 * Add a new location to the overlay
	 * 
	 * @param location The Location to render
	 */
	/*public void add(Location location) {
		OverlayItem overlay = new OverlayItem(location.center.asGeoPoint(), location.name, location.description);
		PointOfInterest poi = new PointOfInterest(location, overlay);
		poiMap.put(location.id, poi);
		pois.add(poi);
		populate();
	}*/
	
	/**
	 * Focus a particular POI
	 * 
	 * @param id The ID of the POI to focus
	 * @param animate Whether the focus operation should animate or not
	 * 
	 * @return True if the POI was found immediately; false otherwise
	 */
	/*public boolean focus(long id, boolean animate) {
		PointOfInterest poi = poiMap.get(id);
		if (poi == null) {
			ShowLocation task = new ShowLocation(animate);
			taskManager.addTask(task);
			task.execute(id);
			
			//hide what is visible for now
			this.setFocus(null);
			return false;
		}
		this.animate = animate;
		this.setLastFocusedIndex(pois.indexOf(poi));
		this.onTap(pois.indexOf(poi));
		this.animate = true;
		
		return true;
	}*/
	
	/**
	 * Determines the id of the focused poi
	 * 
	 * @return the id of the focused poi, or -1 if none
	 */
	/*public long getFocusId() {
		int index = getLastFocusedIndex();
		if (index == -1)
			return -1;
		
		return pois.get(index).location.id;
	}*/

	@Override
	protected OverlayItem createItem(int i) {
		OverlayItem overlay;
		
		if (i == 0) {
			overlay = new OverlayItem(directions.start.asGeoPoint(), "Starting location", "");
		} else {
			Path path = directions.paths[i-1];
			overlay = new OverlayItem(path.dest.asGeoPoint(), path.dir, "");
			if (path.flag){ 
				//TODO have a custom marker for flagged nodes
				//overlay.setMarker(null);
			}
		}
		
		return overlay;
	}

	@Override
	public int size() {
		return directions.paths.length + 1;
	}
	
	@Override
	protected boolean onBalloonTap(int index, OverlayItem item) {
		//TODO show directions list
		
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
	
	@Override
	public void clearSelection() {
		this.setFocus(null);
	}

	@Override
	public void setManager(OverlayManagerControl manager) {
		this.manager = manager;
	}

}
