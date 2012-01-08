package edu.rosehulman.android.directory.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import edu.rosehulman.android.directory.util.ArrayUtil;
import edu.rosehulman.android.directory.util.BoundingBox;

public class Directions implements Parcelable {
	
	public double distance;
	
	public LatLon start;
	
	public int stairsUp;
	
	public int stairsDown;
	
	public Path[] paths;

	private static Path[] deserializePaths(JSONArray root) throws JSONException {
		Path[] res = new Path[root.length()];
		
		for (int i = 0; i < res.length; i++) {
			res[i] = Path.deserialize(root.getJSONObject(i));
		}
		
		return res;
	}
	
	public static Directions deserialize(JSONObject root) throws JSONException {
		Directions res = new Directions();
		
		res.distance = root.getDouble("Dist");
		res.start = LatLon.deserialize(root.getJSONObject("Start"));
		res.stairsUp = root.getInt("StairsUp");
		res.stairsDown = root.getInt("StairsDown");
		res.paths = deserializePaths(root.getJSONArray("Paths"));

		return res;
	}
	
	/**
	 * Computes the bounding box for the set of directions
	 * 
	 * @return The bounds of the coordinates contained in the directions
	 */
	public BoundingBox getBounds() {
		int left = start.lon;
		int right = start.lon;
		int top = start.lat;
		int bottom = start.lat;
		
		for (Path path : paths) {
			left = Math.min(left, path.dest.lon);
			right = Math.max(right, path.dest.lon);
			bottom = Math.min(bottom, path.dest.lat);
			top = Math.max(top, path.dest.lat);
			
		}
		return new BoundingBox(left, right, top, bottom);
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(distance);
		dest.writeParcelable(start, flags);
		dest.writeInt(stairsUp);
		dest.writeInt(stairsDown);
		dest.writeParcelableArray(paths, flags);
	}
	
	public static final Parcelable.Creator<Directions> CREATOR = new Parcelable.Creator<Directions>() {

		@Override
		public Directions createFromParcel(Parcel in) {
			Directions res = new Directions();
			
			res.distance = in.readDouble();
			res.start = in.readParcelable(LatLon.class.getClassLoader());
			res.stairsUp = in.readInt();
			res.stairsDown = in.readInt();
			
			Parcelable[] paths = in.readParcelableArray(Path.class.getClassLoader());
			res.paths = new Path[paths.length];
			ArrayUtil.cast(paths, res.paths);
			
			return res;
		}

		@Override
		public Directions[] newArray(int size) {
			return new Directions[size];
		}
		
	};
	
}
