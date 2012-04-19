package edu.rosehulman.android.directory.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.net.SSLCertificateSocketFactory;

/** Provides common functionality for making REST web service calls */
public abstract class RestClient {

	/** The set of parameters added to the query string */
	protected List<NameValuePair> queryParams;

	/** The path of the request */
	protected String path;

	private String host;
	private int port;
	private HttpMethod method;

	/** Create a new RestClient
	 * 
	 * @param host
	 *            The host to send the request to (i.e. example.com)
	 * @param port
	 *            The port the remote service is running on
	 * @param path
	 *            The request path (i.e. index.htm) */
	public RestClient(String host, int port, String path) {
		this.queryParams = new LinkedList<NameValuePair>();
		this.host = host;
		this.port = port;
		this.path = path;
		this.method = HttpMethod.GET;
	}

	/** Adds a parameter to the request's query string. This method does not do
	 * duplicate detection. Adding a parameter more than once will result in a
	 * query string with a duplicated parameter.
	 * 
	 * @param name
	 *            The name of the parameter to add
	 * @param value
	 *            The value to associate with the parameter */
	public void addParameter(String name, String value) {
		queryParams.add(new BasicNameValuePair(name, value));
	}

	/** Sets the HTTP method that is used for this request
	 * 
	 * @param method
	 *            The method to use */
	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	/** Perform the REST request, parsing the server's response as a JSONObject
	 * 
	 * @return The HttpResponse
	 * 
	 * @throws URISyntaxException
	 *             If an invalid URI was used
	 * @throws Exception
	 *             On other errors, including Internal Server Errors */
	protected HttpResponse performRequest() throws URISyntaxException, Exception {
		String query = null;
		if (queryParams.size() > 0) {
			query = URLEncodedUtils.format(queryParams, "UTF-8");
		}
		URI uri;
		uri = URIUtils.createURI("https", host, port, path, query, null);

		SSLCertificateSocketFactory.getInsecure(0, null);
		HttpRequestBase request;

		switch (method) {
		case GET:
			request = new HttpGet(uri);
			break;
		case POST:
			request = new HttpPost(uri);
			break;
		default:
			throw new UnsupportedOperationException("Invalid http method");
		}
		
		HttpClient http = new DefaultHttpClient(SSLUtil.getManager(), SSLUtil.getParams());
		HttpResponse response = http.execute(request);

		int statusClass = response.getStatusLine().getStatusCode() / 100;
		if (statusClass == 5) {
			String msg = "Internal Server Error: " +
				response.getStatusLine().toString();
			throw new Exception(msg);
		}

		return response;
	}


}
