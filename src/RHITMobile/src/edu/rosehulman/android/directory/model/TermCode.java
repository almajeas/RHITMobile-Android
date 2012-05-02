package edu.rosehulman.android.directory.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a term code and description
 */
public class TermCode implements Parcelable {

	/** The term's code (ex. 201230) */
	public String code;
	
	/**
	 * Creates a new instance
	 * 
	 * @param code The term code
	 */
	public TermCode(String code) {
		this.code = code;
		assert(code.length() == 6);
	}
	
	public static TermCode[] deserialize(JSONArray array) throws JSONException {
		TermCode[] res = new TermCode[array.length()];
		for (int i = 0; i < res.length; i++) {
			res[i] = deserialize(array.getJSONObject(array.length() - i - 1));
		}
		return res;
	}
	
	public static TermCode deserialize(JSONObject root) throws JSONException {
		TermCode res = new TermCode(root.getString("Id"));
		//root.getString("Name");
		return res;
	}

	@Override
	public String toString() {
		String year = code.substring(0, 4);
		String term = code.substring(4);
		
		String termName;
		switch (Integer.parseInt(term)) {
		case 10:
			termName = "Fall";
			break;
		case 20:
			termName = "Winter";
			break;
		case 30:
			termName = "Spring";
			break;
		default:
			termName = "";
		}
		
		return String.format("%s %s", termName, year);
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof TermCode))
			return false;
		
		TermCode o = (TermCode)other;
		
		return code.equals(o.code);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(code);
	}
	
	public static final Parcelable.Creator<TermCode> CREATOR = new Parcelable.Creator<TermCode>() {

		@Override
		public TermCode createFromParcel(Parcel in) {
			String code = in.readString();
			return new TermCode(code);
		}

		@Override
		public TermCode[] newArray(int size) {
			return new TermCode[size];
		}
	};
}
