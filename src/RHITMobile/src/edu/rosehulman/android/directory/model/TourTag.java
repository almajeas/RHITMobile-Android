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
	 * The id of the tag
	 */
	public long id;
	
	/**
	 * The name of the tag
	 */
	public String name;
	
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
		this.id = id;
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
		return new TourTag(root.getLong("Id"), root.getString("Name"));
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeLong(id);
		out.writeString(name);
	}
	
	public static final Parcelable.Creator<TourTag> CREATOR = new Parcelable.Creator<TourTag>() {

		@Override
		public TourTag createFromParcel(Parcel in) {
			long id = in.readLong();
			String name = in.readString();
			
			return new TourTag(id, name);
		}

		@Override
		public TourTag[] newArray(int size) {
			return new TourTag[size];
		}
		
	};
	
}
