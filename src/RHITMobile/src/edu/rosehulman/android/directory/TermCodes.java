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
	
	//FIXME Retrieve term codes from remote server
	
	private static TermCode[] TERMS = new TermCode[] {
			new TermCode("201230"),
			new TermCode("201220"),
			new TermCode("201210"),
			new TermCode("201130"),
			new TermCode("201120"),
			new TermCode("201110"),
			new TermCode("201030"),
			new TermCode("201020"),
			new TermCode("201010"),
			new TermCode("200930"),
			new TermCode("200920"),
			new TermCode("200910")
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
