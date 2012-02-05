package edu.rosehulman.android.directory.util;

/**
 * Utility functions to deal with ordinal numbers (such as "2nd, 3rd, ...")
 */
public class Ordinal {
	
	private static String getSuffix(int value) {
		switch (value % 10) {
		case 1:
			return "st";
		case 2:
			return "nd";
		case 3:
			return "rd";
		default:
			return "th";
		}
	}
	
	/**
	 * Get the ordinal number for the given value
	 * 
	 * @param value The value to stringify
	 * @return The number with the proper suffix
	 */
	public static String convert(int value) {
		return value + getSuffix(value);
	}

}
