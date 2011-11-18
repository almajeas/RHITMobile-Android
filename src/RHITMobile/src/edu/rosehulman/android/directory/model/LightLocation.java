package edu.rosehulman.android.directory.model;

/**
 * A lightweight representation of a location
 */
public class LightLocation {
	
	/** The id of the location */
	public long id;
	
	/** The name of the location */
	public String name;

	/**
	 * Creates a new LightLocation
	 * 
	 * @param id The id of the location
	 * @param name The name of the location
	 */
	public LightLocation(long id, String name) {
		this.id = id;
		this.name = name;
	}

}
