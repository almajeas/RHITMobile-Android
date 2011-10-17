package edu.rosehulman.android.directory.db;

import android.database.Cursor;
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

	/**
	 * Convert the result of a single cell into a String
	 * 
	 * @param cursor An active cursor, with a single column and row
	 * @return the String contained in the only cell
	 */
	protected String getString(Cursor cursor) {
		assert(cursor.getColumnCount() == 1);
		assert(cursor.getCount() == 1);
		cursor.moveToFirst();
		return cursor.getString(0);
	}
	
	protected boolean getBoolean(Cursor cursor, int column) {
		int val = cursor.getInt(column);
		assert(val == 0 || val == 1);
		return val == 1;
	}
	

	protected String columns(String... args) {
		StringBuilder builder = new StringBuilder(" ");
		for (String arg : args) {
			builder.append(arg);
			builder.append(",");
		}
		builder.setCharAt(builder.length() - 1, ' ');
		return builder.toString();
	}
	
	protected String column(String table, String column) {
		return table + "." + column;
	}
	
	protected String tables(String... args) {
		//TODO implement
		return args[0];
	}
	
	protected String table(String name, String alias) {
		return name + " " + alias;
	}
	
}
