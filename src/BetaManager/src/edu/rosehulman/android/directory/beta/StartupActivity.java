package edu.rosehulman.android.directory.beta;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import edu.rosehulman.android.directory.beta.StartupStatusItem.StatusState;
import edu.rosehulman.android.directory.beta.model.BuildInfo;
import edu.rosehulman.android.directory.beta.model.LatestBuilds;
import edu.rosehulman.android.directory.beta.service.BetaService;

public class StartupActivity extends Activity {
	
	private Button btnContinue;
	
	private StartupStatusItem stepCheckForUpdates;
	private StartupStatusItem stepPromptForUpdate;
	private StartupStatusItem stepDownloading;
	private StartupStatusItem stepUpdatingMobileDirectory;
	private StartupStatusItem stepUpdatingBetaManager;
	
	private State currentState = State.CHECKING_FOR_UPDATES;
	
	private BuildInfo latestBuild;
	
	private enum State {
		CHECKING_FOR_UPDATES,
		NO_UPDATES,
		UPDATES_AVAILABLE,
		
		DOWNLOADING_UPDATES,
		INSTALLING_UPDATE1,
		INSTALLING_UPDATE2,
		
		DONE_UPDATING,
		
		DOWNLOADING_FAILED
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup);        
        
        btnContinue = (Button)findViewById(R.id.btnContinue);
        
        stepCheckForUpdates = (StartupStatusItem)findViewById(R.id.stepCheckForUpdates);
        stepPromptForUpdate = (StartupStatusItem)findViewById(R.id.stepPromptForUpdate);
        stepDownloading = (StartupStatusItem)findViewById(R.id.stepDownloading);
        stepUpdatingMobileDirectory = (StartupStatusItem)findViewById(R.id.stepUpdatingMobileDirectory);
        stepUpdatingBetaManager = (StartupStatusItem)findViewById(R.id.stepUpdatingBetaManager);
        
        stepCheckForUpdates.setMessage("Checking for updates");
        
        stepPromptForUpdate.setMessage("Update required");
        stepPromptForUpdate.setState(StatusState.ACTION_REQUIRED);
        
        stepDownloading.setMessage("Downloading updates");
        
        stepUpdatingMobileDirectory.setMessage("Updating (step 1)");

