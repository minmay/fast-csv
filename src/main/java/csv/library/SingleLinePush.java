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


import java.util.function.Consumer;

/**
 * A line push callback designed for handling a single csv line.
 */
public class SingleLinePush implements Consumer<CSVLine> {

	/** The single comma separated values data-structure. */
	private CSVLine info = null;

	/**
	 * Allows the application programmer to handle pushing a comma separated value line
	 *
	 * @param info The comma separated value line.
	 */
	public void accept(CSVLine info) {
		this.info = info;
	}

	/**
	 * Retrieves the single comma separated value line that was pushed.
	 *
	 * @return The single comma separated value line that was pushed.
	 */
	public CSVLine getInfo() {
		return info;
	}
}
