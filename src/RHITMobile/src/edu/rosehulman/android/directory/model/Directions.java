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
	
	public int stairsUp;
	
	public int stairsDown;
	
	public DirectionPath[] paths;

	private static DirectionPath[] deserializePaths(JSONArray root) throws JSONException {
		DirectionPath[] res = new DirectionPath[root.length()];
		
		for (int i = 0; i < res.length; i++) {
			res[i] = DirectionPath.deserialize(root.getJSONObject(i));
		}
		
		return res;
	}
	
	public static Directions deserialize(JSONObject root) throws JSONException {
		Directions res = new Directions();
		
		res.distance = root.getDouble("Dist");
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
		if (paths.length == 0)
			return null;
		
		LatLon pos = paths[0].coord;
		int left = pos.lon;
		int right = pos.lon;
		int bottom = pos.lat;
		int top = pos.lat;
		
		for (DirectionPath path : paths) {
			left = Math.min(left, path.coord.lon);
			right = Math.max(right, path.coord.lon);
			bottom = Math.min(bottom, path.coord.lat);
			top = Math.max(top, path.coord.lat);
			
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
		dest.writeInt(stairsUp);
		dest.writeInt(stairsDown);
		dest.writeParcelableArray(paths, flags);
	}
	
	public static final Parcelable.Creator<Directions> CREATOR = new Parcelable.Creator<Directions>() {

		@Override
		public Directions createFromParcel(Parcel in) {
			Directions res = new Directions();
			
			res.distance = in.readDouble();
			res.stairsUp = in.readInt();
			res.stairsDown = in.readInt();
			
			Parcelable[] paths = in.readParcelableArray(DirectionPath.class.getClassLoader());
			res.paths = new DirectionPath[paths.length];
			ArrayUtil.cast(paths, res.paths);
			
			return res;
		}

		@Override
		public Directions[] newArray(int size) {
			return new Directions[size];
		}
		
	};
	
}
