package edu.rosehulman.android.directory;

import java.util.HashSet;
import java.util.Set;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import edu.rosehulman.android.directory.IDataUpdateService.AsyncRequest;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.db.VersionsAdapter;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.model.LocationCollection;
import edu.rosehulman.android.directory.model.VersionResponse;
import edu.rosehulman.android.directory.model.VersionType;
import edu.rosehulman.android.directory.service.MobileDirectoryService;
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
		
		private void performTask(Task task, AsyncRequest listener) {
		
			if (!updateTask.queue.prioritizeTask(task)) {
				if (listener != null) {
					listener.onCompleted();
				}
				return;
			}
			
			synchronized (updateTask.queue) {
				updateTask.waitingListener = listener;
				updateTask.waitingTask = task;
			}

			if (listener == null)
				return;
			
			listener.onQueued(new Runnable() {
				@Override
				public void run() {
					synchronized (updateTask.queue) {
						updateTask.waitingTask = null;
						updateTask.waitingListener = null;
					}
				}
			});
		}
		
		@Override
		public void requestTopLocations(AsyncRequest listener) {
			startUpdate();

			performTask(updateTask.new TopLocationsTask(), listener);
		}
		
		@Override
		public void requestInnerLocation(long id, AsyncRequest listener) {
			startUpdate();
			
			performTask(updateTask.new InnerLocationTask(id), listener);
		}
		
		@Override
		public boolean isUpdating() {
			startUpdate();
			
			return updateTask.getStatus() != AsyncTask.Status.FINISHED;
		}
		
	}
	
	private enum UpdateStatus {
		UPDATE_VERSIONS,
		UPDATE_LOCATIONS,
		UPDATE_SERVICES
	}
	
	private class UpdateDataTask extends AsyncTask<Void, Void, Void> {
		
		private PendingIntent startupIntent;
		private Notification updateNotification;
		
		public UpdateStatus step;
		private int progress;
		
		private int locationProgress;
		private int locationCount;
		
		public TaskQueue queue;

		public Task waitingTask;
		public AsyncRequest waitingListener;
		
		public UpdateDataTask() {
			queue = new TaskQueue();

			queue.addTask(new VersionsTask());
		}
		
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
			
			while (!queue.isEmpty()) {
				queue.runTask();
				
				synchronized (queue) {
					if (waitingTask != null && waitingListener != null &&
							waitingTask.equals(queue.getLatestTask()) &&
							!queue.contains(waitingTask)) {
						final AsyncRequest listener = waitingListener;
						MyApplication.getInstance().post(new Runnable() {
							@Override
							public void run() {
								listener.onCompleted();	
							}
						});
					}
				}
				
				if (isCancelled())
					return null;
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
	        updateNotification.flags = Notification.FLAG_ONGOING_EVENT;
	    }
		
		private void updateNotification(String message) {
	        String title = getResources().getString(R.string.app_name);
	        updateNotification.setLatestEventInfo(DataUpdateService.this, title, message, startupIntent);
	        notifyManager.notify(NOTIFICATION_ID, updateNotification);
		}
		
		private void cancelNotification() {
			notifyManager.cancel(NOTIFICATION_ID);
		}
		
		private void updateVersionsProgress() {
			step = UpdateStatus.UPDATE_VERSIONS;
			publishProgress();
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
		
		class VersionsTask implements Task {

			@Override
			public void run(TaskQueue queue) {
				updateVersionsProgress();
				
				VersionsAdapter versionsAdapter = new VersionsAdapter();
				versionsAdapter.open();
				String locationsVersion = versionsAdapter.getVersion(VersionType.MAP_AREAS);
				String servicesVersion = versionsAdapter.getVersion(VersionType.CAMPUS_SERVICES);
		    	versionsAdapter.close();
				
				MobileDirectoryService service = new MobileDirectoryService();
		    	
				VersionResponse versions = null;
				do {
					
					try {
						versions = service.getVersions();
					} catch (Exception e) {
						Log.e(C.TAG, "Failed to downlaod version information", e);
						sleep(2000);
					}
				} while (versions == null);
				
				if (locationsVersion == null || !locationsVersion.equals(versions.locations)) {
					queue.addTask(new TopLocationsTask());
				} else {
					//Finish loading inner locations
					LocationAdapter buildingAdapter = new LocationAdapter();
			        buildingAdapter.open();
					long[] ids = buildingAdapter.getUnloadedTopLocations();
					locationCount = buildingAdapter.getAllTopLocations().length;
					locationProgress = locationCount - ids.length - 1;
			        buildingAdapter.close();
			        for (long id : ids) {
			        	queue.addTask(new InnerLocationTask(id));
			        }
				}
				if (servicesVersion == null || !servicesVersion.equals(versions.services)) {
					queue.addTask(new CampusServicesTask());
				}
			}

			@Override
			public boolean equals(Object o) {
				return o instanceof VersionsTask;
			}
			
		}
		
		class TopLocationsTask implements Task {
			
			@Override
			public void run(TaskQueue queue) {
				locationProgress = -1;
				updateLocationProgress();
				
				//check for updated map areas
				VersionsAdapter versionsAdapter = new VersionsAdapter();
				versionsAdapter.open();
		        
				MobileDirectoryService service = new MobileDirectoryService();
		    	String version = versionsAdapter.getVersion(VersionType.MAP_AREAS);
		    	versionsAdapter.close();
				
		        LocationCollection collection = null;
		        try {
		        	collection = service.getTopLocationData(version);
				} catch (Exception e) {
					Log.e(C.TAG, "Failed to download new locations", e);
					//wait a bit and try again
					queue.addTask(this);
					sleep(5000);
					return;
				}
				if (isCancelled()) {
					return;
				}
				
				if (collection == null) {
					//data was up to date, make sure we have loaded all of our inner locations
					LocationAdapter buildingAdapter = new LocationAdapter();
			        buildingAdapter.open();
					long[] ids = buildingAdapter.getUnloadedTopLocations();
					locationCount = buildingAdapter.getAllTopLocations().length;
					locationProgress = locationCount - ids.length - 1;
			        buildingAdapter.close();
			        for (long id : ids) {
			        	queue.addTask(new InnerLocationTask(id));
			        }
					return;
				}
				
		        LocationAdapter buildingAdapter = new LocationAdapter();
		        buildingAdapter.open();
		        buildingAdapter.replaceLocations(collection.mapAreas);
		        buildingAdapter.close();
		        
		        versionsAdapter.open();
		        versionsAdapter.setVersion(VersionType.MAP_AREAS, collection.version);
		        versionsAdapter.close();
		        
		        locationCount = collection.mapAreas.length;
		        
				for (int i = 0; i < locationCount; i++) {
					queue.addTask(new InnerLocationTask(collection.mapAreas[i].id));
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
				
				MobileDirectoryService service = new MobileDirectoryService();
		        LocationAdapter buildingAdapter = new LocationAdapter();
		        buildingAdapter.open();

		        try {
		        	Set<Long> topIds = new HashSet<Long>();
			        long[] ids = buildingAdapter.getAllTopLocations();
			        for (long id : ids) {
			        	topIds.add(id);
			        }
		        	
					LocationCollection collection = null;
			        try {
			        	collection = service.getLocationData(id, null);
					} catch (Exception e) {
						Log.e(C.TAG, "Failed to download locations within a parent", e);
						locationProgress--;
						queue.addTask(this);
						return;
					}
					if (isCancelled()) {
						return;
					}
	
			        buildingAdapter.startTransaction();
			        for (Location location : collection.mapAreas) {
			        	if (topIds.contains(location.id))
			        		continue;
			        	
			        	buildingAdapter.addLocation(location);
			        }
			        buildingAdapter.setChildrenLoaded(id, true);
			        
					buildingAdapter.commitTransaction();
		        	buildingAdapter.finishTransaction();
		        	
		        } finally {
		        	buildingAdapter.close();
		        }
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
				sleep(1000);
				
				VersionsAdapter versions = new VersionsAdapter();
				versions.open();
				versions.setVersion(VersionType.CAMPUS_SERVICES, "0");
				versions.close();
			}
			
			@Override
			public boolean equals(Object o) {
				return o instanceof CampusServicesTask;
			}
		}
		
		
	}

}
