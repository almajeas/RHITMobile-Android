package edu.rosehulman.android.directory.service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

public class SSLUtil {

	private static ClientConnectionManager manager;
	private static HttpParams params;

	private static void init() {
		if (manager != null && params != null)
			return;

		KeyStore trustStore;
		EasySSLSocketFactory sf;
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			sf = new EasySSLSocketFactory(trustStore);
			
		} catch (KeyStoreException e) {
			throw new RuntimeException("Error creating keystore", e);
			
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Required algorithm unavailable", e);
			
		} catch (CertificateException e) {
			throw new RuntimeException("Invalid certificate", e);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
			
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
			
		} catch (UnrecoverableKeyException e) {
			throw new RuntimeException(e);
		}
		
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		SchemeRegistry schemes = new SchemeRegistry();
		schemes.register(new Scheme("https", sf, 443));
		params = new BasicHttpParams();
		manager = new ThreadSafeClientConnManager(params, schemes);
	}

	public static ClientConnectionManager getManager() {
		init();

		return manager;
	}

	public static HttpParams getParams() {
		init();

		return params;
	}

}
