package org.neos.gams;

public class GAMSUtil {
	
	/**
	 * Parse GAMS output string into double
	 * @param input
	 * @return
	 */
	public static double parseDouble(String input) {

		if (input.equals("."))
			return 0;
		if (input.equals("-INF"))
			return Double.NEGATIVE_INFINITY;

		if (input.equals("+INF"))
			return Double.POSITIVE_INFINITY;

		double result;
		try {
			result = Double.parseDouble(input);
		} catch (NumberFormatException e) {
			result = 0;
		}
		return result;
	}
}
