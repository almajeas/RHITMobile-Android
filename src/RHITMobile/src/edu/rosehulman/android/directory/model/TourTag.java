package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a tag for tour generation
 */
public class TourTag implements Parcelable {

	/**
	 * The tag id (understandable by the server)
	 */
	public long tagId;
	
	/**
	 * The name of the tag
	 */
	public String name;
	
	/**
	 * Is this tag a default tag
	 */
	public boolean isDefault;
	
	/**
	 * Creates a new, uninitialized TourTag
	 */
	public TourTag() {
	}

	/**
	 * Creates a new, initialized TourTag
	 * 
	 * @param id The ID of the tag
	 * @param name The name to use
	 */
	public TourTag(long id, String name) {
		this.tagId = id;
		this.name = name;
	}
	
	/**
	 * Deserialize the given JSONObject into a new instance of TourTag
	 * 
	 * @param root The JSONObject with the necessary field to create a new TourTag
	 * @return A new CampusServicesCategory initialized from the given JSONObject
	 * @throws JSONException
	 */
	public static TourTag deserialize(JSONObject root) throws JSONException {
		TourTag res = new TourTag();
		
		res.tagId = root.getLong("Id");
		res.name = root.getString("Name");
		res.isDefault = root.getBoolean("IsDefault");
		
		return res;
	}

	public boolean equals(TourTag o) {
		return o.tagId == tagId &&
				o.name.equals(name);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof TourTag)
			return equals((TourTag)o);
		return false;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeLong(tagId);
		out.writeString(name);
		out.writeInt(isDefault ? 1 : 0);
	}
	
	public static final Parcelable.Creator<TourTag> CREATOR = new Parcelable.Creator<TourTag>() {

		@Override
		public TourTag createFromParcel(Parcel in) {
			TourTag res = new TourTag();
			
			res.tagId = in.readLong();
			res.name = in.readString();
			res.isDefault = in.readInt() == 0 ? false : true;
			
			return res;
		}

		@Override
		public TourTag[] newArray(int size) {
			return new TourTag[size];
		}
		
	};
	
}
