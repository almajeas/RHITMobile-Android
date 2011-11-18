package edu.rosehulman.android.directory.db;

import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import edu.rosehulman.android.directory.MyApplication;

/** Provide generic functionality to all database adapters */
public abstract class TableAdapter {

	private static Map<SQLiteDatabase, Integer> dbCount;

	private SQLiteOpenHelper dbOpenHelper;
	
	static {
		dbCount = new HashMap<SQLiteDatabase, Integer>();
	}

	/** Shared db connection */
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
		ref(db);
	}

	/**
	 * Open a writable version of the database
	 */
	public void open() {
		if (db != null) {
			deref(db);
			db = null;
		}
		db = dbOpenHelper.getWritableDatabase();
		ref(db);

		verifyThread();
	}

	/**
	 * Close an open database connection.
	 * 
	 * TableAdapters created with an existing connection should not close the
	 * connection to the database.
	 */
	public void close() {
		deref(db);
		db = null;
	}
	
	public void startTransaction() {
		db.beginTransaction();
	}
	
	public void commitTransaction() {
		db.setTransactionSuccessful();
	}
	
	public void finishTransaction() {
		db.endTransaction();
	}

	/**
	 * Convert the result of a single cell into a String
	 * 
	 * @param cursor An active cursor, with a single column and row
	 * @return the String contained in the only cell
	 */
	protected String getString(Cursor cursor) {
		assert (cursor.getColumnCount() == 1);
		assert (cursor.getCount() == 1);
		cursor.moveToFirst();
		return cursor.getString(0);
	}

	/**
	 * Retrieve a boolean value from the database
	 * 
	 * @param cursor The cursor to read from, moved to the appropriate row
	 * @param column The index of the column to read
	 * @return True if the value is 1, False if 0
	 */
	protected boolean getBoolean(Cursor cursor, int column) {
		int val = cursor.getInt(column);
		assert (val == 0 || val == 1);
		return val == 1;
	}

	/**
	 * Convert an array of strings to a list of columns
	 * 
	 * @param args
	 *            A list of column names
	 * @return A string separating the column names with commas
	 */
	protected String columns(String... args) {
		StringBuilder builder = new StringBuilder(" ");
		for (String arg : args) {
			builder.append(arg);
			builder.append(",");
		}
		builder.setCharAt(builder.length() - 1, ' ');
		return builder.toString();
	}

	/**
	 * Format a table/column pair
	 * 
	 * @param table
	 *            The table to use
	 * @param column
	 *            The column to use
	 * @return table.column
	 */
	protected String column(String table, String column) {
		return table + "." + column;
	}

	//protected String tables(String... args) {
	//	//TODO implement
	//	return args[0];
	//}

	/**
	 * Format a table name/alias pair
	 * 
	 * @param name
	 *            The table name
	 * @param alias
	 *            The alias of the table
	 * 
	 * @return name alias
	 */
	protected String table(String name, String alias) {
		return name + " " + alias;
	}

	private static void verifyThread() {
		if (!MyApplication.CHECK_UI_THREAD)
			return;

		//make sure we are not the main thread
		StackTraceElement stack[] = Thread.currentThread().getStackTrace();
		for (StackTraceElement frame : stack) {
			if ("main".equals(frame.getMethodName()) &&
				frame.getClassName().contains("NativeStart") &&
				frame.isNativeMethod()) {
				throw new RuntimeException("DB access on UI thread");
			}
		}
	}
	
	private synchronized static void ref(SQLiteDatabase db) {
		int count = 0;
		
		if (dbCount.containsKey(db)) {
			count = dbCount.get(db);
		}
		
		dbCount.put(db, count + 1);
	}
	
	private synchronized static void deref(SQLiteDatabase db) {
		int count = dbCount.get(db);
		
		if (count == 1) {
			dbCount.remove(db);
			db.close();
		} else {
			dbCount.put(db, count - 1);
		}
	}

}
