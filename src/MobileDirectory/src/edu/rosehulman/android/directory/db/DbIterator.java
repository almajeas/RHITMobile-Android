package edu.rosehulman.android.directory.db;

import android.database.Cursor;

public abstract class DbIterator<T> {

	private Cursor cursor;
	
	public DbIterator(Cursor cursor) {
		this.cursor = cursor;
	}
	
	protected abstract T convertRow(Cursor cursor);
	
	public boolean hasNext() {
		return cursor.getPosition() < cursor.getCount() - 1;
	}
	
	public T getNext() {
		cursor.moveToNext();
		return convertRow(cursor);
	}
	
}
