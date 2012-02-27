package edu.rosehulman.android.directory.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import edu.rosehulman.android.directory.db.TourTagsAdapter;

/**
 * Provide search suggestions to the location search dialog
 */
public class TourTagsSearchSuggestions extends ContentProvider {
	
	public static final Uri CONTENT_URI = Uri.parse("content://edu.rosehulman.android.directory.providers.CampusServicesSearchSuggestions");

	private TourTagsAdapter adapter;
	
	private synchronized void init() {
		if (adapter == null) {
			adapter = new TourTagsAdapter();
			adapter.open();
		}
	}
	
	@Override
	public boolean onCreate() {
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		init();
		
		String path = selectionArgs[0];
		return adapter.searchSuggestions(path);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

}
