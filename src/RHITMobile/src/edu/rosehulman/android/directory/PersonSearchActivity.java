package edu.rosehulman.android.directory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import edu.rosehulman.android.directory.R;
import edu.rosehulman.android.directory.fragments.AuthenticatedFragment;
import edu.rosehulman.android.directory.fragments.PersonListFragment;
import edu.rosehulman.android.directory.fragments.AuthenticatedFragment.AuthenticationCallbacks;
import edu.rosehulman.android.directory.fragments.PersonListFragment.PersonListCallbacks;

public class PersonSearchActivity extends SherlockFragmentActivity implements AuthenticationCallbacks, PersonListCallbacks {

	private AuthenticatedFragment mAuth;
	private PersonListFragment mList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.person_search);
		getSupportActionBar().setHomeButtonEnabled(true);

		FragmentManager fragments = getSupportFragmentManager();
        mAuth = (AuthenticatedFragment)fragments.findFragmentByTag("auth");
        if (mAuth == null) {
        	mAuth = new AuthenticatedFragment();
			getSupportFragmentManager().beginTransaction().add(mAuth, "auth").commit();
        }
        
        mList = (PersonListFragment)fragments.findFragmentById(R.id.list_fragment);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.person_search, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle item selection
        switch (item.getItemId()) {
        case R.id.search:
        	onSearchRequested();
        	break;
        case android.R.id.home:
        	startActivity(StartupActivity.createIntent(this, true));
        	break;
        default:
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

	@Override
	public void onRequestAuthToken() {
		mAuth.obtainAuthToken();
	}
	
	@Override
	public void onInvalidateAuthToken(String authToken) {
		mAuth.invalidateAuthToken(authToken);
	}

	@Override
	public void onAuthTokenObtained(String authToken) {
		mList.onAuthTokenObtained(authToken);
	}

	@Override
	public void onAuthTokenCancelled() {
		Toast.makeText(this, getString(R.string.authentication_error), Toast.LENGTH_SHORT).show();
		finish();
	}
}
