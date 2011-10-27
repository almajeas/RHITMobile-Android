package edu.rosehulman.android.directory.beta;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import edu.rosehulman.android.directory.beta.model.BuildInfo;
import edu.rosehulman.android.directory.beta.model.LatestBuilds;
import edu.rosehulman.android.directory.beta.service.BetaService;

public class StartupActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup);
        
        findViewById(R.id.btnDismiss).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(Activity.RESULT_OK);
				finish();
			}
        });
        
        new CheckVersion().execute();
    }
    
    private class CheckVersion extends AsyncTask<Void, Void, BuildInfo> {
    	
    	private ProgressDialog status;
    	
    	@Override
    	protected void onPreExecute() {
    		String title = "Checking for updates";
    		String message = "These happen a lot since this is a beta";
        	status = ProgressDialog.show(StartupActivity.this, title, message, false);
    	}
    	
		@Override
		protected BuildInfo doInBackground(Void... params) {
			BetaService service = new BetaService();
			
			LatestBuilds builds;
			try {
				builds = service.getLatestBuilds();
			} catch (Exception e) {
				return null;
			}
			
			return builds.latest;
		}
		
		@Override
    	protected void onPostExecute(BuildInfo result) {
    		status.dismiss();
    		
    		if (result != null) {
    			Toast.makeText(StartupActivity.this, result.viewURL, Toast.LENGTH_SHORT).show();
    		} else {
    			Toast.makeText(StartupActivity.this, "No updates", Toast.LENGTH_SHORT).show();
    		}
    	}
    	
    }
}
