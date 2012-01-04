package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * Manages a connection to a service in a convenient way for activities to use.
 * 
 * Services cannot be immediately obtained when the activity is constructed, but it is
 * convenient to be able to make calls when the activity is being setup (even through onResume).
 * This class caches requests for the service and calls them in order when the service connection
 * is made.
 *
 * @param <T> The interface for the service
 */
public class ServiceManager<T> extends ContextWrapper {
	
	/**
	 * Interface for operating on a bound service
	 *
	 * @param <T> The interface for the service
	 */
	public interface ServiceRunnable<T> {
		
		/**
		 * Called when a service connection is made
		 *  
		 * @param service The bound service
		 */
		public void run(T service);
	}
	
	private static class ServiceConn<T> implements ServiceConnection {
		
		public T service;
		private List<ServiceConnection> listeners = new ArrayList<ServiceConnection>();

		@Override
		@SuppressWarnings("unchecked")
		public synchronized void onServiceConnected(ComponentName name, IBinder service) {
			this.service = (T)service;
			
			for (ServiceConnection conn : listeners) {
				conn.onServiceConnected(name, service);
			}
		}

		@Override
		public synchronized void onServiceDisconnected(ComponentName name) {
			Log.e(C.TAG, "Service disconnected: " + name.flattenToString());
			service = null;

			for (ServiceConnection conn : listeners) {
				conn.onServiceDisconnected(name);
			}
		}
		
	}
	
	private static Map<ComponentName, ServiceConn<?>> serviceCache;

	private ServiceConn<T> serviceConn;
	private ServiceConnection conn;
	private List<ServiceRunnable<T>> listeners;
	private boolean cancelled;
	
	static {
		serviceCache = new HashMap<ComponentName, ServiceConn<?>>();
	}

	/**
	 * Creates a new ServiceManager
	 * 
	 * @param context The context to create the service in
	 * @param intent The intent for the service
	 */
	@SuppressWarnings("unchecked")
	public ServiceManager(Context context, Intent intent) {
		super(context);
		listeners = new ArrayList<ServiceRunnable<T>>();
		final ComponentName componentName = intent.getComponent();
		
		conn = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder binder) {
				synchronized (listeners) {
					T service = (T)serviceCache.get(componentName).service;
					for (ServiceRunnable<T> listener : listeners) {
						listener.run(service);
					}
					listeners.clear();
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name) { }
		};
		
		serviceConn = (ServiceConn<T>)serviceCache.get(componentName);
		
		if (serviceConn == null) {
			serviceConn = new ServiceConn<T>();
			
			boolean res = bindService(intent, serviceConn, BIND_AUTO_CREATE);
			
			if (!res) {
				Log.e(C.TAG, "Failed to bind to service");
				conn = null;
			}
			serviceCache.put(componentName, serviceConn);
		}
		
		serviceConn.listeners.add(conn);
	}

	/**
	 * Run a method that requires a service instance.  The runnable will either
	 * be run immediately if the service is available or will be delayed until
	 * the service is available.  If cancel() is called before the service is
	 * acquired, the listener will never be run. This method can be called after
	 * calling cancel, but there is no guarantee that the callback will be run.
	 * 
	 * @param listener The callback for when the service is available. 
	 */
	public void run(ServiceRunnable<T> listener) {
		if (serviceConn.service != null) {
			listener.run(serviceConn.service);
			return;
		}
		
		synchronized (listeners) {
			if (cancelled)
				return;
			listeners.add(listener);
		}
	}
	
	/**
	 * Retrieves the service managed by this instance.
	 * 
	 * NOTE: The instance returned by this may be a service that has disconnected.
	 *       You are better off using the run method.  That is what it is there for. 
	 * 
	 * @return The service instance, if it exists.
	 */
	public T get() {
		return serviceConn.service;
	}
	
	/**
	 * Clears all pending operations and disconnects from the service cache
	 */
	public void cancel() {
		synchronized (listeners) {
			listeners.clear();
			cancelled = true;
		}
		serviceConn.listeners.remove(conn);
	}
	
}
