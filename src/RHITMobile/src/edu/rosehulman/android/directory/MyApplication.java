package edu.rosehulman.android.directory;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
	 * The global database helper
	 */
	public SQLiteOpenHelper dbHelper;
	
	/**
	 * The global BetaManagerManager
	 */
	public BetaManagerManager betaManagerManager;
	
	private static MyApplication instance;
	
	private static final boolean PURGE_DB = true;
	
	/**
	 * Get the globally accessible application instance
	 * 
	 * @return The one instance of MyApplication
	 */
	public static MyApplication getInstance() {
		return instance;
	}
	
	private void purgeDb() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		dbHelper.onUpgrade(db, 0, 0);
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
	
	public interface UpdateServiceListener {
		public void onServiceAcquired(IDataUpdateService service);
		public void onServiceLost();
	}
	
	public interface ServiceRunnable<T> {
		public void onServiceAcquired(T service);
	}
	
}
