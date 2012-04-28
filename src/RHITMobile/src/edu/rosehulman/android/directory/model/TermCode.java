package edu.rosehulman.android.directory.model;

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
