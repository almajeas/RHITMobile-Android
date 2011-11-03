package edu.rosehulman.android.directory.beta;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import edu.rosehulman.android.directory.beta.StartupStatusItem.StatusState;
import edu.rosehulman.android.directory.beta.model.LatestBuilds;
import edu.rosehulman.android.directory.beta.service.BetaService;

public class StartupActivity extends Activity {
	
	private Button btnContinue;
	
	private StartupStatusItem stepCheckForUpdates;
	private StartupStatusItem stepPromptForUpdate;
	
	private State currentState = State.CHECKING_FOR_UPDATES;
	
	private enum State {
		CHECKING_FOR_UPDATES,
		NO_UPDATES,
		UPDATES_AVAILABLE,
		
		DONE_UPDATING
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup);        
        
        btnContinue = (Button)findViewById(R.id.btnContinue);
        
        stepCheckForUpdates = (StartupStatusItem)findViewById(R.id.stepCheckForUpdates);
        stepPromptForUpdate = (StartupStatusItem)findViewById(R.id.stepPromptForUpdate);
        
        stepCheckForUpdates.setMessage("Checking for updates");
        
        stepPromptForUpdate.setMessage("Update required");
        stepPromptForUpdate.setState(StatusState.ACTION_REQUIRED);

        
        
        findViewById(R.id.btnExit).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnExit_clicked();
			}
		});
        
        btnContinue.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnContinue_clicked();
			}
		});
        
        updateProgress();
        
        new CheckVersion().execute();
    }
    
    private void btnExit_clicked() {
    	setResult(Activity.RESULT_CANCELED);
		finish();
    }
    
    private void btnContinue_clicked() {
    	if (currentState == State.UPDATES_AVAILABLE) {
	    	//TODO implement
    		
    		btnContinue.setEnabled(false);
    	} else {
    		setResult(Activity.RESULT_OK);
			finish();
    	}
    }
    
    private void updateProgress() {
    	switch (currentState) {
    	case NO_UPDATES:
    		btnContinue.setEnabled(true);
    		stepCheckForUpdates.setState(StatusState.SUCCESS);
    	}
    }
    
    private class CheckVersion extends AsyncTask<Void, Void, LatestBuilds> {
    	
    	//private ProgressDialog status;
    	
    	@Override
    	protected void onPreExecute() {
    		//String title = "Checking for updates";
    		//String message = null;
        	//status = ProgressDialog.show(StartupActivity.this, title, message, false);
    	}
    	
		@Override
		protected LatestBuilds doInBackground(Void... params) {
			BetaService service = new BetaService();
			
			LatestBuilds builds;
			try {
				builds = service.getLatestBuilds();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
			return builds;
		}
		
		@Override
    	protected void onPostExecute(LatestBuilds result) {
    		//status.dismiss();
    	
			//FIXME check actual version numbers
    		if (result != null) {
    			//StartupActivity.this.getExternalFilesDir(null);
    			String url = result.rolling.getMobileDownloadUrl();
    			Log.d("BetaManager", "Download URL: " + url);
    			//Intent promptInstall = new Intent(Intent.ACTION_VIEW)
    		    //.setData(Uri.parse(url))
    		    //.setType("application/vnd.android.package-archive");
    			//startActivity(promptInstall); 
    			//Toast.makeText(StartupActivity.this, result.viewURL, Toast.LENGTH_SHORT).show();
    			currentState = State.NO_UPDATES;
    		} else {
    			currentState = State.NO_UPDATES;
    			Toast.makeText(StartupActivity.this, "No updates", Toast.LENGTH_SHORT).show();
    		}
    		updateProgress();
    	}
    	
    }
}
