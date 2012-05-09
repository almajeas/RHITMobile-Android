package edu.rosehulman.android.directory.model;

import java.util.HashMap;

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
	 * The type of the link
	 */
	public HyperlinkType type;
	
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
	public Hyperlink(String name, HyperlinkType type, String url) {
		this.name = name;
		this.type = type;
		this.url = url;
	}
	
	private static HashMap<String, HyperlinkType> typeMap;

	static {
		typeMap = new HashMap<String, HyperlinkType>();
		typeMap.put("W", HyperlinkType.WEBSITE);
		typeMap.put("I", HyperlinkType.IMAGE);
		typeMap.put("V", HyperlinkType.VIDEO);
		typeMap.put("A", HyperlinkType.AUDIO);
	}
	
	public static Hyperlink deserialize(JSONObject root) throws JSONException {
		String name = root.getString("Name");
		
		HyperlinkType type = HyperlinkType.WEBSITE;
		if (root.has("Type"))
			type = typeMap.get(root.getString("Type"));
		String url = root.getString("Url");
		
		return new Hyperlink(name, type, url);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(name);
		out.writeInt(type.ordinal());
		out.writeString(url);
	}
	
	public static final Parcelable.Creator<Hyperlink> CREATOR = new Parcelable.Creator<Hyperlink>() {

		@Override
		public Hyperlink createFromParcel(Parcel in) {
			String name = in.readString();
			HyperlinkType type = HyperlinkType.fromOrdinal(in.readInt());
			String url = in.readString();
			
			return new Hyperlink(name, type, url);
		}

		@Override
		public Hyperlink[] newArray(int size) {
			return new Hyperlink[size];
		}
		
	};
	
}
