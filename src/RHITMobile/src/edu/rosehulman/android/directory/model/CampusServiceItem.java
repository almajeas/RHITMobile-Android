package edu.rosehulman.android.directory.model;

/**
 * Represents a campus service search result 
 */
public class CampusServiceItem {
	
	/** The associated link */
	public Hyperlink link;
	
	/** The path to the link */
	public String path;

	/**
	 * Creates a new CampusServiceItem
	 * 
	 * @param link The hyperlink
	 * @param path The path to the hyperlink
	 */
	public CampusServiceItem(Hyperlink link, String path) {
		this.link = link;
		this.path = path;
	}
}