package edu.rosehulman.android.directory.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class TableAdapter {

	private SQLiteOpenHelper dbOpenHelper;
	protected SQLiteDatabase db;
	
	public TableAdapter() {
		dbOpenHelper = DatabaseHelper.getInstance(null);
	}
	
	public TableAdapter(SQLiteDatabase db) {
		this.db = db;
	}
	
	public void open() {
		if (db != null) {
			db.close();
			db = null;
		}
		db = dbOpenHelper.getWritableDatabase();
	}
	
	public void close() {
		db.close();
	}
	
}
