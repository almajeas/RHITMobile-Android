package edu.rosehulman.android.directory.model;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Location implements Parcelable {

	/** Unique identifier for this Location */
	public long id;
	
	/** Parent identifier to this Location */
	public long parentId;

	/** The name of the area this Location represents */
	public String name;

	/** A short description of the Location */
	public String description;

	/** The center point of this Location */
	public LatLon center;
	
	/** Alternate names for this location, or null if not loaded */
	public String[] altNames;
	
	/** Relevant links associated with this location */
	public Hyperlink[] links;
	
	/** Can this location be departed from */
	public boolean isDepartable;
	
	/** The type of this location */
	public LocationType type;
	
	/** The id of the map area (or -1 if unavailable) */
	public long mapAreaId;
	
	/** The map area data associated with this location */
	public MapAreaData mapData;

	/** Create a new Location with all fields set to their default values */
	public Location() {
	}

	/**
	 * Create a new Location with some common parameters
	 * 
	 * @param name The name of the Location
	 * @param latE6 The latitude of the center of the area, in microdegrees
	 * @param lonE6 The longitude of the center of the area, in microdegrees
	 */
	public Location(String name, int latE6, int lonE6) {
		this.name = name;
		this.center = new LatLon(latE6, lonE6);
	}
	
	/**
	 * Deserialize the given JSONObject into an instance of Location
	 * 
	 * @param root The JSONObject with the required fields to create a new, 
	 * fully initialized MapArea
	 * @return A new Location
	 * @throws JSONException
	 */
	public static Location deserialize(JSONObject root) throws JSONException {
		Location res = new Location();
		
		res.id = root.getInt("Id");
		if (root.isNull("Parent"))
			res.parentId = -1;
		else
			res.parentId = root.getInt("Parent");
		res.name = root.getString("Name");
		res.description = root.getString("Description");
		
		res.center = LatLon.deserialize(root.getJSONObject("Center"));
		
		JSONArray altNames = root.getJSONArray("AltNames");
		res.altNames = new String[altNames.length()];
		for (int i = 0; i < res.altNames.length; i++) {
			res.altNames[i] = altNames.getString(i);
		}
		
		JSONArray links = root.getJSONArray("Links");
		res.links = new Hyperlink[links.length()];
		for (int i = 0; i < res.links.length; i++) {
			res.links[i] = Hyperlink.deserialize(links.getJSONObject(i));
		}
		
		res.isDepartable = root.getBoolean("IsDepartable");
		res.type = typeMap.get(root.getString("Type"));
		
		if (!root.isNull("MapArea")) {
			res.mapData = MapAreaData.deserialize(root.getJSONObject("MapArea"));
		}
		
		return res;
	}
	
	private static HashMap<String, LocationType> typeMap;

	static {
		typeMap = new HashMap<String, LocationType>();
		typeMap.put("NL", LocationType.NORMAL);
		typeMap.put("PI", LocationType.POINT_OF_INTEREST);
		typeMap.put("QL", LocationType.ON_QUICK_LIST);
		typeMap.put("MB", LocationType.MENS_BATHROOM);
		typeMap.put("WB", LocationType.WOMENS_BATHROOM);
		typeMap.put("UB", LocationType.UNISEX_BATHROOM);
		typeMap.put("PR", LocationType.PRINTER);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeLong(parentId);
		dest.writeLong(mapAreaId);
		dest.writeString(name);
		dest.writeString(description);
		dest.writeInt(center.lat);
		dest.writeInt(center.lon);
		dest.writeInt(isDepartable ? 1 : 0);
		dest.writeInt(type.ordinal());
		
		dest.writeStringArray(altNames);
		dest.writeTypedArray(links, 0);
		
	}
	
	public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {

		@Override
		public Location createFromParcel(Parcel in) {
			Location res = new Location();
			
			res.id = in.readLong();
			res.parentId = in.readLong();
			res.mapAreaId = in.readLong();
			res.name = in.readString();
			res.description = in.readString();
			res.center = new LatLon(in.readInt(), in.readInt());
			res.isDepartable = in.readInt() != 0;
			res.type = LocationType.fromOrdinal(in.readInt());
			
			res.altNames = in.createStringArray();
			res.links = in.createTypedArray(Hyperlink.CREATOR);
			
			return res;
		}

		@Override
		public Location[] newArray(int size) {
			return new Location[size];
		}
		
	};

}
