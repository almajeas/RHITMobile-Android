package edu.rosehulman.android.directory.service;

import java.security.KeyStore;

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

	private static void init() throws Exception {
		if (manager != null && params != null)
			return;

		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		trustStore.load(null, null);
		EasySSLSocketFactory sf = new EasySSLSocketFactory(trustStore);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		SchemeRegistry schemes = new SchemeRegistry();
		schemes.register(new Scheme("https", sf, 443));
		params = new BasicHttpParams();
		manager = new ThreadSafeClientConnManager(params, schemes);
	}

	public static ClientConnectionManager getManager() throws Exception {
		init();

		return manager;
	}

	public static HttpParams getParams() throws Exception {
		init();

		return params;
	}

}
