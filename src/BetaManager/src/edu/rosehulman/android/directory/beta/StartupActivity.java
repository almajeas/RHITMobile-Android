package edu.rosehulman.android.directory.beta;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
	
	private File betaPath;
	private File mobilePath;
	
	private static final String PREF_STATE = "STATE";
	
	private State currentState = State.CHECKING_FOR_UPDATES;
	
	private BuildInfo latestBuild;
	
	private enum State {
		CHECKING_FOR_UPDATES,
		NO_UPDATES,
		UPDATE_CHECK_FAILED,
		UPDATES_AVAILABLE,
		
		DOWNLOADING_UPDATES,
		INSTALLING_UPDATE1,
		INSTALLING_UPDATE2,
		
		DONE_UPDATING,
		
		DOWNLOADING_FAILED;
		
		public static State fromOrdinal(int ordinal) {
			for (State state : State.values()) {
				if (state.ordinal() == ordinal) {
					return state;
				}
			}
			return CHECKING_FOR_UPDATES;
		}
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
        
        //build file paths
        String storageDirectory = getExternalFilesDir(null) + "/updates/";
		new File(storageDirectory).mkdirs();
		betaPath = new File(storageDirectory, "BetaManager.apk");
		mobilePath = new File(storageDirectory, "RHITMobile.apk");
        
		//pick up where we left off
        int state = getPreferences(MODE_PRIVATE).getInt(PREF_STATE, State.CHECKING_FOR_UPDATES.ordinal());
        currentState = State.fromOrdinal(state);
        Log.d("BetaManager", "Starting up in state: " + currentState.toString());
        switch (currentState) {
        case INSTALLING_UPDATE1:
        	//TODO verify that the app is installed
        	updateProgress(State.INSTALLING_UPDATE2);
        	installApplication(betaPath);
        	break;
        	
        case INSTALLING_UPDATE2:
        	//TODO verify that the app is installed
        	updateProgress(State.DONE_UPDATING);
        	break;
        	
        default:
        	updateProgress(State.CHECKING_FOR_UPDATES);
            new CheckVersion().execute();
        	break;
        }
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("android.intent.action.PACKAGE_ADDED");
//        filter.addDataScheme("package");
//        this.registerReceiver(packageReceiver, filter);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	//this.unregisterReceiver(packageReceiver);
    }

    @Override
    protected void onDestroy() {
    	super.onPause();
    	
    	//this.unregisterReceiver(packageReceiver);
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d("BetaManager", "Received activity result: " + requestCode + ", " + resultCode);
    	
    	//if (requestCode != REQUEST_STARTUP_CODE)
    	//	return;
    	
		updateProgress(State.INSTALLING_UPDATE2);
		installApplication(betaPath);
    	
    	switch (resultCode) {
    		case Activity.RESULT_CANCELED:
    			break;
    		case Activity.RESULT_OK:
    			break;	
    	}
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
    
    private void updateProgress(State state) {
    	currentState = state;
    	this.getPreferences(MODE_PRIVATE).edit().putInt(PREF_STATE, state.ordinal()).commit();
    	
    	switch (state) {
    	case NO_UPDATES:
    		btnContinue.setEnabled(true);
    		stepCheckForUpdates.setState(StatusState.SUCCESS);
    		break;
    		
    	case UPDATE_CHECK_FAILED:
    		btnContinue.setEnabled(true);
    		stepCheckForUpdates.setState(StatusState.ERROR);
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
    		stepUpdatingMobileDirectory.setState(StatusState.IN_PROGRESS);
    		break;
    		
    	case INSTALLING_UPDATE2:
    		//possible to get here after restarting the application
    		stepCheckForUpdates.setState(StatusState.SUCCESS);
    		stepPromptForUpdate.setVisibility(View.VISIBLE);
    		stepPromptForUpdate.setState(StatusState.SUCCESS);
    		stepDownloading.setVisibility(View.VISIBLE);
    		stepDownloading.setState(StatusState.SUCCESS);
    		stepUpdatingMobileDirectory.setVisibility(View.VISIBLE);
    		stepUpdatingMobileDirectory.setState(StatusState.SUCCESS);
    		stepUpdatingBetaManager.setVisibility(View.VISIBLE);
    		stepUpdatingBetaManager.setState(StatusState.IN_PROGRESS);
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
    		btnContinue.setEnabled(true);
    		break;
    	}
    }
    
    private BroadcastReceiver packageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {			
			switch (currentState) {
			case INSTALLING_UPDATE1:
				updateProgress(State.INSTALLING_UPDATE2);
				installApplication(betaPath);
				break;
			default:
				break;
			}
		}
	};
	
	private boolean installApplication(File path) {
		try {
			Intent promptInstall = new Intent(Intent.ACTION_VIEW)
		    .setDataAndType(Uri.fromFile(path), "application/vnd.android.package-archive");
			startActivityForResult(promptInstall, 42);
		} catch (ActivityNotFoundException ex) {
			return false;
		}
		return true;
	}
    
    private class UpdateApplication extends AsyncTask<Void, State, Boolean>
    {
    	private void downloadFile(String url, File path) throws Exception {
    		FileOutputStream fout = new FileOutputStream(path);
    		HttpClient client = new DefaultHttpClient();
    		HttpResponse response = client.execute(new HttpGet(url));
    		response.getEntity().writeTo(fout);
    	}
    	
    	private boolean downloadUpdates() {
    		String storageDirectory = getExternalFilesDir(null) + "/updates/";
    		new File(storageDirectory).mkdirs();
			
    		try {
        		downloadFile(latestBuild.getBetaManagerDownloadUrl(), betaPath);
        		downloadFile(latestBuild.getMobileDownloadUrl(), mobilePath);	
    		} catch (Exception ex) {
    			Log.e("BetaManager", "Failed to download a file", ex);
    			return false;
    		}
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
			//this.publishProgress(State.INSTALLING_UPDATE2);
			//installApplication(betaPath);
			//this.publishProgress(State.DONE_UPDATING);
			
			return true;
		}
		
		@Override
		protected void onProgressUpdate(State... progress) {
			updateProgress(progress[0]);
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
    	
    	private boolean checkFailed = false;
    	
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
				checkFailed = true;
				return null;
			}
			
			if (builds == null || builds.rolling == null) {
				return null;
			}
			
			int buildNumber = Env.getBuildNumber(StartupActivity.this, Env.RHIT_MOBILE);
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
    			
    			updateProgress(State.UPDATES_AVAILABLE);
    		} else if (checkFailed) {
    			updateProgress(State.UPDATE_CHECK_FAILED);
    		} else {
    			updateProgress(State.NO_UPDATES);
    		}
    	}
    	
    }
}

