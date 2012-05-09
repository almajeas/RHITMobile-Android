package edu.rosehulman.android.directory.tasks;

import android.os.AsyncTask;
import edu.rosehulman.android.directory.db.TourTagsAdapter;
import edu.rosehulman.android.directory.model.TourTag;

/**
 * Load a \ref model.TourTag
 */
public class LoadTourTag extends AsyncTask<Long, Void, Void> {
	
	/**
	 * Listener for when a TourTag is loaded
	 */
	public interface OnTourTagLoadedListener {
		
		/**
		 * Called when a TourTag is loaded
		 * 
		 * @param tag The loaded TourTag
		 * @param path The path to the tag
		 */
		public void onTagLoaded(TourTag tag, String path);
	}
	
	private OnTourTagLoadedListener listener;
	
	private TourTag tag;
	private String path;
	
	/**
	 * Creates a new LoadTourTag task
	 * 
	 * @param listener The task to run once the link is successfully loaded
	 */
	public LoadTourTag(OnTourTagLoadedListener listener) {
		this.listener = listener;
	}

	@Override
	protected Void doInBackground(Long... params) {
		long id = params[0];
		
		TourTagsAdapter adapter = new TourTagsAdapter();
		adapter.open();
		tag = adapter.getTag(id);
		path = adapter.getTagPath(id);
		adapter.close();
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		listener.onTagLoaded(tag, path);
	}

}
