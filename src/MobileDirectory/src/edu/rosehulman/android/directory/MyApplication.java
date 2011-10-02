package edu.rosehulman.android.directory;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import edu.rosehulman.android.directory.db.DatabaseHelper;
import edu.rosehulman.android.directory.service.MobileDirectoryService;
import edu.rosehulman.android.directory.service.MockClientFactory;
import edu.rosehulman.android.directory.service.WebClientFactory;

/**
 * Initialize static application data
 */
public class MyApplication extends Application {
	
	public SQLiteOpenHelper dbHelper;
	
	public BetaManagerManager betaManagerManager;
	
	private static MyApplication instance;
	
	public static MyApplication getInstance() {
		return instance;
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
		} else {
			MobileDirectoryService.setClientFactory(new WebClientFactory());
		}
		
		betaManagerManager = new BetaManagerManager(context);
		
		instance = this;
	}

}
