package edu.rosehulman.android.directory;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import edu.rosehulman.android.directory.util.TaskQueue;
import edu.rosehulman.android.directory.util.TaskQueue.Task;

public class DataUpdateService extends Service {
	
	public static Intent createIntent(Context context) {
		return new Intent(context, DataUpdateService.class);
	}
	
	private LocalBinder binder = new LocalBinder();

	private NotificationManager notifyManager;
	private UpdateDataTask updateTask;
	
	private static final int NOTIFICATION_ID = 4848;
	
	@Override
	public void onCreate() {
		notifyManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);	
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			//TODO handle method
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		binder.abort();
	}


	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public class LocalBinder extends Binder implements IDataUpdateService {

		@Override
		public void startUpdate() {
			synchronized (DataUpdateService.this) {
				if (updateTask == null) {
					updateTask = new UpdateDataTask();
					updateTask.execute();
				}	
			}
		}
		
		@Override
		public void abort() {
			synchronized (DataUpdateService.this) {
				if (updateTask != null) {
					updateTask.cancel(true);
					updateTask = null;
				}	
			}
		}
		
		@Override
		public void requestTopLocations(AsyncRequest listener) {
			//TODO implement
		}
		
	}
	
	private enum UpdateStatus {
		UPDATE_LOCATIONS,
		UPDATE_SERVICES
	}
	
	private class UpdateDataTask extends AsyncTask<Void, Void, Void> {
		
		private PendingIntent startupIntent;
		private Notification updateNotification;
		
		private UpdateStatus step;
		private int progress;
		
		private int locationProgress;
		private int locationCount;
		
		@Override
		protected void onPreExecute() {
			createNotification();
		}
		
		private void sleep(int ms) {
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) { }
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			
			TaskQueue queue = new TaskQueue();
			queue.addTask(new TopLocationsTask());
			queue.addTask(new CampusServicesTask());
			
			while (!queue.isEmpty()) {
				queue.runTask();
			}
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... args) {
			updateStatus();
		}
		
		@Override
		protected void onPostExecute(Void result) {
			cancelNotification();
		}
		
		@Override
		protected void onCancelled() {
			cancelNotification();
		}
		
		private void updateStatus() {
			switch (step) {
			case UPDATE_LOCATIONS:
				if (progress < 0) {
					updateNotification("Updating locations...");
				} else {
					updateNotification(String.format("Updating locations (%d/%d)...", progress+1, locationCount));
				}
				break;
			case UPDATE_SERVICES:
				updateNotification("Updating campus services...");
				break;
			}
		}
		
		private void createNotification() {
	        Intent appIntent = StartupActivity.createIntent(DataUpdateService.this);
	        startupIntent = PendingIntent.getActivity(DataUpdateService.this, 0, appIntent, 0);
	        appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        appIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
	 
	        String message = "Updating remote data...";
	 
	        updateNotification = new Notification(R.drawable.icon, message, System.currentTimeMillis());
	        updateNotification.when = System.currentTimeMillis();
	        
	    }
		
		private void updateNotification(String message) {
	        String title = getResources().getString(R.string.app_name);
	        updateNotification.setLatestEventInfo(DataUpdateService.this, title, message, startupIntent);
	        notifyManager.notify(NOTIFICATION_ID, updateNotification);
		}
		
		private void cancelNotification() {
			notifyManager.cancel(NOTIFICATION_ID);
		}
		
		private void updateLocationProgress() {
			step = UpdateStatus.UPDATE_LOCATIONS;
			progress = locationProgress;
			publishProgress();
		}
		
		private void updateServicesProgress() {
			step = UpdateStatus.UPDATE_SERVICES;
			publishProgress();
		}
		
		class TopLocationsTask implements Task {
			
			@Override
			public void run(TaskQueue queue) {
				locationProgress = -1;
				updateLocationProgress();
				
				sleep(1000);
				
				locationCount = 100;
				for (int i = 0; i < locationCount; i++) {
					queue.addTask(new InnerLocationTask(i));
				}
			}
			
			@Override
			public boolean equals(Object o) {
				return o instanceof TopLocationsTask;
			}
		}
		
		class InnerLocationTask implements Task {
			
			private long id;
			
			public InnerLocationTask(long id) {
				this.id = id;
			}

			@Override
			public void run(TaskQueue queue) {
				locationProgress++;
				updateLocationProgress();
				
				sleep(50);
			}
			
			@Override
			public boolean equals(Object o) {
				if (!(o instanceof InnerLocationTask))
					return false;
				
				return ((InnerLocationTask)o).id == id;
			}
		}
		
		class CampusServicesTask implements Task {

			@Override
			public void run(TaskQueue queue) {
				updateServicesProgress();
				sleep(5000);
			}
			
			@Override
			public boolean equals(Object o) {
				return o instanceof CampusServicesTask;
			}
		}
		
		
	}

}