        stepUpdatingBetaManager.setMessage("Updating (step 2)");
        
        
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
    	switch (currentState) {
    	case UPDATES_AVAILABLE:
    	case DOWNLOADING_FAILED:
    		new UpdateApplication().execute();
    		btnContinue.setEnabled(false);
    		break;
    		
    	default:
    		setResult(Activity.RESULT_OK);
			finish();
			break;
    	}
    }
    
    private void updateProgress() {
    	switch (currentState) {
    	case NO_UPDATES:
    		btnContinue.setEnabled(true);
    		stepCheckForUpdates.setState(StatusState.SUCCESS);
    		break;
    		
    	case UPDATES_AVAILABLE:
    		btnContinue.setText("Update");
    		btnContinue.setEnabled(true);
    		stepCheckForUpdates.setState(StatusState.SUCCESS);
    		stepPromptForUpdate.setVisibility(View.VISIBLE);
    		stepPromptForUpdate.setState(StatusState.ACTION_REQUIRED);
    		break;

    	case DOWNLOADING_UPDATES:
    		stepPromptForUpdate.setState(StatusState.SUCCESS);
    		stepDownloading.setState(StatusState.IN_PROGRESS);
    		stepDownloading.setVisibility(View.VISIBLE);
    		break;
    		
    	case DOWNLOADING_FAILED:
    		stepDownloading.setState(StatusState.ERROR);
    		btnContinue.setText("Retry");
    		btnContinue.setEnabled(true);
    		break;
    		
    	case INSTALLING_UPDATE1:
    		stepDownloading.setState(StatusState.SUCCESS);
    		stepUpdatingMobileDirectory.setVisibility(View.VISIBLE);
    		break;
    		
    	case INSTALLING_UPDATE2:
    		stepUpdatingMobileDirectory.setState(StatusState.SUCCESS);
    		stepUpdatingBetaManager.setVisibility(View.VISIBLE);
    		break;
    		
    	case DONE_UPDATING:
    		//called after the application is restarted, start over with state info
    		stepCheckForUpdates.setState(StatusState.SUCCESS);
    		stepPromptForUpdate.setVisibility(View.VISIBLE);
    		stepPromptForUpdate.setState(StatusState.SUCCESS);
    		stepDownloading.setVisibility(View.VISIBLE);
    		stepDownloading.setState(StatusState.SUCCESS);
    		stepUpdatingMobileDirectory.setVisibility(View.VISIBLE);
    		stepUpdatingMobileDirectory.setState(StatusState.SUCCESS);
    		stepUpdatingBetaManager.setVisibility(View.VISIBLE);
    		stepUpdatingBetaManager.setState(StatusState.SUCCESS);
    		break;
    	}
    }
    
    private class UpdateApplication extends AsyncTask<Void, State, Boolean>
    {
    	
    	private File betaPath;
    	private File mobilePath;
    	
    	private void sleep(long time) {
    		try {
				Thread.sleep(time);
			} catch (InterruptedException e)
			{}
    	}
    	
    	private void downloadFile(String url, File path) throws Exception {
    		FileOutputStream fout = new FileOutputStream(path);
    		HttpClient client = new DefaultHttpClient();
    		HttpResponse response = client.execute(new HttpGet(url));
    		response.getEntity().writeTo(fout);
    	}
    	
    	private boolean downloadUpdates() {
			String storageDirectory = getExternalFilesDir(null) + "/updates/";
			new File(storageDirectory).mkdirs();
			
			betaPath = new File(storageDirectory, "BetaManager.apk");
			mobilePath = new File(storageDirectory, "RHITMobile.apk");
			
    		try {
        		downloadFile(latestBuild.getBetaManagerDownloadUrl(), betaPath);
        		downloadFile(latestBuild.getMobileDownloadUrl(), mobilePath);	
    		} catch (Exception ex) {
    			Log.e("BetaManager", "Failed to download a file", ex);
    			return false;
    		}
    		return true;
    	}
    	
    	private boolean installApplication(File path) {
    		try {
				Intent promptInstall = new Intent(Intent.ACTION_VIEW)
			    .setDataAndType(Uri.fromFile(path), "application/vnd.android.package-archive");
				startActivity(promptInstall);
    		} catch (ActivityNotFoundException ex) {
    			return false;
    		}
    		//ref http://stackoverflow.com/questions/6362479/install-apk-programmatically-on-android
    		//for knowing when the application is finished installing
			return true;
    	}

		@Override
		protected Boolean doInBackground(Void... params) {
			this.publishProgress(State.DOWNLOADING_UPDATES);
			if (!downloadUpdates()) {
				this.publishProgress(State.DOWNLOADING_FAILED);
				return false;
			}

			this.publishProgress(State.INSTALLING_UPDATE1);
			installApplication(mobilePath);
			this.publishProgress(State.INSTALLING_UPDATE2);
			installApplication(betaPath);
			this.publishProgress(State.DONE_UPDATING);
			
			return true;
		}
		
		@Override
		protected void onProgressUpdate(State... progress) {
			currentState = progress[0];
			updateProgress();
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
		    //for testing only
			if (result) {
				btnContinue.setText("Continue");
				btnContinue.setEnabled(true);
			}
		}
    }
    
    private class CheckVersion extends AsyncTask<Void, Void, BuildInfo> {
    	
    	@Override
    	protected void onPreExecute() {
    	}
    	
		@Override
		protected BuildInfo doInBackground(Void... params) {
			BetaService service = new BetaService();
			
			LatestBuilds builds;
			try {
				builds = service.getLatestBuilds();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
			if (builds == null || builds.rolling == null) {
				return null;
			}
			
	    	PackageInfo packageInfo;
	    	try {
	    		packageInfo = getPackageManager().getPackageInfo("edu.rosehulman.android.directory", 0); 
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				return null;
			}
	    	
			int buildNumber = packageInfo.versionCode;
			if (buildNumber < builds.rolling.buildNumber) {
				return builds.rolling;
			}
			
			return null;
		}
		
		@Override
    	protected void onPostExecute(BuildInfo result) {
    	
    		if (result != null) {
    			String url = result.getMobileDownloadUrl();
    			Log.d("BetaManager", "Download URL: " + url);
    			
    			latestBuild = result;
    			
    			currentState = State.UPDATES_AVAILABLE;
    		} else {
    			currentState = State.NO_UPDATES;
    			Toast.makeText(StartupActivity.this, "No updates", Toast.LENGTH_SHORT).show();
    		}
    		updateProgress();
    	}
    	
    }
}
