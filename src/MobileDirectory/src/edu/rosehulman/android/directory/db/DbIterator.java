package edu.rosehulman.android.directory.db;

import android.database.Cursor;

/**
 * An abstract iterator over database model objects
 *
 * @param <T> The model object represented by this iterator
 */
public abstract class DbIterator<T> {

	private Cursor cursor;
	
	/**
	 * Create a new DbIterator
	 * 
	 * @param cursor The cursor to draw data from, initialized to the first record
	 */
	public DbIterator(Cursor cursor) {
		this.cursor = cursor;
	}
	
	/**
	 * Convert the selected row in the cursor to type T
	 * 
	 * @param cursor The cursor, moved to the appropriate row
	 * 
	 * @return An object that represents the data in the row
	 */
	protected abstract T convertRow(Cursor cursor);
	
	/**
	 * Determine if another object is contained in the cursor
	 * 
	 * @return True if another object is available
	 */
	public boolean hasNext() {
		return cursor.getPosition() < cursor.getCount() - 1;
	}
	
	/**
	 * Gets the next object in the cursor
	 * 
	 * @return the next available object
	 */
	public T getNext() {
		cursor.moveToNext();
		return convertRow(cursor);
	}
	
}
