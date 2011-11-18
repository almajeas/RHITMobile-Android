package edu.rosehulman.android.directory.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import edu.rosehulman.android.directory.model.Hyperlink;

/**
 * Performs operations on Hyperlinks table
 */
public class HyperlinksAdapter extends TableAdapter {

	public static final String TABLE_NAME = "Hyperlinks";
	
	public static final String KEY_ID = "_Id";
	public static final String KEY_LOCATION_ID = "LocationId";
	public static final String KEY_NAME = "Name";
	public static final String KEY_URL = "Url";
	
	/**
	 * Creates a new, closed instance
	 */
	public HyperlinksAdapter() {
	}
	
	/**
	 * Creates a new instance with the given database connection
	 * 
	 * @param db The db connection to use
	 */
	public HyperlinksAdapter(SQLiteDatabase db) {
		super(db);
	}
	
	/**
	 * Retrieve the hyperlinks for the given location id
	 * 
	 * @param id The id of the location to query
	 * @return an array of Hyperlinks associated with the location
	 */
	public Hyperlink[] getHyperlinks(long id) {
		String[] projection = new String[] {KEY_NAME, KEY_URL};
		String where = KEY_LOCATION_ID + " =?";
		String[] args = new String[] {String.valueOf(id)};
		
		Cursor cursor = db.query(TABLE_NAME, projection, where, args, null, null, null);
		DbIterator<Hyperlink> it = new HyperlinkIterator(cursor);
		Hyperlink[] links = new Hyperlink[cursor.getCount()];
		
		for (int i = 0; it.hasNext(); i++) {
			links[i] = it.getNext();
		}
		
		return links;
	}

	/**
	 * Adds a hyperlink to the database
	 * 
	 * @param locationId The id of the location to associate with
	 * @param link The link to add
	 */
	public void addHyperlink(long locationId, Hyperlink link) {
		ContentValues values = new ContentValues();
		
		values.put(KEY_LOCATION_ID, locationId);
		values.put(KEY_NAME, link.name);
		values.put(KEY_URL, link.url);
		
		db.insert(TABLE_NAME, null, values);
	}
	
	/**
	 * Deletes all hyperlinks from the db
	 */
	public void clear() {
		db.delete(TABLE_NAME, null, null);
	}

	private class HyperlinkIterator extends DbIterator<Hyperlink> {

		private int iName;
		private int iUrl;
		
		public HyperlinkIterator(Cursor cursor) {
			super(cursor);
			iName = cursor.getColumnIndex(KEY_NAME);
			iUrl = cursor.getColumnIndex(KEY_URL);
		}

		@Override
		protected Hyperlink convertRow(Cursor cursor) {
			String name = cursor.getString(iName);
			String url = cursor.getString(iUrl);
			return new Hyperlink(name, url);
		}
		
	}
	
}
