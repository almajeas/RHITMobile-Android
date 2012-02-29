package edu.rosehulman.android.directory.db;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import android.content.ContentValues;
import android.database.Cursor;
import edu.rosehulman.android.directory.model.TourTag;
import edu.rosehulman.android.directory.model.TourTagsGroup;

/**
 * Provide access to various version information stored in the database
 */
public class TourTagsAdapter extends TableAdapter {

	public static final String TABLE_NAME = "TourTags";
	
	public static final String KEY_ID = "_Id";
	public static final String KEY_PRE = "Pre";
	public static final String KEY_POST = "Post";
	public static final String KEY_NAME = "Name";
	public static final String KEY_IS_DEFAULT = "IsDefault";
	public static final String KEY_TAG_ID = "TagId";
	
	/**
	 * Replace all data with the given categories
	 * 
	 * @param root The new category data to use
	 */
	public void replaceData(TourTagsGroup root) {
		db.beginTransaction();
		
		//delete all records
		db.delete(TABLE_NAME, null, null);
		
		//add each record to the database
		addCategory(root, 1);
		
		//update parent ids
		updateParents(root, -1);
		
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
	private int addCategory(TourTagsGroup category, int pre) {
		int i = pre+1;
		
		//add each link
		for (TourTag link : category.tags) {
			ContentValues values = new ContentValues();
			values.put(KEY_NAME, link.name);
			values.put(KEY_TAG_ID, link.tagId);
			values.put(KEY_IS_DEFAULT, link.isDefault);
			values.put(KEY_PRE, i);
			values.put(KEY_POST, i+1);
			i += 2;
			db.insertOrThrow(TABLE_NAME, null, values);
		}
		
		//add each category
		for (TourTagsGroup child : category.children) {
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
	
	private void updateParents(TourTagsGroup root, long parent) {
		//update this category
		String where = KEY_ID + "=?";
		String args[] = new String[] {String.valueOf(root.id)};
		ContentValues values = new ContentValues();
		if (parent >= 0)
			values.put(KEY_TAG_ID, parent);
		else
			values.putNull(KEY_TAG_ID);
		db.update(TABLE_NAME, values, where, args);

		//update the children
		for (TourTagsGroup child : root.children) {
			updateParents(child, root.id);
		}
	}
	
	public DbIterator<TourTag> getDefaultTags() {
		String projection[] = {KEY_NAME, KEY_TAG_ID};
		Cursor cursor;
		String where = KEY_IS_DEFAULT + "='1'";
		cursor = db.query(TABLE_NAME, projection, where, null, null, null, null);

		final int iTagId = cursor.getColumnIndex(KEY_TAG_ID);
		final int iName = cursor.getColumnIndex(KEY_NAME);
		
		return new DbIterator<TourTag>(cursor) {
			@Override
			protected TourTag convertRow(Cursor cursor) {
				TourTag res = new TourTag();
				
				res.tagId = cursor.getLong(iTagId);
				res.name = cursor.getString(iName);
				res.isDefault = true;
				
				return res;
			}
		};
	}
	
	/**
	 * Loads a single tag by tag id
	 * 
	 * @param id The tag id of the link to load
	 * @return The TourTag
	 */
	public TourTag getTag(long id) {
		String projection[] = {KEY_NAME, KEY_TAG_ID, KEY_IS_DEFAULT};
		String args[] = {String.valueOf(id)};
		Cursor cursor;
		String where = KEY_TAG_ID + "=?";
		cursor = db.query(TABLE_NAME, projection, where, args, null, null, null);
		
		if (cursor.getCount() != 1)
			return null;
		cursor.moveToFirst();
		
		String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
		long tagId = cursor.getLong(cursor.getColumnIndex(KEY_TAG_ID));
		boolean isDefault = getBoolean(cursor, cursor.getColumnIndex(KEY_IS_DEFAULT));
		cursor.close();
		
		TourTag res = new TourTag(tagId, name);
		res.isDefault = isDefault;
		return res;
	}
	
	/**
	 * Computes the path to the given tag node
	 * 
	 * @param tagId The tag id of a tag
	 * @return The path to get to that tag
	 */
	public String getTagPath(long tagId) {
		long id = getRowId(tagId);
		return getPath(id, false);
	}
	
	/**
	 * Computes the path to the given tag node
	 * 
	 * @param id The id of a tag
	 * @param includeNode Should the given node be included
	 * @return The path to get to that tag
	 */
	public String getPath(long id, boolean includeNode) {
		if (id < 0)
			return "";
		
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
		"  FROM TourTags " +
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
	
	/**
	 * Retrieves the id of the root group
	 * 
	 * @return the id of the root group
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
	 * Retrieves the id of given tag node
	 * 
	 * @param tagId the tag id of the tag node
	 * @return the id of the tag node
	 */
	public long getRowId(long tagId) {
		String projection[] = {KEY_ID};
		Cursor cursor;
		String where = KEY_TAG_ID + "=?";
		String[] args = new String[] {String.valueOf(tagId)};
		cursor = db.query(TABLE_NAME, projection, where, args, null, null, null);
		
		if (cursor.getCount() != 1)
			return -1;
		cursor.moveToFirst();
		long id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
		cursor.close();
		
		return id;
	}
	
	/**
	 * Retrieves a filtered subset of a single tag group.
	 * 
	 * isDefault attribute is not loaded.
	 * 
	 * @param id The id of the group
	 * @param query The search terms to use
	 * @return The tag group and all matching children
	 */
	public TourTagsGroup getGroup(Long id, String query) {
		if (id == -1) {
			id = getRootId();
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
		
		String projection[] = {KEY_ID, KEY_NAME, KEY_TAG_ID, KEY_PRE, KEY_POST};
		String args[] = {String.valueOf(pre), String.valueOf(post), "%" + query + "%"};
		String where = 
			KEY_PRE  + ">=? AND " +
			KEY_POST + "<=? AND " +
			"((Pre+1=Post AND Name LIKE ?) OR (Pre+1!=Post))"
			;
		cursor = db.query(TABLE_NAME, projection, where, args, null, null, KEY_PRE);
		cursor.moveToFirst();
		
		return new SearchParser(cursor).convert();
	}
	
	/**
	 * Provides search suggestions to the corresponding provider.
	 * 
	 * isDefault attribute is not loaded.
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
				"FROM (SELECT c1.TagId as _Id, c1.Name AS Name, c2.Name AS Path FROM TourTags c1 " +
				"INNER JOIN TourTags c2 " +
				"ON c2.Pre < c1.Pre AND c2.Post > c1.Post " +
				"WHERE c1.TagId IS NOT NULL AND c1.Pre + 1 = c1.Post AND c1.Name LIKE ? AND c2.Pre > 1 " +
				"ORDER BY c1.Name, c2.Name) " +
				"GROUP BY Name " +
				"LIMIT 10 ";

		String[] args = new String[] {"%" + path + "%"};
		return db.rawQuery(query, args);
	}
	
	/**
	 * Retrieves a group for a given filter
	 * 
	 * isDefault attribute is not loaded.
	 * 
	 * @param filter The string used to filter the category or name
	 * @return A group matching either criteria
	 */
	public TourTagsGroup getGroup(String filter) {
		String query = "SELECT _Id, Pre, Post, Name, Url FROM TourTags " +
			"WHERE (TagId IS NOT NULL AND Pre + 1 = Post AND Name LIKE ?) OR (TagId IS NULL) " +
			"ORDER BY Pre";
		String args[] = {"%" + filter + "%"};
		Cursor cursor = db.rawQuery(query, args);
		cursor.moveToFirst();
		
		return new SearchParser(cursor).convert();
	}
	
	private class SearchParser {
		private Cursor cursor;
		private int iId;
		private int iPre;
		private int iPost;
		private int iName;
		private int iTagId;
		
		public SearchParser(Cursor cursor) {
			this.cursor = cursor;
			iId = cursor.getColumnIndex(KEY_ID);
			iPre = cursor.getColumnIndex(KEY_PRE);
			iPost = cursor.getColumnIndex(KEY_POST);
			iName = cursor.getColumnIndex(KEY_NAME);
			iTagId = cursor.getColumnIndex(KEY_TAG_ID);
		}
		
		public TourTagsGroup convert() {
			cursor.moveToFirst();
			
			TourTagsGroup res;
			res = getGroup(1, Integer.MAX_VALUE);
			cursor.close();
			return res;
		}
		
		private TourTag[] getTags(int pre, int post) {
			List<TourTag> links = new ArrayList<TourTag>();
		
			TourTag link;
			while ((link = getTag(pre, post)) != null) {
				links.add(link);
			}
			
			TourTag res[] = new TourTag[links.size()];
			links.toArray(res);
			return res;
		}
		
		private TourTag getTag(int pre, int post) {
			if (cursor.isAfterLast())
				return null;
			
			int childPre = cursor.getInt(iPre);
			int childPost = cursor.getInt(iPost);
			
			if (childPre < pre || childPost > post)
				return null;
			
			if (childPre + 1 != childPost)
				return null;
			
			if (cursor.isNull(iTagId))
				return null;
			
			String name = cursor.getString(iName);
			long tagId = cursor.getLong(iTagId);
			TourTag link = new TourTag(tagId, name);
			cursor.moveToNext();
			return link;
		}
		
		private TourTagsGroup[] getGroups(int pre, int post) {
			List<TourTagsGroup> children = new ArrayList<TourTagsGroup>();
		
			TourTagsGroup child;
			while ((child = getGroup(pre, post)) != null) {
				if (child.tags.length > 0 || child.children.length > 0)
					children.add(child);
			}
			
			TourTagsGroup res[] = new TourTagsGroup[children.size()];
			children.toArray(res);
			return res;
		}
	
		private TourTagsGroup getGroup(int pre, int post) {
			if (cursor.isAfterLast())
				return null;

			int childPre = cursor.getInt(iPre);
			int childPost = cursor.getInt(iPost);			
			if (childPre < pre || childPost > post)
				return null;
			
			TourTagsGroup category = new TourTagsGroup();
			category.name = cursor.getString(iName);
			category.id = cursor.getLong(iId);
			cursor.moveToNext();
			
			category.tags = getTags(childPre, childPost);
			category.children = getGroups(childPre, childPost);
			
			return category;
		}
		
	}
}
