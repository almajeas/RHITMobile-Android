package edu.rosehulman.android.directory;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.actionbarsherlock.view.ActionProvider;

import edu.rosehulman.android.directory.model.TermCode;

/**
 * Provides a term code spinner for menu items
 */
public class TermCodeProvider extends ActionProvider {
	
	/**
	 * Callback interface to inform the creating activity when the term is set
	 */
	public interface OnTermSetListener {
		
		/**
		 * Called when a term is set.  This will be called when the spinner
		 * is first created with the initially selected term.
		 * 
		 * @param newTerm The term that has been set
		 */
		public void onTermSet(TermCode newTerm);
	}
	
	private Context mContext;
	
	private TermCode[] mTerms;

	/**
	 * Creates a new instance
	 * 
	 * @param context The creating Activity.  Must implement OnTermSetListener.
	 */
	public TermCodeProvider(Context context) {
		super(context);
		mContext = context;
		mTerms = TermCodes.generateTerms();
	}

	@Override
	public View onCreateActionView() {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		Spinner terms = (Spinner)inflater.inflate(R.layout.schedule_term_selector, null);
		
		ArrayAdapter<TermCode> termAdapter = new ArrayAdapter<TermCode>(mContext, R.layout.sherlock_spinner_item_light_dark, mTerms);
        termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        terms.setAdapter(termAdapter);
        terms.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				try {
					OnTermSetListener listener = (OnTermSetListener)mContext;
					listener.onTermSet(mTerms[position]);
				} catch (ClassCastException ex) {
					Log.e(C.TAG, "Activity should implement OnTermChangedListener", ex);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {}
		});
		
		return terms;
	}	
}