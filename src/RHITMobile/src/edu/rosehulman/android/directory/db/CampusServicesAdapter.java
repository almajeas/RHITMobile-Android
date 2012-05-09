package edu.rosehulman.android.directory.db;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import android.content.ContentValues;
import android.database.Cursor;
import edu.rosehulman.android.directory.model.CampusServiceItem;
import edu.rosehulman.android.directory.model.CampusServicesCategory;
import edu.rosehulman.android.directory.model.Hyperlink;
import edu.rosehulman.android.directory.model.HyperlinkType;

/**
 * Provide access to various version information stored in the database
 */
public class CampusServicesAdapter extends TableAdapter {

	public static final String TABLE_NAME = "CampusServices";
	
	public static final String KEY_ID = "_Id";
	public static final String KEY_PARENT = "Parent";
	public static final String KEY_NAME = "Name";
	public static final String KEY_URL = "Url";
	public static final String KEY_PRE = "Pre";
	public static final String KEY_POST = "Post";
	
	/**
	 * Replace all data with the given categories
	 * 
	 * @param root The new category data to use
	 */
	public void replaceData(CampusServicesCategory root) {
		db.beginTransaction();
		
		//delete all records
		db.delete(TABLE_NAME, null, null);
		
		//add each record to the database
		root.name = null;
		addCategory(root, 1);
		
		//update parent ids
		updateParents(root);
		
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	/**
	 * Adds a category, and all of its links, to the database
	 * 
	 * @param category The category to add
	 * @param pre The DFS pre number to start with
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
		category.id = db.insertOrThrow(TABLE_NAME, null, values);
		
		return i+1;
	}
	
	private void updateParents(CampusServicesCategory root) {
		Cursor cursor;
		long pre;
		long post;
		{		
			String projection[] = {KEY_PRE, KEY_POST};
			String args[] = {String.valueOf(root.id)};
			String where = KEY_ID + "=?";
			cursor = db.query(TABLE_NAME, projection, where, args, null, null, null);
			
			assert(cursor.getCount() == 1);
			cursor.moveToFirst();
			pre = cursor.getLong(cursor.getColumnIndex(KEY_PRE));
			post = cursor.getLong(cursor.getColumnIndex(KEY_POST));
			cursor.close();
		}
		
		//update this category
		String where = KEY_PRE + ">? AND " + KEY_POST + "<?";
		String args[] = new String[] {String.valueOf(pre), String.valueOf(post)};
		ContentValues values = new ContentValues();
		values.put(KEY_PARENT, root.id);
		db.update(TABLE_NAME, values, where, args);

		//update the children
		for (CampusServicesCategory child : root.children) {
			updateParents(child);
		}
	}
	
	/**
	 * Retrieves the id of the root category
	 * 
	 * @return the id of the root category
	 */
	public long getRootId() {
		String projection[] = {KEY_ID};
		Cursor cursor;
		String where = KEY_PRE + "='1'";
		cursor = db.query(TABLE_NAME, projection, where, null, null, null, null);
		
		if (cursor.getCount() != 1)
			return -1;
		cursor.moveToFirst();
		long id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
		cursor.close();
		
		return id;
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
		
		return new Hyperlink(name, HyperlinkType.WEBSITE, url);
	}

	/**
	 * Searches for the given string
	 * 
	 * @param filter The search filter
	 * @return An iterator of search results
	 */
	public DbIterator<CampusServiceItem> search(String filter) {
		if (filter.length() == 0) {
			return null;
		}
		
		String query = 
				"SELECT Name, Url, group_concat(Node, '/') AS Path " + 
				"FROM (SELECT c1._Id as Id, c1.Name AS Name, c2.Name AS Node, c1.Url as Url " +
				"  FROM CampusServices c1 " + 
				"  INNER JOIN CampusServices c2 " + 
				"  ON c2.Pre < c1.Pre AND c2.Post > c1.Post " + 
				"  WHERE c1.Url IS NOT NULL AND c1.Pre + 1 = c1.Post AND c1.Name LIKE ? " + 
				"  ORDER BY c1.Name, c2.Pre) " + 
				"GROUP BY Id " ;

		String[] args = new String[] {"%" + filter + "%"};
		return new SearchIterator(db.rawQuery(query, args));
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
				"ORDER BY c1.Name, c2.Pre) " +
				"GROUP BY _Id " +
				"LIMIT 10 ";

		String[] args = new String[] {"%" + path + "%"};
		return db.rawQuery(query, args);
	}
	
	/**
	 * Retrieves a single level of a single category
	 *  
	 * @param id The id of the category to retrieve (or -1 for root)
	 * @return The requested category
	 */
	public CampusServicesCategory getCategory(long id) {
		if (id < 0) {
			id = getRootId();
		}
		
		String query = "SELECT _Id, Pre, Post, Name, Url FROM CampusServices " +
				"WHERE _Id=? OR Parent=?" +
				"ORDER BY Pre";
		String args[] = {String.valueOf(id), String.valueOf(id)};
		Cursor cursor = db.rawQuery(query, args);

		return new SearchParser(cursor, false).convert();
	}
	

	/**
	 * Computes the path to the given node
	 * 
	 * @param id The id of a node
	 * @param includeNode Should the given node be included
	 * @return The path to get to that node
	 */
	public String getPath(long id, boolean includeNode) {
		if (id < 0) {
			return "";
		}
		
		Cursor cursor;
		long pre;
		long post;
		{		
			String projection[] = {KEY_PRE, KEY_POST};
			String args[] = {String.valueOf(id)};
			String where = KEY_ID + "=?";
			cursor = db.query(TABLE_NAME, projection, where, args, null, null, null);
			
			if (cursor.getCount() != 1)
				return null;
			
			cursor.moveToFirst();
			pre = cursor.getLong(cursor.getColumnIndex(KEY_PRE));
			post = cursor.getLong(cursor.getColumnIndex(KEY_POST));
			cursor.close();
		}
		
		String where;
		if (includeNode) {
			where = "Pre<=? AND Post>=? AND Pre>1 ";
		} else {
			where = "Pre<? AND Post>? AND Pre>1 ";
		}
		String query = 
		"SELECT group_concat(Name, '/') AS Path " +
		"FROM (SELECT Name " + 
		"  FROM CampusServices " +
		"  WHERE " + where +
		"  ORDER BY Pre)";
		String args[] = {String.valueOf(pre), String.valueOf(post)};
		cursor = db.rawQuery(query, args);
		
		if (cursor.getCount() != 1)
			return null;
		cursor.moveToFirst();
		String path = cursor.getString(0);
		cursor.close();
		
		return path;
	}
	
	private class SearchIterator extends DbIterator<CampusServiceItem> {
		
		private int iName;
		private int iUrl;
		private int iPath;

		public SearchIterator(Cursor cursor) {
			super(cursor);
			iName = cursor.getColumnIndex(KEY_NAME);
			iUrl = cursor.getColumnIndex(KEY_URL);
			iPath = cursor.getColumnIndex("Path");
		}

		@Override
		protected CampusServiceItem convertRow(Cursor cursor) {
			String name = cursor.getString(iName);
			String url = cursor.getString(iUrl);
			String path = cursor.getString(iPath);
			
			return new CampusServiceItem(new Hyperlink(name, HyperlinkType.WEBSITE, url), path);
		}
		
	}
	
	private class SearchParser {
		private Cursor cursor;
		private int iId;
		private int iPre;
		private int iPost;
		private int iName;
		private int iUrl;
		private boolean trim;
		
		public SearchParser(Cursor cursor, boolean trim) {
			this.cursor = cursor;
			this.trim = trim;
			iId = cursor.getColumnIndex(KEY_ID);
			iPre = cursor.getColumnIndex(KEY_PRE);
			iPost = cursor.getColumnIndex(KEY_POST);
			iName = cursor.getColumnIndex(KEY_NAME);
			iUrl = cursor.getColumnIndex(KEY_URL);
		}
		
		public CampusServicesCategory convert() {
			cursor.moveToFirst();
			
			CampusServicesCategory res;
			res = getCategory(1, Integer.MAX_VALUE);
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
			Hyperlink link = new Hyperlink(name, HyperlinkType.WEBSITE, url);
			cursor.moveToNext();
			return link;
		}
		
		private CampusServicesCategory[] getCategories(int pre, int post) {
			List<CampusServicesCategory> children = new ArrayList<CampusServicesCategory>();
		
			CampusServicesCategory child;
			while ((child = getCategory(pre, post)) != null) {
				if (!trim || child.entries.length > 0 || child.children.length > 0)
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
			category.id = cursor.getLong(iId);
			category.name = cursor.getString(iName);
			cursor.moveToNext();
			
			category.entries = getHyperlinks(childPre, childPost);
			category.children = getCategories(childPre, childPost);
			
			return category;
		}
		
	}
}
