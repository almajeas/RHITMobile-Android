package edu.rosehulman.android.directory.db;

import android.content.ContentValues;
import android.database.Cursor;
import edu.rosehulman.android.directory.model.LatLon;
import edu.rosehulman.android.directory.model.MapArea;

public class MapAreaAdapter extends TableAdapter {
	
	public static final String TABLE_NAME = "MapAreas";
	
	public static final String KEY_ID = "_Id";
	public static final String KEY_NAME = "Name";
	public static final String KEY_DESCRIPTION = "Description";
	public static final String KEY_LABEL_ON_HYBRID = "LabelOnHybrid";
	public static final String KEY_MIN_ZOOM_LEVEL = "MinZoomLevel";
	public static final String KEY_CENTER_LAT = "CenterLat";
	public static final String KEY_CENTER_LON = "CenterLon";
	
	private MapAreaCornersAdapter cornersAdapter;
	
	public MapAreaAdapter() {
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
	
	public Cursor getBuildingOverlayCursor(boolean textVisible) {
		String[] projection = new String[] {KEY_ID, KEY_NAME, KEY_CENTER_LAT, KEY_CENTER_LON, KEY_MIN_ZOOM_LEVEL};
		String[] args = new String[] {textVisible ? "1" : "0"};
		return db.query(TABLE_NAME, projection, KEY_LABEL_ON_HYBRID + "=?", args, null, null, null);
	}
	
	public Cursor getBuildingCornersCursor(int buildingId) {
		return cornersAdapter.getBuildingCornersCursor(buildingId);
	}
	
	public DbIterator<MapArea> getBuildingIterator() {
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
		return new BuildingIterator(cursor);
	}
	
	public void loadCorners(MapArea area) {
		area.corners = cornersAdapter.getCorners(area.id);
	}
	
	public void replaceBuildings(MapArea[] newData) {
		db.beginTransaction();
		
		//delete all records
		db.delete(TABLE_NAME, null, null);
		cornersAdapter.clearCorners();
		
		//add each building to the database
		for (MapArea building : newData) {
			ContentValues values = new ContentValues();
			values.put(KEY_ID, building.id);
			values.put(KEY_NAME, building.name);
			if (building.description != null)
				values.put(KEY_DESCRIPTION, building.description);
			values.put(KEY_LABEL_ON_HYBRID, building.labelOnHybrid);
			values.put(KEY_MIN_ZOOM_LEVEL, building.minZoomLevel);
			values.put(KEY_CENTER_LAT, building.center.lat);
			values.put(KEY_CENTER_LON, building.center.lon);
			
			db.insert(TABLE_NAME, null, values);
			cornersAdapter.addMapAreaCorners(building.id, building.corners);
		}
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	private class BuildingIterator extends DbIterator<MapArea> {

		public BuildingIterator(Cursor cursor) {
			super(cursor);
		}

		@Override
		protected MapArea convertRow(Cursor cursor) {
			MapArea area = new MapArea();
			area.id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
			area.name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
			area.description = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
			area.labelOnHybrid = getBoolean(cursor, cursor.getColumnIndex(KEY_LABEL_ON_HYBRID));
			area.minZoomLevel = cursor.getInt(cursor.getColumnIndex(KEY_MIN_ZOOM_LEVEL));
			int lat = cursor.getInt(cursor.getColumnIndex(KEY_CENTER_LAT));
			int lon = cursor.getInt(cursor.getColumnIndex(KEY_CENTER_LON));
			area.center = new LatLon(lat, lon);
			
			return area;
		}
		
	}
}
