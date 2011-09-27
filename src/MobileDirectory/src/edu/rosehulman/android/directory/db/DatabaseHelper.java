package edu.rosehulman.android.directory.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static DatabaseHelper instance = null;
	
	public static DatabaseHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DatabaseHelper(context.getApplicationContext());
		}
		return instance;
	}

	private static final String DATABASE_NAME = "mobile_directory.db";

	private static final int DATABASE_VERSION = 1;

	private static final String TABLE_BUILDINGS = "buildings";
	private static final String CREATE_TABLE_BUILDINGS =
			"CREATE TABLE " + TABLE_BUILDINGS + " " +
				"( _id INTEGER PRIMARY KEY AUTOINCREMENT" +
				", name TEXT NOT NULL" +
				", showLabel INTEGER NOT NULL" +
				", description TEXT" +
				", centerLat REAL NOT NULL" + 
				", centerLon REAL NOT NULL" +
				");";

	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_BUILDINGS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUILDINGS);
		onCreate(db);
	}

}
