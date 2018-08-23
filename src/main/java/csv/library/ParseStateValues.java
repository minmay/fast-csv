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

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A data-structure for keeping track of parse state.
 */
public class ParseStateValues {

	/** The delimiter to use for separating, usually this is a comma. */
	final char delimiter;

	/** The names of the field parsers to use in the type parser for the ith row of the csv line when a field is converted into an object. */
	final String types[];

	/** The type parser to use when a CSV line is parsed and converted into an object. */
	final TypeParser typeParsers;

	/** This parameter is optional.  If not null, then a quoted string will be limited to this value.  Any lines that exceed a the limit are flagged as errors. */
	final Integer quote_length_limit;

	final boolean is_relaxed;

	/** A flag that marks an end of file has been reached. */
	boolean isEOF;

	/** The character buffer containing the CSV lines. */
	CharBuffer in;

	/** The current state in the CSV transition table. */
	State current = State.START;

	/** The comma separated values that belong to a line that are being built. */
	private List<String> csvs = null;																					//array list has to be passed around too, otherwise a partial list tht needs to go out of stream will be erased if not put in out

	/** The current comma separated that is being built. */
	private StringBuilder csv = null;

	/** The raw, un-parsed csv line that is being built. */
	private StringBuilder raw = null;

	/** A flag to determine if the previous character was a carriage return "\r". */
	boolean is_prev_char_cr = false;

	/**
	 * Creates an instance of this class.
	 *
	 * All parameters are immutable.
	 *
	 * @param delimiter The delimiter to use for separating, usually this is a comma.
	 * @param types The names of the field parsers to use in the type parser for the ith row of the csv line when a field is converted into an object.
	 * @param typeParsers The type parser to use when a CSV line is parsed and converted into an object.
	 * @param quote_length_limit This parameter is optional.  If not null, then a quoted string will be limited to this value.  Any lines that exceed a the limit are flagged as errors.
	 * @param is_relaxed todo
	 */
	public ParseStateValues(char delimiter, String[] types, TypeParser typeParsers, Integer quote_length_limit, boolean is_relaxed) {
		this.delimiter = delimiter;
		this.types = types;
		this.typeParsers = typeParsers;
		this.quote_length_limit = quote_length_limit;
		this.is_relaxed = is_relaxed;
	}

	public void addToCsvs(String csv) {
		if (csvs == null) {
			csvs = new ArrayList<String>();
		}
		csvs.add(csv == null ? "" : csv);
	}

	public String[] getCsvs() {
		return csvs == null ? null : csvs.toArray(new String[csvs.size()]);
	}

	public void resetCsvs() {
		csvs = null;
	}

	public void appendToCsv(char c) {
		if (csv == null) {
			csv = new StringBuilder();
		}
		csv.append(c);
	}

	public String getCsv() {
		return csv == null ? null : csv.toString();
	}

	public int getCsvLength() {
		return csv == null ? -1 : csv.length();
	}

	public void resetCsv() {
		csv = null;
	}

	public void appendToRaw(char c) {
		if (raw == null) {
			raw = new StringBuilder();
		}
		raw.append(c);
	}

	public String getRaw() {
		return raw == null ? null : raw.toString();
	}

	public void resetRaw() {
		raw = null;
	}
}
