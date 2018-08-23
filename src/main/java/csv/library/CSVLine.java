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
 * The Original Code is Fast CVS.
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
 * Encapsulates a line of comma separated values.
 */
public class CSVLine {

	/** The delimiter. */
	private final char delimiter;

	/** A flag that represents whether this is an error value. */
	private final boolean isError;

	/** The raw csv line, unparsed. */
	private final String raw;

	/** An array of the csv values. */
	private final String[] csvs;

	/** The values that were converted by the type parser. */
	private final Object[] values;

	/**
	 * Creates an instance of this CSV information.
	 *
	 * @param delimiter The delimiter.
	 * @param error The error flag to mark this as an error.
	 * @param raw The raw csv line.
	 * @param csvs The separated values.
	 */
	public CSVLine(char delimiter, boolean error, String raw, String[] csvs) {
		this.delimiter = delimiter;
		isError = error;
		this.raw = raw;
		this.csvs = csvs;
		this.values = csvs;
	}

	/**
	 * Creates an instance of this CSV information.
	 *
	 * @param delimiter The delimiter.
	 * @param error The error flag to mark this as an error.
	 * @param raw The raw csv line.
	 * @param csvs The separated values.
	 * @param values The values that were converted by the type parser.
	 */
	public CSVLine(char delimiter, boolean error, String raw, String[] csvs, Object[] values) {
		this.delimiter = delimiter;
		isError = error;
		this.raw = raw;
		this.csvs = csvs;
		this.values = values;
	}

	/**
	 * Retrieves the delimiter.
	 *
	 * @return The delimiter.
	 */
	public char getDelimiter() {
		return delimiter;
	}

	/**
	 * Retrieves the flag that represents whether this is an error value.
	 *
	 * @return The flag that represents whether this is an error value.
	 */
	public boolean isError() {
		return isError;
	}

	/**
	 * Retrieves the raw csv line unparsed.
	 *
	 * @return The raw csv line unparsed.
	 */
	public String getRaw() {
		return raw;
	}

	/**
	 * The comma separated values.
	 *
	 * @return The comma separated values.
	 */
	public String[] getCsvs() {
		return csvs;
	}

	/**
	 * Retrieve the values that were converted by the type parser.
	 *
	 * @return The values that were converted by the type parser.
	 */
	public Object[] getValues() {
		return values;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 *
	 * @param   obj   the reference object with which to compare.
	 * @return  <code>true</code> if this object is the same as the obj
	 *          argument; <code>false</code> otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		CSVLine csvLine = (CSVLine) obj;

		if (delimiter != csvLine.delimiter) return false;
		if (isError != csvLine.isError) return false;
		if (raw != null ? !raw.equals(csvLine.raw) : csvLine.raw != null) return false;

		return true;
	}

	/**
	 * Returns a hash code value for the object. This method is
	 * supported for the benefit of hashtables such as those provided by
	 * <code>java.util.Hashtable</code>.
	 *
	 * @return  a hash code value for this object.
	 */
	@Override
	public int hashCode() {
		int result = (int) delimiter;
		result = 31 * result + (isError ? 1 : 0);
		result = 31 * result + (raw != null ? raw.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return raw;
	}
}
