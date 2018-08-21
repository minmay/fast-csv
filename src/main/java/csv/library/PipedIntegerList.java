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
 * A Field Parser that will convert a CSV of pipe | separated integers into an array of integers.
 */
public class PipedIntegerList extends FieldParser<Integer[]> {

	/**
	 * Parses a string into the give class.
	 *
	 * @param v The string value to valueOf.
	 * @return The class that this string represents.
	 */
	@Override
	public Integer[] valueOf(String v) {
		Integer r[] = null;
		if (v!=null) {
			String[] a = v.split("|");
			r = new Integer[a.length];
			for (int i=0;i<a.length;i++) {
				r[i] = Integer.parseInt(a[i]);
			}
		}
		return r;
	}
}
