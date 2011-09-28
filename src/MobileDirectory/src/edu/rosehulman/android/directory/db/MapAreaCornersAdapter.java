package edu.rosehulman.android.directory.db;

import android.content.ContentValues;
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
	
	

}
