package edu.rosehulman.android.directory.auth;

import java.io.IOException;

import org.json.JSONException;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import edu.rosehulman.android.directory.C;
import edu.rosehulman.android.directory.LoginActivity;
import edu.rosehulman.android.directory.model.BannerAuthResponse;
import edu.rosehulman.android.directory.service.ClientException;
import edu.rosehulman.android.directory.service.MobileDirectoryService;
import edu.rosehulman.android.directory.service.ServerException;

public class AccountAuthenticator extends AbstractAccountAuthenticator {
	
	public static final String ACCOUNT_TYPE = "edu.rosehulman.android.directory";
	public static final String TOKEN_TYPE = "Kerberos";
	
	public static final String KEY_EXPIRATION_TIME = "ExpirationTime";
	public static final String KEY_TERM_CODES = "TermCodes";
	public static final String KEY_TERM_CODE = "TermCode";
	
	private Context mContext;

	public AccountAuthenticator(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options)
			throws NetworkErrorException {
		Bundle res = new Bundle();
		Intent intent = LoginActivity.createIntentForNewAccount(mContext, response);
		
		res.putParcelable(AccountManager.KEY_INTENT, intent);
		
		return res;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
		Log.e(C.TAG, "FIXME Unexpected call to confirmCredentials");
		Bundle res = new Bundle();
		res.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION);
		res.putString(AccountManager.KEY_ERROR_MESSAGE, "Not implemented");
		return res;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
		return new Bundle();
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		
		AccountManager manager = AccountManager.get(mContext);
		
		String username = account.name;
		String password = manager.getPassword(account);
		
		BannerAuthResponse auth;
		Bundle res = new Bundle();
		
		try {
			Log.d(C.TAG, "Logging in for auth token");
			MobileDirectoryService service = new MobileDirectoryService();
			auth = service.login(username, password);

		} catch (ClientException e) {
			Log.w(C.TAG, "Invalid credentials");
			Intent intent = LoginActivity.createIntentForUpdateAccount(mContext, response, account);
			res.putParcelable(AccountManager.KEY_INTENT, intent);
			return res;
			
		} catch (ServerException e) {
			Log.e(C.TAG, "Server failed on login, aborting...", e);
			res.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_REMOTE_EXCEPTION);
			res.putString(AccountManager.KEY_ERROR_MESSAGE, "Authentication service is rejecting requests. Please try again later.");
			return res;
			
		} catch (JSONException e) {
			Log.e(C.TAG, "Bad JSON response, aborting...", e);
			res.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_INVALID_RESPONSE);
			res.putString(AccountManager.KEY_ERROR_MESSAGE, "API Version Mismatch.  Ensure your app is up to date and try again.");
			return res;
			
		} catch (IOException e) {
			Log.e(C.TAG, "Login failed due to network issue, giving up...");
			throw new NetworkErrorException(e);
		}
		
		String terms[] = new String[auth.terms.length];
		for (int i = 0; i < terms.length; i++) {
			terms[i] = auth.terms[i].code;
		}

		res.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
		res.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
		res.putString(AccountManager.KEY_AUTHTOKEN, auth.token);
		res.putLong(KEY_EXPIRATION_TIME, auth.expiration.getTime());
		res.putStringArray(KEY_TERM_CODES, terms);
		res.putString(KEY_TERM_CODE, auth.currentTerm.code);
		return res;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		return "RHIT Mobile Token";
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
		Bundle res = new Bundle();
		res.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
		return res;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		Bundle res = new Bundle();
		Intent intent = LoginActivity.createIntentForUpdateAccount(mContext, response, account);
		res.putParcelable(AccountManager.KEY_INTENT, intent);
		return res;
	}
}
