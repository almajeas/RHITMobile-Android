package edu.rosehulman.android.directory.loaders;

public class LoaderResult<T> {
	
	private boolean mSuccess;
	
	private LoaderException mError;
	private T mResult;
	
	public static <T> LoaderResult<T> createResult(T result) {
		return new LoaderResult<T>(result);
	}
	
	public static <T> LoaderResult<T> createError(LoaderException error) {
		LoaderResult<T> res = new LoaderResult<T>();
		res.mError = error;
		return res;
	}
	
	private LoaderResult() {
		mSuccess = false;
	}
	
	private LoaderResult(T result) {
		mSuccess = true;
		mResult = result;
	}
	
	public boolean isSuccess() {
		return mSuccess;
	}
	
	public T getResult() throws LoaderException {
		if (mSuccess) {
			return mResult;
		} else {
			throw mError;
		}
	}

}
