package edu.rosehulman.android.directory.tasks;

import android.os.AsyncTask;
import edu.rosehulman.android.directory.db.CampusServicesAdapter;
import edu.rosehulman.android.directory.model.Hyperlink;

/**
 * Load a \ref model.Hyperlink
 */
public class LoadCampusServiceHyperlink extends AsyncTask<Long, Void, Hyperlink> {
	
	/**
	 * Listener for when a Hyperlink is loaded
	 */
	public interface OnHyperlinkLoadedListener {
		
		/**
		 * Called when a Hyperlink is loaded
		 * 
		 * @param link The loaded Hyperlink
		 */
		public void onLinkLoaded(Hyperlink link);
	}
	
	private OnHyperlinkLoadedListener listener;
	
	/**
	 * Creates a new LoadCampusServiceHyperlink task
	 * 
	 * @param listener The task to run once the link is successfully loaded
	 */
	public LoadCampusServiceHyperlink(OnHyperlinkLoadedListener listener) {
		this.listener = listener;
	}

	@Override
	protected Hyperlink doInBackground(Long... params) {
		Long id = params[0];
		
		CampusServicesAdapter adapter = new CampusServicesAdapter();
		adapter.open();
		Hyperlink link = adapter.getHyperLink(id);
		adapter.close();
		
		return link;
	}
	
	@Override
	protected void onPostExecute(Hyperlink res) {
		listener.onLinkLoaded(res);
	}

}
