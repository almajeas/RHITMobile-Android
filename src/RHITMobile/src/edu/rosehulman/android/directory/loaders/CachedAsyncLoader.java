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
		if (mData != null) {
			deliverResult(mData);
		}
		
		if (takeContentChanged() || mData == null) {
			forceLoad();
		}
	}
	
    @Override
    protected void onStopLoading() {
    	cancelLoad(true);
    }
    
	@Override
	protected void onReset() {
		super.onReset();
		
		onStopLoading();
		
		mData = null;
	}
	
	@Override
	public void deliverResult(AsyncLoaderResult<D> data) {
		if (isReset()) {
			return;
		}
		
		mData = data;
		
		super.deliverResult(data);
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
	
	protected abstract D doInBackground() throws AsyncLoaderException;
	
}
