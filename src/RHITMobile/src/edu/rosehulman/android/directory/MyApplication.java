package edu.rosehulman.android.directory;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import edu.rosehulman.android.directory.db.DatabaseHelper;
import edu.rosehulman.android.directory.service.MobileDirectoryService;
import edu.rosehulman.android.directory.service.MockClientFactory;
import edu.rosehulman.android.directory.service.MockJsonClient;
import edu.rosehulman.android.directory.service.WebClientFactory;

/**
 * Initialize static application data
 */
public class MyApplication extends Application {
	
	/**
	 * Should we check for intensive work on the UI thread?
	 */
	public static boolean CHECK_UI_THREAD = false;

	/**
	 * The name of the global application preferences file
	 */
	public static final String PREFS_APP = "prefs";
	
	/**
	 * The global database helper
	 */
	public SQLiteOpenHelper dbHelper;
	
	/**
	 * The global BetaManagerManager
	 */
	public BetaManagerManager betaManagerManager;
	
	private static MyApplication instance;
	
	private Handler handler = new Handler();
	
	public static final boolean DEBUG = false;
	
	private static final boolean PURGE_DB = false;
	
	/**
	 * Get the globally accessible application instance
	 * 
	 * @return The one instance of MyApplication
	 */
	public static MyApplication getInstance() {
		return instance;
	}
	
	/**
	 * Purges the database
	 */
	public void purgeDb() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		dbHelper.onUpgrade(db, 0, 0);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}
	
	@Override
	public void onCreate() {
		instance = this;
		
		Context context = getApplicationContext();
		BetaManagerManager beta = new BetaManagerManager(context);
		boolean isMocking = beta.isMocking();

		dbHelper = DatabaseHelper.createInstance(context, isMocking);
		if (isMocking) {
			MobileDirectoryService.setClientFactory(new MockClientFactory());
			MockJsonClient.setContext(context);
		} else {
			MobileDirectoryService.setClientFactory(new WebClientFactory());
		}
		
		betaManagerManager = new BetaManagerManager(context);
		
		instance = this;
		
		//start with a fresh database every run
		if (PURGE_DB) {
			purgeDb();
		}
		
	}
	
	public void post(Runnable runnable) {
		handler.post(runnable);
	}
	
	public SharedPreferences getAppPreferences() {
		return getSharedPreferences(PREFS_APP, MODE_PRIVATE);
	}
	
	public interface UpdateServiceListener {
		public void onServiceAcquired(IDataUpdateService service);
		public void onServiceLost();
	}
	
	public interface ServiceRunnable<T> {
		public void onServiceAcquired(T service);
	}
	
}
