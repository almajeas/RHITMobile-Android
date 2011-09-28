package edu.rosehulman.android.directory;

import edu.rosehulman.android.directory.db.DatabaseHelper;
import android.app.Application;
import android.database.sqlite.SQLiteOpenHelper;

public class MyApplication extends Application {
	
	public SQLiteOpenHelper dbHelper;
	
	@Override
	public void onCreate() {
		dbHelper = DatabaseHelper.getInstance(getApplicationContext());
	}

}
