package edu.rosehulman.android.directory.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import edu.rosehulman.android.directory.model.Building;

public class BuildingAdapter {
	
	public static final String TABLE_NAME = "buildings";
	
	public static final String KEY_ID = "_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_SHOW_LABEL = "showLabel";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_CENTER_LAT = "centerLat";
	public static final String KEY_CENTER_LON = "centerLon";
	
	private SQLiteOpenHelper dbOpenHelper;
	private SQLiteDatabase db;
	
	public BuildingAdapter(Context context) {
		dbOpenHelper = DatabaseHelper.getInstance(context);
	}
	
	public void open() {
		db = dbOpenHelper.getWritableDatabase();
	}
	
	public void close() {
		db.close();
	}
	
	public Cursor getBuildingOverlayCursor() {
		String[] projection = new String[] {KEY_ID, KEY_NAME, KEY_CENTER_LAT, KEY_CENTER_LON};
		return db.query(TABLE_NAME, projection, "showLabel='1'", null, null, null, null);
	}
	
	public void replaceBuildings(Building[] newData) {
		db.beginTransaction();
		
		//delete all records
		db.delete(TABLE_NAME, null, null);
		
		//add each building to the database
		for (Building building : newData) {
			ContentValues values = new ContentValues();
			values.put(KEY_NAME, building.name);
			if (building.description != null)
				values.put(KEY_DESCRIPTION, building.description);
			values.put(KEY_SHOW_LABEL, building.showLabel);
			values.put(KEY_CENTER_LAT, building.centerLat);
			values.put(KEY_CENTER_LON, building.centerLon);
			db.insert(TABLE_NAME, null, values);
		}
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
}
