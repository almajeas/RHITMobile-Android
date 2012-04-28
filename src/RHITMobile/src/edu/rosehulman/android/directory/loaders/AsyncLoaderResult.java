package edu.rosehulman.android.directory.loaders;

public class AsyncLoaderResult<T> {
	
	private boolean mSuccess;
	
	private AsyncLoaderException mError;
	private T mResult;
	
	public static <T> AsyncLoaderResult<T> createResult(T result) {
		return new AsyncLoaderResult<T>(result);
	}
	
	public static <T> AsyncLoaderResult<T> createError(AsyncLoaderException error) {
		AsyncLoaderResult<T> res = new AsyncLoaderResult<T>();
		res.mError = error;
		return res;
	}
	
	private AsyncLoaderResult() {
		mSuccess = false;
	}
	
	private AsyncLoaderResult(T result) {
		mSuccess = true;
		mResult = result;
	}
	
	public T getResult() throws AsyncLoaderException {
		if (mSuccess) {
			return mResult;
		} else {
			throw mError;
		}
	}

}
