package edu.rosehulman.android.directory.loaders;

import android.content.Context;
import android.util.Log;
import edu.rosehulman.android.directory.C;
import edu.rosehulman.android.directory.MyApplication;

public abstract class CachedAsyncLoader<D> extends AsyncLoader<AsyncLoaderResult<D>> {
	
	private AsyncLoaderResult<D> mData;

	public CachedAsyncLoader(Context context) {
		super(context);
	}
	
	@Override
	protected void onStartLoading() {
		if (mData == null || shouldUpdate()) {
			forceLoad();
			
		} else {
			deliverResult(mData);	
		}
	}

	@Override
	public AsyncLoaderResult<D> loadInBackground() {
		if (MyApplication.DEBUG) {
			try {
				return AsyncLoaderResult.createResult(doInBackground());
				
			} catch (AsyncLoaderException ex) {
				return AsyncLoaderResult.createError(ex);
				
			} catch (RuntimeException ex) {
				Log.e(C.TAG, "Unhandled exception in loader", ex);
				throw ex;
			}
			
		} else {
			try {
				return AsyncLoaderResult.createResult(doInBackground());
				
			} catch (AsyncLoaderException ex) {
				return AsyncLoaderResult.createError(ex);
			}
		}
	}
	
	/**
	 * Implementations can override this method to specify
	 * whether or not the loader should use the cached result
	 * or recompute the data
	 *  
	 * @return True if the AsyncTask should be run
	 */
	protected boolean shouldUpdate() {
		return true;
	}

	protected abstract D doInBackground() throws AsyncLoaderException;

	@Override
	public void onCanceled(AsyncLoaderResult<D> data) {
		//override to dispose data
	}

	@Override
	public void deliverResult(AsyncLoaderResult<D> data) {
		mData = data;
		
		if (isStarted()) {
			super.deliverResult(data);
		}
	}
	
	@Override
	protected void onReset() {
		super.onReset();
		
		onStopLoading();
		
		if (mData != null) {
			mData = null;
		}
	}
	
}
