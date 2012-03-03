package edu.rosehulman.android.directory.model;

/**
 * Represents a term code and description
 */
public class TermCode {

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
}
