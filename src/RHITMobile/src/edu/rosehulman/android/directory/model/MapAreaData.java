package edu.rosehulman.android.directory.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rosehulman.android.directory.util.BoundingBox;
import edu.rosehulman.android.directory.util.BoundingPath;
import edu.rosehulman.android.directory.util.Point;

public class MapAreaData {

	/** True if this mapArea is labeled on Google's Hybrid map layer */
	public boolean labelOnHybrid;

	/** The minimum zoom level that this mapview should be displayed at */
	public int minZoomLevel;

	/** A set of coordinates providing an enclosing polygon around this area */
	public LatLon corners[];
	

	/**
	 * Determine if the MapArea has corners available
	 * 
	 * @return True if the MapArea has corners available
	 */
	public boolean hasCorners() {
		return corners != null;
	}
	
	/**
	 * Computes the bounding box around this map area
	 * 
	 * @return The BoundingBox that encapsulates all corner points
	 */
	public BoundingBox getBounds() {
		List<Point> points = new ArrayList<Point>(corners.length);
		for (LatLon corner : corners) {
			points.add(new Point(corner.lat, corner.lon));
		}
		BoundingPath path = new BoundingPath(points);
		return path.getBoundingBox();
	}
	

	private static LatLon[] deserializeCorners(JSONArray array) throws JSONException {
		LatLon[] res = new LatLon[array.length()];
		for (int i = 0; i < res.length; i++) {
			res[i] = LatLon.deserialize(array.getJSONObject(i));
		}
		return res;
	}
	
	/**
	 * Deserialize the given JSONObject into an instance of MapAreaData
	 * 
	 * @param root The JSONObject with the required fields to create a new, 
	 * fully initialized MapArea
	 * @return A new MapAreaData
	 * @throws JSONException
	 */
	public static MapAreaData deserialize(JSONObject root) throws JSONException {
		MapAreaData res = new MapAreaData();
		
		res.labelOnHybrid = root.getBoolean("LabelOnHybrid");
		res.minZoomLevel = root.getInt("MinZoomLevel");
		res.corners = deserializeCorners(root.getJSONArray("Corners"));
		
		return res;
	}
}
