package edu.rosehulman.android.directory.model;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class DirectionPath implements Parcelable {
	
	public DirectionActionType action;
	
	public String dir;
	
	public LatLon coord;

	public double altitude;
	
	public boolean flag;
	
	public boolean outside;
	
	public long location;
	
	/**
	 * Determines if this node represents
	 * a direction that the user should follow
	 * 
	 * @return True if the user should care about this node
	 */
	public boolean hasDirection() {
		return dir != null;
	}

	public static DirectionPath deserialize(JSONObject root) throws JSONException {
		DirectionPath res = new DirectionPath();
		if (!root.isNull("Action"))
			res.action = actionMap.get(root.getString("Action"));
		else
			res.action = DirectionActionType.NONE;
		if (!root.isNull("Dir"))
			res.dir = root.getString("Dir");
		res.coord = LatLon.deserialize(root);
		res.altitude = root.getDouble("Altitude");
		res.flag = root.getBoolean("Flag");
		res.outside = root.getBoolean("Outside");
		if (!root.isNull("Location"))
			res.location = root.getLong("Location");
		else
			res.location = -1;
		
		return res;
	}
	
	private static HashMap<String, DirectionActionType> actionMap;

	static {
		actionMap = new HashMap<String, DirectionActionType>();
		actionMap.put("GS", DirectionActionType.GO_STRAIGHT);
		actionMap.put("CS", DirectionActionType.CROSS_STREET);
		actionMap.put("FP", DirectionActionType.FOLLOW_PATH);
		actionMap.put("L1", DirectionActionType.SLIGHT_LEFT);
		actionMap.put("R1", DirectionActionType.SLIGHT_RIGHT);
		actionMap.put("L2", DirectionActionType.TURN_LEFT);
		actionMap.put("R2", DirectionActionType.TURN_RIGHT);
		actionMap.put("L3", DirectionActionType.SHARP_LEFT);
		actionMap.put("R3", DirectionActionType.SHARP_RIGHT);
		actionMap.put("EN", DirectionActionType.ENTER_BUILDING);
		actionMap.put("EX", DirectionActionType.EXIT_BUILDING);
		actionMap.put("US", DirectionActionType.ASCEND_STAIRS);
		actionMap.put("DS", DirectionActionType.DESCEND_STAIRS);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(action.ordinal());
		dest.writeString(dir);
		dest.writeParcelable(this.coord, flags);
		dest.writeDouble(altitude);
		dest.writeInt(flag ? 1 : 0);
		dest.writeInt(outside ? 1 : 0);
		dest.writeLong(location);
	}
	
	public static final Parcelable.Creator<DirectionPath> CREATOR = new Parcelable.Creator<DirectionPath>() {

		@Override
		public DirectionPath createFromParcel(Parcel in) {
			DirectionPath res = new DirectionPath();
			
			res.action = DirectionActionType.fromOrdinal(in.readInt());
			res.dir = in.readString();
			res.coord = in.readParcelable(LatLon.class.getClassLoader());
			res.altitude = in.readDouble();
			res.flag = in.readInt() > 0 ? true : false;
			res.outside = in.readInt() > 0 ? true : false;
			res.location = in.readLong();
			
			return res;
		}

		@Override
		public DirectionPath[] newArray(int size) {
			return new DirectionPath[size];
		}
	};
}
