package edu.rosehulman.android.directory.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


/**
 * Performs operations on AltNames table
 */
public class AlternateNamesAdapter extends TableAdapter {

	public static final String TABLE_NAME = "AltNames";
	
	public static final String KEY_ID = "_Id";
	public static final String KEY_LOCATION_ID = "LocationId";
	public static final String KEY_NAME = "Name";
	
	/**
	 * Creates a new, closed instance
	 */
	public AlternateNamesAdapter() {
	}
	
	/**
	 * Creates a new instance with the given database connection
	 * 
	 * @param db The db connection to use
	 */
	public AlternateNamesAdapter(SQLiteDatabase db) {
		super(db);
	}
	
	/**
	 * Retrieve the alternate names for the given location id
	 * 
	 * @param id The id of the location to query
	 * @return an array of alternate names associated with the location
	 */
	public String[] getAlternateNames(long id) {
		String[] projection = new String[] {KEY_NAME};
		String where = KEY_LOCATION_ID + " =?";
		String[] args = new String[] {String.valueOf(id)};
		
		Cursor cursor = db.query(TABLE_NAME, projection, where, args, null, null, null);
		DbIterator<String> it = new AltNameIterator(cursor);
		String[] names = new String[cursor.getCount()];
		
		for (int i = 0; it.hasNext(); i++) {
			names[i] = it.getNext();
		}
		
		return names;
	}

	/**
	 * Adds an alternate name to the database
	 * 
	 * @param locationId The id of the location to associate with
	 * @param name The name to add
	 */
	public void addName(long locationId, String name) {
		ContentValues values = new ContentValues();
		
		values.put(KEY_LOCATION_ID, locationId);
		values.put(KEY_NAME, name);
		
		db.insert(TABLE_NAME, null, values);
	}
	
	/**
	 * Deletes all alternate names from the db
	 */
	public void clear() {
		db.delete(TABLE_NAME, null, null);
	}

	private class AltNameIterator extends DbIterator<String> {

		private int iName;
		
		public AltNameIterator(Cursor cursor) {
			super(cursor);
			iName = cursor.getColumnIndex(KEY_NAME);
		}

		@Override
		protected String convertRow(Cursor cursor) {
			return cursor.getString(iName);
		}
		
	}
	
}