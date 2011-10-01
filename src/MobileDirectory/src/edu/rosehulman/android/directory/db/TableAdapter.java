package edu.rosehulman.android.directory.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Provide generic functionality to all database adapters
 */
public abstract class TableAdapter {

	private SQLiteOpenHelper dbOpenHelper;
	protected SQLiteDatabase db;
	
	/**
	 * Creates a new TableAdapter
	 */
	public TableAdapter() {
		dbOpenHelper = DatabaseHelper.getInstance();
	}
	
	/**
	 * Creates a new TableAdapter with an existing database connection
	 * 
	 * @param db The database connection to use
	 */
	public TableAdapter(SQLiteDatabase db) {
		this.db = db;
	}
	
	/**
	 * Open a writable version of the database
	 */
	public void open() {
		if (db != null) {
			db.close();
			db = null;
		}
		db = dbOpenHelper.getWritableDatabase();
	}
	
	/**
	 * Close an open database connection.
	 * 
	 * TableAdapters created with an existing connection should
	 * not close the connection to the database.
	 */
	public void close() {
		db.close();
	}
	
}
