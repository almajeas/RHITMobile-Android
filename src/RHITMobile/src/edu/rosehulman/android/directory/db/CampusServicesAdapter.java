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
	public static final String KEY_NAME = "Name";
	public static final String KEY_URL = "Url";
	public static final String KEY_PRE = "Pre";
	public static final String KEY_POST = "Post";
	
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
		int pre = 1;
		for (CampusServicesCategory category : categories) {
			pre = addCategory(category, pre);
		}
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	/**
	 * Adds a category, and all of its links, to the database
	 * 
	 * @param category The category to add
	 * @return the post number for the parent
	 */
	private int addCategory(CampusServicesCategory category, int pre) {
		int i = pre+1;
		
		//add each link
		for (Hyperlink link : category.entries) {
			ContentValues values = new ContentValues();
			values.put(KEY_NAME, link.name);
			values.put(KEY_URL, link.url);
			values.put(KEY_PRE, i);
			values.put(KEY_POST, i+1);
			i += 2;
			db.insertOrThrow(TABLE_NAME, null, values);
		}
		
		//add each category
		for (CampusServicesCategory child : category.children) {
			i = addCategory(child, i);
		}
		
		//add this category
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, category.name);
		values.put(KEY_PRE, pre);
		values.put(KEY_POST, i);
		db.insertOrThrow(TABLE_NAME, null, values);
		
		return i+1;
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
				columnAlias(KEY_NAME, SearchManager.SUGGEST_COLUMN_TEXT_1),
				columnAlias("group_concat(Path, '/')", SearchManager.SUGGEST_COLUMN_TEXT_2)
				) + 
				"FROM (SELECT c1._Id as _Id, c1.Name AS Name, c2.Name AS Path FROM CampusServices c1 " +
				"INNER JOIN CampusServices c2 " +
				"ON c2.Pre < c1.Pre AND c2.Post > c1.Post " +
				"WHERE c1.Url IS NOT NULL AND c1.Pre + 1 = c1.Post AND c1.Name LIKE ? " +
				"ORDER BY c1.Name, c2.Name) " +
				"GROUP BY Name " +
				"LIMIT 10 ";

		String[] args = new String[] {"%" + path + "%"};
		return db.rawQuery(query, args);
	}
	
	/**
	 * Retrieves an array of categories for a given filter
	 * 
	 * @param filter The string used to filter the category or hyperlink name
	 * @return An array of categories matching either criteria
	 */
	public CampusServicesCategory[] getCategories(String filter) {
		String query = "SELECT Pre, Post, Name, Url FROM CampusServices " +
			"WHERE (Url IS NOT NULL AND Pre + 1 = Post AND Name LIKE ?) OR (Url IS NULL) " +
			"ORDER BY Pre";
		String args[] = {"%" + filter + "%"};
		Cursor cursor = db.rawQuery(query, args);
		cursor.moveToFirst();
		
		return new SearchParser(cursor).convert();
	}
	
	private class SearchParser {
		private Cursor cursor;
		private int iPre;
		private int iPost;
		private int iName;
		private int iUrl;
		
		public SearchParser(Cursor cursor) {
			this.cursor = cursor;
			iPre = cursor.getColumnIndex(KEY_PRE);
			iPost = cursor.getColumnIndex(KEY_POST);
			iName = cursor.getColumnIndex(KEY_NAME);
			iUrl = cursor.getColumnIndex(KEY_URL);
		}
		
		public CampusServicesCategory[] convert() {
			cursor.moveToFirst();
			
			CampusServicesCategory[] res;
			res = getCategories(1, Integer.MAX_VALUE);
			cursor.close();
			return res;
		}
		
		private Hyperlink[] getHyperlinks(int pre, int post) {
			List<Hyperlink> links = new ArrayList<Hyperlink>();
		
			Hyperlink link;
			while ((link = getHyperlink(pre, post)) != null) {
				links.add(link);
			}
			
			Hyperlink res[] = new Hyperlink[links.size()];
			links.toArray(res);
			return res;
		}
		
		private Hyperlink getHyperlink(int pre, int post) {
			if (cursor.isAfterLast())
				return null;
			
			int childPre = cursor.getInt(iPre);
			int childPost = cursor.getInt(iPost);
			
			if (childPre < pre || childPost > post)
				return null;
			
			if (cursor.isNull(iUrl))
				return null;
			
			String name = cursor.getString(iName);
			String url = cursor.getString(iUrl);
			Hyperlink link = new Hyperlink(name, url);
			cursor.moveToNext();
			return link;
		}
		
		private CampusServicesCategory[] getCategories(int pre, int post) {
			List<CampusServicesCategory> children = new ArrayList<CampusServicesCategory>();
		
			CampusServicesCategory child;
			while ((child = getCategory(pre, post)) != null) {
				if (child.entries.length > 0 || child.children.length > 0)
					children.add(child);
			}
			
			CampusServicesCategory res[] = new CampusServicesCategory[children.size()];
			children.toArray(res);
			return res;
		}
	
		private CampusServicesCategory getCategory(int pre, int post) {
			if (cursor.isAfterLast())
				return null;

			int childPre = cursor.getInt(iPre);
			int childPost = cursor.getInt(iPost);			
			if (childPre < pre || childPost > post)
				return null;
			
			CampusServicesCategory category = new CampusServicesCategory();
			category.name = cursor.getString(iName);
			cursor.moveToNext();
			
			category.entries = getHyperlinks(childPre, childPost);
			category.children = getCategories(childPre, childPost);
			
			return category;
		}
		
	}
}
