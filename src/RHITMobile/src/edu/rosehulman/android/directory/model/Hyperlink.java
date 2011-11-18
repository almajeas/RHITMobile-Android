package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a hyperlink (name/url pair)
 */
public class Hyperlink implements Parcelable {

	/**
	 * The name of the link
	 */
	public String name;
	
	/**
	 * The URL that can be viewed in a web browser
	 */
	public String url;
	
	/**
	 * Creates a new, uninitialized Hyperlink
	 */
	public Hyperlink() {
	}

	/**
	 * Creates a new, initialized Hyperlink
	 * 
	 * @param name The name to use
	 * @param url The URL to use
	 */
	public Hyperlink(String name, String url) {
		this.name = name;
		this.url = url;
	}
	
	public static Hyperlink deserialize(JSONObject root) throws JSONException {
		return new Hyperlink(root.getString("Name"), root.getString("Url"));
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(name);
		out.writeString(url);
	}
	
	public static final Parcelable.Creator<Hyperlink> CREATOR = new Parcelable.Creator<Hyperlink>() {

		@Override
		public Hyperlink createFromParcel(Parcel in) {
			String name = in.readString();
			String url = in.readString();
			
			return new Hyperlink(name, url);
		}

		@Override
		public Hyperlink[] newArray(int size) {
			return new Hyperlink[size];
		}
		
	};
	
}
