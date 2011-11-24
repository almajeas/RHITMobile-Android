package edu.rosehulman.android.directory.db;

import android.app.SearchManager;
import android.content.ContentValues;
import android.database.Cursor;
import edu.rosehulman.android.directory.model.Hyperlink;
import edu.rosehulman.android.directory.model.LatLon;
import edu.rosehulman.android.directory.model.LightLocation;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.model.LocationType;

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
	public static final String KEY_TYPE = "Type";
	public static final String KEY_CHILDREN_LOADED = "ChildrenLoaded";
	
	private MapAreaDataAdapter areasAdapter;
	private AlternateNamesAdapter namesAdapter;
	private HyperlinksAdapter linksAdapter;
	
	/**
	 * Creates a new LocationAdapter with its connection to the database closed
	 */
	public LocationAdapter() {
	}
	
	@Override
	public void open() {
		super.open();
		areasAdapter = new MapAreaDataAdapter(db);
		namesAdapter = new AlternateNamesAdapter(db);
		linksAdapter = new HyperlinksAdapter(db);
	}
	
	@Override
	public void close() {
		super.close();
		areasAdapter = null;
		linksAdapter = null;
		namesAdapter = null;
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
		String where = KEY_TYPE + "=?";
		String[] args = new String[] {String.valueOf(LocationType.ON_QUICK_LIST.ordinal())};
		
		return db.query(TABLE_NAME, projection, where, args, null, null, KEY_NAME);
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
		return new LocationIterator(cursor);
	}
	
	/**
	 * Iterate over every point of interest in the database
	 * 
	 * @return An iterator over Location objects
	 * 
	 */
	public DbIterator<Location> getPOIIterator() {
		String typeClause = KEY_TYPE + "=?";
		String where = "(" + typeClause + " OR " + typeClause + ") AND " + KEY_MAP_AREA_ID + " IS NULL";
		String[] args = new String[] {String.valueOf(LocationType.POINT_OF_INTEREST.ordinal()),
										String.valueOf(LocationType.ON_QUICK_LIST.ordinal())};
		
		Cursor cursor = db.query(TABLE_NAME, null, where, args, null, null, null);
		return new LocationIterator(cursor);
	}
	
	/**
	 * Get the children to this location
	 * 
	 * @param id The id of the parent location
	 * @return A cursor with _Id and Name fields
	 */
	public DbIterator<LightLocation> getChildren(long id) {
		String[] projection = new String[] {KEY_ID, KEY_NAME};
		String where = KEY_PARENT_ID + "=?";
		String[] args = new String[] {String.valueOf(id)};
		
		Cursor cursor = db.query(TABLE_NAME, projection, where, args, null, null, null);
		return new LightLocationIterator(cursor);
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
	 * Populate alternate names associated with a Location
	 * 
	 * @param location The location to populate
	 */
	public void loadAlternateNames(Location location) {
		location.altNames = namesAdapter.getAlternateNames(location.id);
	}
	
	/**
	 * Populate Hyperlink information in a Location
	 * 
	 * @param location The location to populate
	 */
	public void loadHyperlinks(Location location) {
		location.links = linksAdapter.getHyperlinks(location.id);
	}
	
	/**
	 * Remove all Locations from the database and replace them with the supplied data
	 * 
	 * @param locations the locations to add to the database
	 */
	public void replaceLocations(Location[] locations) {
		db.beginTransaction();
		
		//delete all records
		namesAdapter.clear();
		linksAdapter.clear();
		db.delete(TABLE_NAME, null, null);
		areasAdapter.clear();
		
		//add each building to the database
		for (Location location : locations) {
			addLocation(location);
			setChildrenLoaded(location.id, false);
		}
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	/**
	 * Adds a location to the database
	 * 
	 * @param location The location to add
	 */
	public void addLocation(Location location) {
		ContentValues values = new ContentValues();
		values.put(KEY_ID, location.id);
		
		//add the corresponding map area first
		if (location.mapData != null) {
			long mapAreaId = areasAdapter.add(location.mapData);
			values.put(KEY_MAP_AREA_ID, mapAreaId);
		}
		
		if (location.parentId >= 0)
			values.put(KEY_PARENT_ID, location.parentId);
		values.put(KEY_NAME, location.name);
		if (location.description != null)
			values.put(KEY_DESCRIPTION, location.description);
		values.put(KEY_CENTER_LAT, location.center.lat);
		values.put(KEY_CENTER_LON, location.center.lon);
		values.put(KEY_TYPE, location.type.ordinal());
		
		db.insert(TABLE_NAME, null, values);

		//add any alternate names
		for (String name : location.altNames){
			namesAdapter.addName(location.id, name);
		}
		
		//add any hyperlinks
		for (Hyperlink link : location.links){
			linksAdapter.addHyperlink(location.id, link);
		}
	}
	
	/**
	 * Marks the given location's children as loaded or not
	 * 
	 * @param id The id of the location to update
	 * @param loaded The value to use
	 */
	public void setChildrenLoaded(long id, boolean loaded) {
		ContentValues values = new ContentValues();
		values.put(KEY_CHILDREN_LOADED, loaded);
		
		String where = KEY_ID + "=?";
		String[] args = new String[] {String.valueOf(id)};
		
		db.update(TABLE_NAME, values, where, args);
	}
	
	/**
	 * Gets the IDs of locations that have not yet loaded their children
	 * 
	 * @return an array of IDs to load
	 */
	public long[] getUnloadedTopLocations() {
		String where = KEY_CHILDREN_LOADED + "=0";
		
		return getTopLocations(where);
	}
	
	/**
	 * Gets the IDs of locations that are considered top level
	 * 
	 * @return an array of IDs
	 */
	public long[] getAllTopLocations() {
		String where = KEY_CHILDREN_LOADED + " IS NOT NULL";
		
		return getTopLocations(where);
	}
	

	/**
	 * Provide suggestions for a search query
	 * 
	 * @param path The path that the user has entered so far
	 * @return A Cursor that contains suggestions to display
	 */
	public Cursor searchSuggestions(String path) {
		if (path.length() == 0) {
			return null;
		}
		
		String query = "SELECT " + columns(
				columnAlias(KEY_ID, "_id"),
				columnAlias(KEY_NAME, SearchManager.SUGGEST_COLUMN_TEXT_1),
				columnAlias(KEY_DESCRIPTION, SearchManager.SUGGEST_COLUMN_TEXT_2)
				) + 
				"FROM " + TABLE_NAME + " " +
				"WHERE Name LIKE ?";
		String[] args = new String[] {"%" + path + "%"};
		return db.rawQuery(query, args);
	}
	
	private long[] getTopLocations(String where) {
		String[] projection = new String[] {KEY_ID, KEY_NAME};
		Cursor cursor = db.query(TABLE_NAME, projection, where, null, null, null, null);
		
		long[] res = new long[cursor.getCount()];
		for (int i = 0; cursor.moveToNext(); i++) {
			res[i] = cursor.getLong(0);
		}
		
		cursor.close();
		return res;
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
		area.type = LocationType.fromOrdinal(cursor.getInt(cursor.getColumnIndex(KEY_TYPE)));
		int lat = cursor.getInt(cursor.getColumnIndex(KEY_CENTER_LAT));
		int lon = cursor.getInt(cursor.getColumnIndex(KEY_CENTER_LON));
		area.center = new LatLon(lat, lon);
		
		return area;
	}
	
	private class LocationIterator extends DbIterator<Location> {

		public LocationIterator(Cursor cursor) {
			super(cursor);
		}

		@Override
		protected Location convertRow(Cursor cursor) {
			return convertCursorRow(cursor);
		}
		
	}
	
	private class LightLocationIterator extends DbIterator<LightLocation> {

		public LightLocationIterator(Cursor cursor) {
			super(cursor);
		}

		@Override
		protected LightLocation convertRow(Cursor cursor) {
			long id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
			String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
			return new LightLocation(id, name);
		}
		
	}
}
