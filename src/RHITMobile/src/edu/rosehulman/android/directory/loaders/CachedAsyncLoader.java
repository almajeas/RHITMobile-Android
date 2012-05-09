package edu.rosehulman.android.directory.loaders;

import android.content.Context;
import android.util.Log;
import edu.rosehulman.android.directory.C;
import edu.rosehulman.android.directory.MyApplication;
import edu.rosehulman.android.directory.compat.AsyncLoader;

public abstract class CachedAsyncLoader<D> extends AsyncLoader<LoaderResult<D>> {
	
	private LoaderResult<D> mData;

	public CachedAsyncLoader(Context context) {
		super(context);
	}
	
	@Override
	protected void onStartLoading() {
		if (mData != null && mData.isSuccess()) {
			deliverResult(mData);
		}
		
		if (takeContentChanged() || mData == null || !mData.isSuccess()) {
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
	public void deliverResult(LoaderResult<D> data) {
		if (isReset()) {
			return;
		}
		
		mData = data;
		
		super.deliverResult(data);
	}

	@Override
	public LoaderResult<D> loadInBackground() {
		if (MyApplication.DEBUG) {
			try {
				return LoaderResult.createResult(doInBackground());
				
			} catch (LoaderException ex) {
				return LoaderResult.createError(ex);
				
			} catch (RuntimeException ex) {
				Log.e(C.TAG, "Unhandled exception in loader", ex);
				throw ex;
			}
			
		} else {
			try {
				return LoaderResult.createResult(doInBackground());
				
			} catch (LoaderException ex) {
				return LoaderResult.createError(ex);
			}
		}
	}
	
	protected abstract D doInBackground() throws LoaderException;
	
}
