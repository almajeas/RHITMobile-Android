package edu.rosehulman.android.directory.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import edu.rosehulman.android.directory.model.LatLon;

/**
 * Provides operations on the database using location model objects
 */
public class MapAreaCornersAdapter extends TableAdapter {
	
	public static final String TABLE_NAME = "MapAreaCorners";
	
	public static final String KEY_ID = "_Id";
	public static final String KEY_MAP_AREA = "MapAreaId";
	public static final String KEY_ITEM = "Item";
	public static final String KEY_LAT = "Lat";
	public static final String KEY_LON = "Lon";
	
	/**
	 * Creates a new MapAreaCornersAdapter
	 * 
	 * @param db The database connection to use
	 */
	public MapAreaCornersAdapter(SQLiteDatabase db) {
		super(db);
	}
	
	/**
	 * Clear all corners
	 */
	public void clearCorners() {
		db.delete(TABLE_NAME, null, null);
	}
	
	/**
	 * Add a new set of corners to the database
	 * 
	 * @param id The map area id to associate with
	 * @param corners The corners to add
	 */
	public void addMapAreaCorners(long id, LatLon corners[]) {
		for (int i = 0; i < corners.length; i++) {
			ContentValues values = new ContentValues();
			values.put(KEY_MAP_AREA, id);
			values.put(KEY_ITEM, i);
			values.put(KEY_LAT, corners[i].lat);
			values.put(KEY_LON, corners[i].lon);
			
			db.insert(TABLE_NAME, null, values);
		}
	}

	/**
	 * Iterate over a building's corner points
	 * 
	 * @param buildingId The id of the map area 
	 * @return A new Cursor
	 */
	public Cursor getBuildingCornersCursor(long buildingId) {
		String[] projection = new String[] {KEY_LAT, KEY_LON};
		String[] args = new String[] { String.valueOf(buildingId) };
		return db.query(TABLE_NAME, projection, KEY_MAP_AREA + "=?", args, null, null, KEY_ITEM);
	}
	
	/**
	 * Retrieve the corners associated with a map area
	 * 
	 * @param buildingId the id of the map area
	 * @return an ordered array of LatLon pairs
	 */
	public LatLon[] getCorners(long buildingId) {
		Cursor cursor = getBuildingCornersCursor(buildingId);
		LatLon[] corners = new LatLon[cursor.getCount()];
		int iLat = cursor.getColumnIndex(KEY_LAT);
		int iLon = cursor.getColumnIndex(KEY_LON);
		while (cursor.moveToNext()) {
			int i = cursor.getPosition();
			int lat = cursor.getInt(iLat);
			int lon = cursor.getInt(iLon);
			corners[i] = new LatLon(lat, lon);
		}
		return corners;
	}	

}
