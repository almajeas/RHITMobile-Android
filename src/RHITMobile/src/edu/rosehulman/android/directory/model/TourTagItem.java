package edu.rosehulman.android.directory.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a TourTag and the path needed to get to it
 */
public class TourTagItem implements Parcelable {
	
	/** The TourTag */
	public TourTag tag;
	
	/** The path to the TourTag */
	public String path;
	
	/**
	 * Creates a new TourTagItem
	 * 
	 * @param tag the TourTag
	 * @param path the path
	 */
	public TourTagItem(TourTag tag, String path) {
		this.tag = tag;
		this.path = path;
	}
	
	public boolean equals(TourTagItem o) {
		return o.tag.equals(tag) &&
				o.path.equals(path);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof TourTagItem)
			return equals((TourTagItem)o);
		return false;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(tag, flags);
		dest.writeString(path);
	}
	
	public static final Parcelable.Creator<TourTagItem> CREATOR = new Parcelable.Creator<TourTagItem>() {
		
		@Override
		public TourTagItem createFromParcel(Parcel source) {
			TourTag tag = source.readParcelable(TourTagItem.class.getClassLoader());
			String path = source.readString();
			return new TourTagItem(tag, path);
		}

		@Override
		public TourTagItem[] newArray(int size) {
			return new TourTagItem[size];
		}
	};
}
