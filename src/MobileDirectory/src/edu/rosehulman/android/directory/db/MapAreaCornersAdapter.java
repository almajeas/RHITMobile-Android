package edu.rosehulman.android.directory.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import edu.rosehulman.android.directory.model.LatLon;

public class MapAreaCornersAdapter extends TableAdapter {
	
	public static final String TABLE_NAME = "MapAreaCorners";
	
	public static final String KEY_ID = "_Id";
	public static final String KEY_MAP_AREA = "MapArea";
	public static final String KEY_ITEM = "Item";
	public static final String KEY_LAT = "Lat";
	public static final String KEY_LON = "Lon";
	
	public MapAreaCornersAdapter(SQLiteDatabase db) {
		super(db);
	}
	
	public void clearCorners() {
		db.delete(TABLE_NAME, null, null);
	}
	
	public void addMapAreaCorners(int id, LatLon corners[]) {
		for (int i = 0; i < corners.length; i++) {
			ContentValues values = new ContentValues();
			values.put(KEY_MAP_AREA, id);
			values.put(KEY_ITEM, i);
			values.put(KEY_LAT, corners[i].lat);
			values.put(KEY_LON, corners[i].lon);
			
			db.insert(TABLE_NAME, null, values);
		}
	}

	public Cursor getBuildingCornersCursor(int buildingId) {
		String[] projection = new String[] {KEY_LAT, KEY_LON};
		String[] args = new String[] { String.valueOf(buildingId) };
		return db.query(TABLE_NAME, projection, KEY_MAP_AREA + "=?", args, null, null, KEY_ITEM);
	}
	
	public LatLon[] getCorners(int buildingId) {
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
