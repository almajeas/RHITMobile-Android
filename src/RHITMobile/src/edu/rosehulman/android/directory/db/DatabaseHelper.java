package edu.rosehulman.android.directory.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Manages the databases on the system.
 * 
 * Knows how to create new and upgrade existing databases.  Also, this class
 * automatically switches from the real database to a mock database if the 
 * system is being unit tested
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static DatabaseHelper instance = null;
	private static final String DATABASE_NAME = "mobile_directory.db";
	private static final String MOCK_DATABASE_NAME = "mock_mobile_directory.db";
	
	/**
	 * Create the single, static instance of DatabaseHelper
	 * 
	 * @param context The context to use
	 * @param mock True if a mock database should be used
	 * @return The newly created DatabaseHelper
	 */
	public static DatabaseHelper createInstance(Context context, boolean mock) {
		if (mock) {
			instance = new DatabaseHelper(context.getApplicationContext(), MOCK_DATABASE_NAME);
		} else {
			instance = new DatabaseHelper(context.getApplicationContext(), DATABASE_NAME);
		}
		return instance;
	}
	
	/**
	 * Retrieves the instance of DatabaseHelper
	 * 
	 * @return The single instance of DatabaseHelper
	 */
	public static DatabaseHelper getInstance() {
		assert(instance != null);
		return instance;
	}

	private static final int DATABASE_VERSION = 15;

	private static final String TABLE_LOCATIONS = "Locations";
	private static final String CREATE_TABLE_LOCATIONS =
			"CREATE TABLE " + TABLE_LOCATIONS + " " +
				"( _Id INTEGER PRIMARY KEY" +
				", MapAreaId INTEGER" +
				", ParentId INTEGER" +
				", Name TEXT NOT NULL" +
				", Description TEXT" +
				", CenterLat INTEGER NOT NULL" + 
				", CenterLon INTEGER NOT NULL" +
				", Type INTEGER NOT NULL" +
				", ChildrenLoaded INTEGER" +
				");";
	
	private static final String TABLE_MAP_AREA_DATA = "MapAreaData";
	private static final String CREATE_TABLE_MAP_AREA_DATA =
			"CREATE TABLE " + TABLE_MAP_AREA_DATA + " " +
				"( _Id INTEGER PRIMARY KEY AUTOINCREMENT" +
				", LabelOnHybrid INTEGER NOT NULL" +
				", MinZoomLevel INTEGER NOT NULL" +
				");";
	
	private static final String TABLE_MAP_AREA_CORNERS = "MapAreaCorners";
	private static final String CREATE_TABLE_MAP_AREA_CORNERS =
			"CREATE TABLE " + TABLE_MAP_AREA_CORNERS + " " +
				"( _Id INTEGER PRIMARY KEY AUTOINCREMENT" +
				", MapAreaId INTEGER REFERENCES MapAreaData(_Id)" +
				", Item INTEGER NOT NULL" +
				", Lat INTEGER NOT NULL" + 
				", Lon INTEGER NOT NULL" +
				");";
	
	private static final String TABLE_HYPERLINKS = "Hyperlinks";
	private static final String CREATE_TABLE_HYPERLINKS =
			"CREATE TABLE " + TABLE_HYPERLINKS + " " +
				"( _Id INTEGER PRIMARY KEY AUTOINCREMENT" +
				", LocationId INTEGER REFERENCES Locations(_Id)" +
				", Name TEXT NOT NULL" +
				", Url TEXT NOT NULL" +
				");";
	
	private static final String TABLE_ALTERNATE_NAMES = "AltNames";
	private static final String CREATE_TABLE_ALTERNATE_NAMES =
			"CREATE TABLE " + TABLE_ALTERNATE_NAMES + " " +
				"( _Id INTEGER PRIMARY KEY AUTOINCREMENT" +
				", LocationId INTEGER REFERENCES Locations(_Id)" +
				", Name TEXT NOT NULL" +
				");";
	
	private static final String TABLE_VERSIONS = "Versions";
	private static final String CREATE_TABLE_VERSIONS =
			"CREATE TABLE " + TABLE_VERSIONS + " " +
				"( _Id INTEGER PRIMARY KEY" +
				", Version TEXT NOT NULL" +
				");";

	private DatabaseHelper(Context context, String dbName) {
		super(context, dbName, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_MAP_AREA_DATA);
		db.execSQL(CREATE_TABLE_MAP_AREA_CORNERS);
		db.execSQL(CREATE_TABLE_LOCATIONS);
		db.execSQL(CREATE_TABLE_ALTERNATE_NAMES);
		db.execSQL(CREATE_TABLE_HYPERLINKS);
		db.execSQL(CREATE_TABLE_VERSIONS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_VERSIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALTERNATE_NAMES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_HYPERLINKS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAP_AREA_CORNERS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAP_AREA_DATA);
		
		onCreate(db);
	}

}
