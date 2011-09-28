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

	private static final int DATABASE_VERSION = 5;

	private static final String TABLE_MAP_AREAS = "MapAreas";
	private static final String CREATE_TABLE_MAP_AREAS =
			"CREATE TABLE " + TABLE_MAP_AREAS + " " +
				"( _Id INTEGER PRIMARY KEY" +
				", Name TEXT NOT NULL" +
				", Description TEXT" +
				", LabelOnHybrid INTEGER NOT NULL" +
				", MinZoomLevel INTEGER NOT NULL" +
				", CenterLat REAL NOT NULL" + 
				", CenterLon REAL NOT NULL" +
				");";
	
	private static final String TABLE_MAP_AREA_CORNERS = "MapAreaCorners";
	private static final String CREATE_TABLE_MAP_AREA_CORNERS =
			"CREATE TABLE " + TABLE_MAP_AREA_CORNERS + " " +
				"( _Id INTEGER PRIMARY KEY AUTOINCREMENT" +
				", MapArea INTEGER REFERENCES MapAreas(_Id)" +
				", Item INTEGER NOT NULL" +
				", Lat REAL NOT NULL" + 
				", Lon REAL NOT NULL" +
				");";

	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_MAP_AREA_CORNERS);
		db.execSQL(CREATE_TABLE_MAP_AREAS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAP_AREA_CORNERS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAP_AREAS);
		onCreate(db);
	}

}
