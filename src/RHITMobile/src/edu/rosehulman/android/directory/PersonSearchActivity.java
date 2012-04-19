package edu.rosehulman.android.directory;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PersonSearchActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.person_search);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		getSupportFragmentManager().beginTransaction().add(new AuthenticatedFragment(), "auth").commit();
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
}
