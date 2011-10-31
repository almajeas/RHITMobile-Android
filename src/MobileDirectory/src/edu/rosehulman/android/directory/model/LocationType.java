package edu.rosehulman.android.directory.model;

import java.util.HashMap;

/**
 * Used to denote the type of a location
 */
public enum LocationType {
	/** Not a special location type */
	NORMAL,
	
	/** Point of interest */
	POINT_OF_INTEREST,
	/** POI that should be quickly accessible */
	ON_QUICK_LIST,
	
	/** A bathroom, for men.  No women allowed. */
	MENS_BATHROOM,
	/** A bathroom, for women.  No men allowed. */
	WOMENS_BATHROOM,
	/** A bathroom, for men and women (not simultaneously).*/
	UNISEX_BATHROOM,
	
	/** Location is a printer */
	PRINTER;
	
	private static HashMap<Integer, LocationType> types;
	
	static {
		types = new HashMap<Integer, LocationType>();
		for (LocationType type : LocationType.values()) {
			types.put(type.ordinal(), type);
		}
	}
	
	public static LocationType fromOrdinal(int ordinal) {
		return types.get(ordinal);
	}
}
