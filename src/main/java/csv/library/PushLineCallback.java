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
 * The call back used for when a csv line is pushed.
 */
public interface PushLineCallback {

	/**
	 * Allows the application programmer to handle pushing a comma separated value line
	 *
	 * @param info The comma separated value line.
	 */
	void pushLine(CSVLine info);
}