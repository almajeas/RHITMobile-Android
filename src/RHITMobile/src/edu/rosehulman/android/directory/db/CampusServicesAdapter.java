package edu.rosehulman.android.directory.db;

import java.util.ArrayList;
import java.util.List;

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
		
		DbIterator<CampusServicesCategory> it = new CategoryIterator(cursor);
		List<CampusServicesCategory> categories = new ArrayList<CampusServicesCategory>();
		while (it.hasNext()) {
			categories.add(it.getNext());
		}
		CampusServicesCategory res[] = new CampusServicesCategory[categories.size()];
		categories.toArray(res);
		
		return res;
	}

	private class CategoryIterator extends DbIterator<CampusServicesCategory> {

		public CategoryIterator(Cursor cursor) {
			super(cursor);
		}

		@Override
		protected CampusServicesCategory convertRow(Cursor cursor) {
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
	
	
}
