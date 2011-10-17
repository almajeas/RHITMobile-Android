package edu.rosehulman.android.directory.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import edu.rosehulman.android.directory.model.MapAreaData;

public class MapAreaDataAdapter extends TableAdapter {

	public static final String TABLE_NAME = "MapAreaData";
	
	public static final String KEY_ID = "_Id";
	public static final String KEY_LABEL_ON_HYBRID = "LabelOnHybrid";
	public static final String KEY_MIN_ZOOM_LEVEL = "MinZoomLevel";
	
	private MapAreaCornersAdapter cornersAdapter;
	
	public MapAreaDataAdapter(SQLiteDatabase db) {
		super(db);
		cornersAdapter = new MapAreaCornersAdapter(db);
	}
	
	@Override
	public void open() {
		super.open();
		cornersAdapter = new MapAreaCornersAdapter(db);		
	}
	
	@Override
	public void close() {
		super.close();
		cornersAdapter = null;
	}
	
	/**
	 * Delete all map areas
	 */
	public void clear() {
		db.delete(TABLE_NAME, null, null);
		
		cornersAdapter.clearCorners();
	}
	
	/**
	 * Adds a new map area to the database
	 * 
	 * @param mapData The map area to add
	 * @return the id of the new map area
	 */
	public long add(MapAreaData mapData) {
		ContentValues values = new ContentValues();
		values.put(KEY_LABEL_ON_HYBRID, mapData.labelOnHybrid);
		values.put(KEY_MIN_ZOOM_LEVEL, mapData.minZoomLevel);
		long id = db.insert(TABLE_NAME, null, values);
		
		cornersAdapter.addMapAreaCorners(id, mapData.corners);
		
		return id;
	}
	
	/**
	 * Load the given map area
	 * 
	 * @param id The id of the map area to load 
	 * @param includeCorners True if corners should also be loaded
	 * @return the new MapAreaData
	 */
	public MapAreaData loadMapArea(long id, boolean includeCorners) {
		MapAreaData mapArea = new MapAreaData();
		
		String[] projection = new String[] {KEY_LABEL_ON_HYBRID, KEY_MIN_ZOOM_LEVEL};
		String[] args = new String[] {String.valueOf(id)};
		Cursor cursor = db.query(TABLE_NAME, projection, KEY_ID + "=?", args, null, null, null);

		cursor.moveToFirst();
		mapArea.labelOnHybrid = getBoolean(cursor, cursor.getColumnIndex(KEY_LABEL_ON_HYBRID));
		mapArea.minZoomLevel = cursor.getInt(cursor.getColumnIndex(KEY_MIN_ZOOM_LEVEL));
		
		if (includeCorners) {
			mapArea.corners = cornersAdapter.getCorners(id);
		}
		
		return mapArea;
	}

	/**
	 * Iterate over a map area's corners
	 * 
	 * @param mapAreaId the id of the map area
	 * @return A new Cursor
	 */
	public Cursor getCornersCursor(int mapAreaId) {
		return cornersAdapter.getBuildingCornersCursor(mapAreaId);
	}
	
}
