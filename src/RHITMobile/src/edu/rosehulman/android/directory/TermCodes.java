package edu.rosehulman.android.directory;

import edu.rosehulman.android.directory.model.TermCode;

/**
 * Utility class for managing term codes
 */
public class TermCodes {
	
//	private static final int YEARS = 5;
//	private static final int TERMS_PER_YEAR = 4;
//	
//	private static final String[] TERM_NAMES = new String[] {
//		"Fall", "Winter", "Spring", "Summer"
//	};
	
	private static TermCode[] TERMS = new TermCode[] {
			new TermCode("201230", "Spring 2012"),
			new TermCode("201220", "Winter 2011"),
			new TermCode("201210", "Fall 2011"),
			new TermCode("201130", "Spring 2011"),
			new TermCode("201120", "Winter 2010"),
			new TermCode("201110", "Fall 2010"),
			new TermCode("201030", "Spring 2010"),
			new TermCode("201020", "Winter 2009"),
			new TermCode("201010", "Fall 2009"),
			new TermCode("200930", "Spring 2009"),
			new TermCode("200920", "Winter 2008"),
			new TermCode("200910", "Fall 2008")
		};

	/**
	 * Generate 5 years worth of TermCodes in reverse order
	 *  
	 * @return The term codes
	 */
	public static TermCode[] generateTerms() {
		
		return TERMS;
		
//		if (res == null) {
//			Calendar cal = Calendar.getInstance();
//			
//			int currentYear = cal.get(Calendar.YEAR);
//			
//			res = new TermCode[YEARS * TERMS_PER_YEAR];
//			
//			for (int diff = 0; diff < YEARS; diff++) {
//				int year = currentYear - diff;
//				
//				for (int term = 1; term <= TERMS_PER_YEAR; term++) {
//					String termCode = String.format("%04d%02d", year, term*10);
//					
//					
//				}
//			}
//			
//		}
//		
//		return res;
		
	}
	
}
