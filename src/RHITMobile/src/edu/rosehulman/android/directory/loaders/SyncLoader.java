package edu.rosehulman.android.directory.loaders;

import android.content.Context;
import android.support.v4.content.Loader;

public abstract class SyncLoader<T> extends Loader<LoaderResult<T>> {

	private boolean mLoading;
	private boolean mLoaded;
	
	private LoaderResult<T> mData;
	
	public SyncLoader(Context context) {
		super(context);
	}
	
	@Override
	protected void onStartLoading() {
		if (mLoaded) {
			deliverResult(mData);
			
		} else if (!mLoading) {
			forceLoad();
		}
	}

	@Override
	protected void onForceLoad() {
		mLoading = true;
		
		try {
			loadData();
		} catch (LoaderException e) {
			handleError(e);
		}
	}

	@Override
	protected void onStopLoading() {
		if (!mLoading)
			return;
		
		mLoading = false;
	}
	
	@Override
	protected void onReset() {
		onStopLoading();
		
		mData = null;
		mLoaded = false;
	}
	
	protected void handleResult(T data) {
		LoaderResult<T> res = LoaderResult.createResult(data); 
		deliverResult(res);
	}
	
	protected void handleError(LoaderException ex) {
		LoaderResult<T> res = LoaderResult.createError(ex); 
		deliverResult(res);
	}
	
	@Override
	public void deliverResult(LoaderResult<T> data) {
		mLoaded = true;
		mLoading = false;
		mData = data;
		super.deliverResult(data);
	}
	
	protected abstract void loadData() throws LoaderException;
}
