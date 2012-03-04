package edu.rosehulman.android.directory.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a term code and description
 */
public class TermCode implements Parcelable {

	/** The term's code (ex. 201230) */
	public String code;
	
	/** The term's name (ex. Spring 2012) */
	public String name;
	
	/**
	 * Creates a new instance
	 * 
	 * @param code The term code
	 * @param name The name of the term
	 */
	public TermCode(String code, String name) {
		this.code = code;
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof TermCode))
			return false;
		
		TermCode o = (TermCode)other;
		
		return code.equals(o.code) && 
				name.equals(o.name);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(code);
		out.writeString(name);
	}
	
	public static final Parcelable.Creator<TermCode> CREATOR = new Parcelable.Creator<TermCode>() {

		@Override
		public TermCode createFromParcel(Parcel in) {
			String code = in.readString();
			String name = in.readString();
			return new TermCode(code, name);
		}

		@Override
		public TermCode[] newArray(int size) {
			return new TermCode[size];
		}
	};
}
