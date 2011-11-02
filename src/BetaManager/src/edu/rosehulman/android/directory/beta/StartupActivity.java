package edu.rosehulman.android.directory.beta;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
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
        
        //TODO fix
        //new CheckVersion().execute();
    }
    
    private class CheckVersion extends AsyncTask<Void, Void, LatestBuilds> {
    	
    	private ProgressDialog status;
    	
    	@Override
    	protected void onPreExecute() {
    		String title = "Checking for updates";
    		String message = null;
        	status = ProgressDialog.show(StartupActivity.this, title, message, false);
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
    		status.dismiss();
    	
    		if (result != null) {
    			StartupActivity.this.getExternalFilesDir(null);
    			String url = result.rolling.getMobileDownloadUrl();
    			Log.d("BetaManager", "Download URL: " + url);
    			Intent promptInstall = new Intent(Intent.ACTION_VIEW)
    		    .setData(Uri.parse(url))
    		    .setType("application/vnd.android.package-archive");
    			startActivity(promptInstall); 
    			//Toast.makeText(StartupActivity.this, result.viewURL, Toast.LENGTH_SHORT).show();
    		} else {
    			Toast.makeText(StartupActivity.this, "No updates", Toast.LENGTH_SHORT).show();
    		}
    	}
    	
    }
}
