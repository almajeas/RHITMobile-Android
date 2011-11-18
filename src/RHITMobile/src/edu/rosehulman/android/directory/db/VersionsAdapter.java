package edu.rosehulman.android.directory.db;

import android.content.ContentValues;
import android.database.Cursor;
import edu.rosehulman.android.directory.model.VersionType;

/**
 * Provide access to various version information stored in the database
 */
public class VersionsAdapter extends TableAdapter {

	public static final String TABLE_NAME = "Versions";
	
	public static final String KEY_ID = "_Id";
	public static final String KEY_VERSION = "Version";
	
	/**
	 * Get the recorded version of a particular type
	 * 
	 * @param versionType The system we want the current version of
	 * @return The version, or 0 if not set
	 */
	public String getVersion(VersionType versionType) {
		String projection[] = {KEY_VERSION};
		String args[] = {String.valueOf(versionType.ordinal())};
		Cursor cursor;
		cursor = db.query(TABLE_NAME, projection, KEY_ID+"=?", args, null, null, null);
		if (cursor.getCount() == 0) {
			return null;
		}
		return getString(cursor);
	}
	
	/**
	 * Set the version of a particular type
	 * 
	 * @param versionType The version type to set
	 * @param version The new version
	 */
	public void setVersion(VersionType versionType, String version) {
		db.beginTransaction();
		
		//remove the old value
		String args[] = {String.valueOf(versionType.ordinal())};
		db.delete(TABLE_NAME, KEY_ID+"=?", args);
		
		//set the new one
		ContentValues values = new ContentValues();
		values.put(KEY_ID, versionType.ordinal());
		values.put(KEY_VERSION, version);
		db.insertOrThrow(TABLE_NAME, null, values);
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
}
