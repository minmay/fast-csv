/*
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is CSV Library.
 *
 * The Initial Developer of the Original Code is
 * Marco Antonio Villalobos, Jr. (mvillalobos@kineteque.com).
 * Portions created by the Initial Developer are Copyright (C) 2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Marco Antonio Villalobos, Jr. (mvillalobos@kineteque.com).
 */

package csv.library;

/**
 * The main entry point into this application for parsing and writing comma separated values.
 */
public class FastCSVWriter {

	/** Regular expression for when quotes are needed in a string csv. */
	public final static String REQUIRES_QUOTE_REG_EX = "^\\s.*|.*\".*|.*\n.*|.*\\s$";

	/**
	 * Given a delimiter and an array of objects, this method will convert the array into CSV format.
	 *
	 * @param delimiter The delimiter to use for CSV.
	 * @param values The array of objects that are to be placed into CSV format.
	 * @return A string representing valid CSV of the array of values.
	 */
	public static String delimit(char delimiter, Object ... values) {
		StringBuilder builder = new StringBuilder();
		boolean prependDelimiter = false;
		if (values != null) {
			for (Object v:values) {
				if (prependDelimiter) {
					builder.append(delimiter);
				}

				if (v == null) {
					builder.append("");
				}
				else {
					builder.append(quoteString(delimiter, v.toString()));
				}
				prependDelimiter = true;
			}
		}
		return builder.toString();
	}

	/**
	 * Quotes a string as needed to create a CSV.
	 *
	 * Please see the rules as specified on http://en.wikipedia.org/wiki/Comma-separated_values
	 *
	 *
	 * @param separator The csv separator, usually the comma.
	 * @param s The string to quote.
	 * @return The string quoted or unquoted according to CSV rules.
	 */
	public static String quoteString(char separator, String s) {
		StringBuilder builder = new StringBuilder();
		if (s.matches(REQUIRES_QUOTE_REG_EX +"|.*"+separator+".*")) {
			builder.append('"');
			int current = 0;
			while (true) {
				int quoteIndex = s.indexOf('"', current);
				if (quoteIndex==-1) {
					builder.append(s.substring(current));
					break;
				} else {
					builder.append(s.substring(current, quoteIndex)).append('"').append('"');
					current = quoteIndex + 1;
				}
			}
			builder.append('"');
		} else {
			builder.append(s);
		}
		return builder.toString();
	}

}
