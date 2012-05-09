package edu.rosehulman.android.directory.model;

import java.util.HashMap;

/**
 * Used to denote a type of hyperlink
 */
public enum HyperlinkType {

	/** link intended to be launched ina web browser */
	WEBSITE,
	
	/** link intended to be viewed as an image */
	IMAGE,
	
	/** link to playable video */
	VIDEO,
	
	/** link to streamable audio file */
	AUDIO;
	
	private static HashMap<Integer, HyperlinkType> types;
	
	static {
		types = new HashMap<Integer, HyperlinkType>();
		for (HyperlinkType type : HyperlinkType.values()) {
			types.put(type.ordinal(), type);
		}
	}
	
	public static HyperlinkType fromOrdinal(int ordinal) {
		return types.get(ordinal);
	}
}
