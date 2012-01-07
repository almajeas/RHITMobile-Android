package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Path implements Parcelable {
	
	public String dir;
	
	public LatLon dest;
	
	public boolean flag;

	public static Path deserialize(JSONObject root) throws JSONException {
		Path res = new Path();
		if (!root.isNull("Dir"))
			res.dir = root.getString("Dir");
		res.dest = LatLon.deserialize(root.getJSONObject("To"));
		res.flag = root.getBoolean("Flag");
		
		return res;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(dir);
		dest.writeParcelable(this.dest, flags);
		dest.writeInt(flag ? 1 : 0);
	}
	
	public static final Parcelable.Creator<Path> CREATOR = new Parcelable.Creator<Path>() {

		@Override
		public Path createFromParcel(Parcel in) {
			Path res = new Path();
			
			res.dir = in.readString();
			res.dest = in.readParcelable(LatLon.class.getClassLoader());
			res.flag = in.readInt() > 0 ? true : false;
			
			return res;
		}

		@Override
		public Path[] newArray(int size) {
			return new Path[size];
		}
		
	};

	
}
