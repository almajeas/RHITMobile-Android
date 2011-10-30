package edu.rosehulman.android.directory.db;

import android.content.ContentValues;
import android.database.Cursor;
import edu.rosehulman.android.directory.model.LatLon;
import edu.rosehulman.android.directory.model.Location;

/**
 * Performs operations on the database using location model objects
 */
public class LocationAdapter extends TableAdapter {
	
	public static final String TABLE_NAME = "Locations";
	
	public static final String KEY_ID = "_Id";
	public static final String KEY_PARENT_ID = "ParentId";
	public static final String KEY_MAP_AREA_ID = "MapAreaId";
	public static final String KEY_NAME = "Name";
	public static final String KEY_DESCRIPTION = "Description";
	public static final String KEY_CENTER_LAT = "CenterLat";
	public static final String KEY_CENTER_LON = "CenterLon";
	public static final String KEY_IS_POI = "IsPOI";
	public static final String KEY_IS_ON_QUICK_LIST = "IsOnQuickList";
	
	private MapAreaDataAdapter areasAdapter;
	
	/**
	 * Creates a new LocationAdapter with its connection to the database closed
	 */
	public LocationAdapter() {
	}
	
	@Override
	public void open() {
		super.open();
		areasAdapter = new MapAreaDataAdapter(db);
	}
	
	@Override
	public void close() {
		super.close();
		areasAdapter = null;
	}
	
	/**
	 * Loads a location from the database
	 * 
	 * @param id The id of the location to load
	 * @return A new Location instance without loaded corners
	 */
	public Location getLocation(long id) {
		String where = KEY_ID + "=?";
		String[] args = new String[] {String.valueOf(id)};
		
		Cursor cursor = db.query(TABLE_NAME, null, where, args, null, null, null);
		cursor.moveToFirst();
		return convertCursorRow(cursor);
	}
	
	/**
	 * Query for building overlay information
	 * 
	 * @param textVisible Should buildings with visible text be returned
	 * @return A new cursor with the following columns: Locations._Id, Name, CenterLat, CenterLon, MinZoomLevel
	 */
	public Cursor getBuildingOverlayCursor(boolean textVisible) {
		/*
		String q = 
			"SELECT" + columns(column("locs", KEY_ID), "Name", "CenterLat", "CenterLon", "MinZoomLevel") +
			"FROM" + tables(table(TABLE_NAME, "locs"), table("MapAreaData", "mapAreas")) +
			"WHERE locs.MapAreaId = mapAreas._Id AND " +
			" mapAreas.LabelOnHybrid=?";
		*/
		
		String query = "SELECT Locations._Id, Name, CenterLat, CenterLon, MinZoomLevel" +
		" FROM Locations, MapAreaData" +
		" WHERE Locations.MapAreaId = MapAreaData._Id AND" +
		"  MapAreaData.LabelOnHybrid=?";		
		String[] args = new String[] {textVisible ? "1" : "0"};
		
		return db.rawQuery(query, args);
	}
	
	/**
	 * Return a cursor over items on the quick list
	 * 
	 * @return A new Cursor with ids and names
	 */
	public Cursor getQuickListCursor() {
		String[] projection = new String[] {KEY_ID, KEY_NAME};
		return db.query(TABLE_NAME, projection, KEY_IS_ON_QUICK_LIST + "=1", null, null, null, KEY_NAME);
	}
	
	/**
	 * Return a cursor over building corners
	 * 
	 * @param buildingId the map area id to iterate over
	 * @return A new Cursor with lat lon pairs
	 */
	public Cursor getBuildingCornersCursor(int buildingId) {
		return areasAdapter.getCornersCursor(buildingId);
	}
	
	/**
	 * Iterate over every building that has an associated map area
	 * 
	 * @return An iterator over Location objects
	 */
	public DbIterator<Location> getBuildingIterator() {
		String where = KEY_MAP_AREA_ID + " IS NOT NULL";
		
		Cursor cursor = db.query(TABLE_NAME, null, where, null, null, null, null);
		return new BuildingIterator(cursor);
	}
	
	/**
	 * Iterate over every point of interest in the database
	 * 
	 * @return An iterator over Location objects
	 * 
	 */
	public DbIterator<Location> getPOIIterator() {
		String where = KEY_IS_POI + " ='1' AND " + KEY_MAP_AREA_ID + " IS NULL";
		
		Cursor cursor = db.query(TABLE_NAME, null, where, null, null, null, null);
		return new BuildingIterator(cursor);
	}
	
	/**
	 * Populate MapArea information in a Location
	 * 
	 * @param area The location to populate
	 * @param includeCorners True if corners should be loaded as well
	 */
	public void loadMapArea(Location area, boolean includeCorners) {
		area.mapData = areasAdapter.loadMapArea(area.mapAreaId, includeCorners);
	}
	
	/**
	 * Remove all Locations from the database and replace them with the supplied data
	 * 
	 * @param newData the locations to add to the database
	 */
	public void replaceBuildings(Location[] newData) {
		db.beginTransaction();
		
		//delete all records
		db.delete(TABLE_NAME, null, null);
		areasAdapter.clear();
		
		//add each building to the database
		for (Location building : newData) {
			
			ContentValues values = new ContentValues();
			values.put(KEY_ID, building.id);
			
			//add the corresponding map area first
			if (building.mapData != null) {
				long mapAreaId = areasAdapter.add(building.mapData);
				values.put(KEY_MAP_AREA_ID, mapAreaId);
			}
			
			if (building.parentId >= 0)
				values.put(KEY_PARENT_ID, building.parentId);
			values.put(KEY_NAME, building.name);
			if (building.description != null)
				values.put(KEY_DESCRIPTION, building.description);
			values.put(KEY_CENTER_LAT, building.center.lat);
			values.put(KEY_CENTER_LON, building.center.lon);
			values.put(KEY_IS_POI, building.isPOI);
			values.put(KEY_IS_ON_QUICK_LIST, building.isOnQuickList);
			
			db.insert(TABLE_NAME, null, values);
		}
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	private long getNullableId(Cursor cursor, int columnIndex) {
		if (cursor.isNull(columnIndex)) {
			return -1;
		}
		
		return cursor.getLong(columnIndex);
	}
	
	private Location convertCursorRow(Cursor cursor) {
		Location area = new Location();
		
		area.id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
		area.parentId = getNullableId(cursor, cursor.getColumnIndex(KEY_PARENT_ID));
		area.mapAreaId = getNullableId(cursor, cursor.getColumnIndex(KEY_MAP_AREA_ID));
		area.name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
		area.description = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
		area.isPOI = getBoolean(cursor, cursor.getColumnIndex(KEY_IS_POI));
		area.isOnQuickList = getBoolean(cursor, cursor.getColumnIndex(KEY_IS_ON_QUICK_LIST));
		int lat = cursor.getInt(cursor.getColumnIndex(KEY_CENTER_LAT));
		int lon = cursor.getInt(cursor.getColumnIndex(KEY_CENTER_LON));
		area.center = new LatLon(lat, lon);
		
		return area;
	}
	
	private class BuildingIterator extends DbIterator<Location> {

		public BuildingIterator(Cursor cursor) {
			super(cursor);
		}

		@Override
		protected Location convertRow(Cursor cursor) {
			return convertCursorRow(cursor);
		}
		
	}
}
