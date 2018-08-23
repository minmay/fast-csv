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
 * The Original Code is Fast CSV.
 *
 * The Initial Developer of the Original Code is
 * Marco Antonio Villalobos, Jr. (mvillalobos@kineteque.com).
 * Portions created by the Initial Developer are Copyright (C) 2018
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Marco Antonio Villalobos, Jr. (mvillalobos@kineteque.com).
 */

package csv.library;

import java.util.HashSet;
import java.util.Set;

/**
 * Symbols and set of characters that are used for parsing comma separated values.
 */
public enum Symbol {

	/** The quote character. */
	QUOTE(
			false,
			new char[] {'"'},
			false
	),

	/** All characters except quote, white space (excluding space and tab), and the delimiter. */
	RELAXED_NO_QUOTE_WS_DL(
			true,
			new char[] {'"', '\n', 0x0B, '\f', '\r'},
			true
	),

	/** All characters except quote, white space, and the delimiter. */
	NO_QUOTE_WS_DL(
			true,
				new char[] {'"', ' ', '\t', '\n', 0x0B, '\f', '\r'},
			true
	),

	/** All characters except quote, new line, and the delimiter. */
	NO_QUOTE_NL_DL(
			true,
			new char[] {'"', '\r', '\n'},
			true
	),

	/** All characters except quote. */
	NO_QUOTE(
			true,
			new char[] {'"'},
			false
	),

	/** The new line character. */
	NL(
			false,
			new char[] {'\r', '\n'},
			false
	),

	/** The end of file marker. */
	EOF(
			false,
			new char[] {(char)-1},
			false
	),

	/** The delimiter. */
	DELIMITER(
			false,
			new char[] {},
			true
	);


	Symbol(boolean isExclusion, char[] symbols, boolean checkDelimiter) {
		this.isExclusion = isExclusion;
		this.symbols = symbols;
		this.checkDelimiter = checkDelimiter;
		set = new HashSet<Character>();
		for (char c:symbols) {
			set.add(c);
		}

	}

	/** Flags this symbol as an exclusive set. */
	public final boolean isExclusion;

	/** The characters used to create this set. */
	public final char[] symbols;

	/** The set of characters. */
	private final Set<Character> set;

	/** Flags this set to check the delimiter. */
	private final boolean checkDelimiter;


	/**
	 * Checks if the given character c is in this symbol set.
	 *
	 * @param delimiter The delimiter.
	 * @param c The character to evaluate.  If c == -1, and is_eof == true, this is an end of file.
	 * @param is_eof The end of file marker.  If c == -1, and is_eof == true, this is an end of file.
	 * @return True if this character is in the set of characters that represent this symbol.
	 */
	public boolean isCharIn(char delimiter, int c, boolean is_eof) {
		boolean v;
		if (this==EOF && is_eof && c == -1) {
			v = true;
		} else if (this!=EOF && is_eof) {
			v = false;
		} else if (isExclusion)  {
			char aschar = (char) c;
			if (checkDelimiter) {
				v = !set.contains(aschar) && delimiter!=aschar;
			} else {
				v = !set.contains(aschar);
			}
		} else {
			char aschar = (char) c;
			if (checkDelimiter) {
				v = set.contains(aschar) || delimiter==aschar;
			} else {
				v = set.contains(aschar);
			}
		}

		return v;
	}
}
