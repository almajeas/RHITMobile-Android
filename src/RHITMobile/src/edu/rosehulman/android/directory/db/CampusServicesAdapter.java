package edu.rosehulman.android.directory.db;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import android.content.ContentValues;
import android.database.Cursor;
import edu.rosehulman.android.directory.model.CampusServicesCategory;
import edu.rosehulman.android.directory.model.Hyperlink;

/**
 * Provide access to various version information stored in the database
 */
public class CampusServicesAdapter extends TableAdapter {

	public static final String TABLE_NAME = "CampusServices";
	
	public static final String KEY_ID = "_Id";
	public static final String KEY_CATEGORY = "Category";
	public static final String KEY_NAME = "Name";
	public static final String KEY_URL = "Url";
	
	/**
	 * Replace all data with the given categories
	 * 
	 * @param categories The new category data to use
	 */
	public void replaceData(CampusServicesCategory[] categories) {
		db.beginTransaction();
		
		//delete all records
		db.delete(TABLE_NAME, null, null);
		
		//add each record to the database
		for (CampusServicesCategory category : categories) {
			addCategory(category);
		}
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	/**
	 * Adds a category, and all of its links, to the database
	 * 
	 * @param category The category to add
	 */
	private void addCategory(CampusServicesCategory category) {
		ContentValues baseValues = new ContentValues();
		baseValues.put(KEY_CATEGORY, category.name);
		for (Hyperlink link : category.entries) {
			ContentValues values = new ContentValues(baseValues);
			values.put(KEY_NAME, link.name);
			values.put(KEY_URL, link.url);
			db.insertOrThrow(TABLE_NAME, null, values);
		}
	}
	
	/**
	 * Loads a single hyperlink by id
	 * 
	 * @param id The id of the link to load
	 * @return The Hyperlink
	 */
	public Hyperlink getHyperLink(Long id) {
		String projection[] = {KEY_NAME, KEY_URL};
		String args[] = {String.valueOf(id)};
		Cursor cursor;
		String where = KEY_ID + "=?";
		cursor = db.query(TABLE_NAME, projection, where, args, null, null, null);
		
		if (cursor.getCount() != 1)
			return null;
		cursor.moveToFirst();
		
		String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
		String url = cursor.getString(cursor.getColumnIndex(KEY_URL));
		cursor.close();
		
		return new Hyperlink(name, url);
	}
	
	/**
	 * Provides search suggestions to the corresponding provider
	 * 
	 * @param path The search filter
	 * @return A cursor suitable for the provider
	 */
	public Cursor searchSuggestions(String path) {
		if (path.length() == 0) {
			return null;
		}
		
		String query = "SELECT " + columns(
				columnAlias(KEY_ID, "_id"),
				columnAlias(KEY_ID, SearchManager.SUGGEST_COLUMN_INTENT_DATA),
				columnAlias(KEY_CATEGORY+"||'/'||"+KEY_NAME, SearchManager.SUGGEST_COLUMN_TEXT_1),
				columnAlias(KEY_URL, SearchManager.SUGGEST_COLUMN_TEXT_2)
				) + 
				"FROM " + TABLE_NAME + " " +
				"WHERE Name LIKE ? OR Category LIKE ? LIMIT 10";
		
		String[] args = new String[] {"%" + path + "%", "%" + path + "%"};
		return db.rawQuery(query, args);
	}
	
	/**
	 * Retrieves an array of categories for a given filter
	 * 
	 * @param filter The string used to filter the category or hyperlink name
	 * @return An array of categories matching either criteria
	 */
	public CampusServicesCategory[] getCategories(String filter) {
		String projection[] = {KEY_CATEGORY, KEY_NAME, KEY_URL};
		String args[] = {"%" + filter + "%", "%" + filter + "%"};
		Cursor cursor;
		String where = KEY_NAME + " LIKE ? OR " + KEY_CATEGORY + " LIKE ?";
		String order = KEY_CATEGORY + ", " + KEY_NAME;
		cursor = db.query(TABLE_NAME, projection, where, args, null, null, order);
		cursor.moveToFirst();
		
		List<CampusServicesCategory> categories = new ArrayList<CampusServicesCategory>();
		while (!cursor.isAfterLast()) {
			categories.add(getNextCategory(cursor));
		}
		CampusServicesCategory res[] = new CampusServicesCategory[categories.size()];
		categories.toArray(res);
		
		return res;
	}
	
	private CampusServicesCategory getNextCategory(Cursor cursor) {
		CampusServicesCategory category = new CampusServicesCategory();
		category.name = cursor.getString(cursor.getColumnIndex(KEY_CATEGORY));
		List<Hyperlink> links = new ArrayList<Hyperlink>();
		do {
			String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
			String url = cursor.getString(cursor.getColumnIndex(KEY_URL));
			links.add(new Hyperlink(name, url));
			cursor.moveToNext();
		} while (!cursor.isAfterLast() && category.name.equals(cursor.getString(cursor.getColumnIndex(KEY_CATEGORY))));
		
		category.entries = new Hyperlink[links.size()];
		links.toArray(category.entries);
		return category;
	}
}
