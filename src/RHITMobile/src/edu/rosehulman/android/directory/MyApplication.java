package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.IBinder;
import android.util.Log;
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
	
	private List<UpdateServiceListener> updateServiceListeners = new ArrayList<UpdateServiceListener>();
	private IDataUpdateService updateService;
	
	public void getDataUpdateService(UpdateServiceListener listener) {
		synchronized(updateServiceListeners) {
			updateServiceListeners.add(listener);
			
			if (updateServiceListeners.size() > 0 && updateService != null) {
				listener.onServiceAcquired(updateService);
			} else {
				boolean res = bindService(DataUpdateService.createIntent(this), new ServiceConnection() {
					@Override
					public void onServiceConnected(ComponentName name, IBinder service) {
						synchronized(updateServiceListeners) {
							updateService = (IDataUpdateService)service;
							for (UpdateServiceListener listener : updateServiceListeners) {
								listener.onServiceAcquired(updateService);
							}
						}
					}
					@Override
					public void onServiceDisconnected(ComponentName name) {
						synchronized(updateServiceListeners) {
							for (UpdateServiceListener listener : updateServiceListeners) {
								listener.onServiceLost();
							}
							updateService = null;
						}
					}
				}, BIND_AUTO_CREATE);
				
				if (!res) {
					Log.e(C.TAG, "Failed to bind data update service");
				}
			}
		}
	}
	
	public interface UpdateServiceListener {
		public void onServiceAcquired(IDataUpdateService service);
		public void onServiceLost();
	}

}
