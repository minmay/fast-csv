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
 * The finite state automata for parsing comma separated values.
 */
public enum State {

	/** The start state. */
	START,
	/** The unquoted string state. */
	UNQUOTED_STRING,
	/** The quoted string state. */
	QUOTED_STRING,
	/** The end quote state. */
	END_QUOTE,
	/** The carriage return state. */
	CR,
	/** The push value state. */
	PUSH,
	/** The final, or end state. */
	FINAL,
	/** The error state. */
	ERROR;

	public State transition(ParseStateValues psv, int c) {
		return transition(psv.delimiter, c, psv.is_relaxed, psv.isEOF, psv.is_prev_char_cr, psv.getCsvLength(), psv.quote_length_limit);
	}

	/**
	 * Given a delimiter, the current character, and the is end of file flag, this will transition the finite state automata to the correct state.
	 *
	 *
	 * @param delimiter The delimiter to use.
	 * @param c The current character.  Please remember to cast the character as an int.
	 * @param is_relaxed The relaxed mode flag.  Relaxed mode allows starting unquoted with a space or tab.
	 * @param is_eof The end of file flag.
	 * @param is_prev_char_cr A flag to determine if the previous character was a carriage return "\r".
	 * @param csv_length The current csv length.
	 * @param limit The limit of length for a quoted comma separated value.	 @return The next state that adheres to the finite state automata for comma separated values parsing.
	 */
	private State transition(char delimiter, int c, boolean is_relaxed, boolean is_eof, boolean is_prev_char_cr, int csv_length, Integer limit) {
		State current = this;
		State r;
		switch(current) {
			case START:
				if (is_relaxed && Symbol.RELAXED_NO_QUOTE_WS_DL.isCharIn(delimiter, c, is_eof)) {
					r = State.UNQUOTED_STRING;
				} else if (!is_relaxed && Symbol.NO_QUOTE_WS_DL.isCharIn(delimiter, c, is_eof)) {
					r = State.UNQUOTED_STRING;
				} else if (Symbol.QUOTE.isCharIn(delimiter, c, is_eof)) {
					r = State.QUOTED_STRING;
				} else if (Symbol.NL.isCharIn(delimiter, c, is_eof)) {
					r = is_prev_char_cr ? State.CR : State.START;
				} else if (Symbol.DELIMITER.isCharIn(delimiter, c, is_eof)) {
					r = State.PUSH;
				} else if (Symbol.EOF.isCharIn(delimiter, c, is_eof)) {
					r = State.FINAL;
				} else {
					r = State.ERROR;
				}
				break;

			case UNQUOTED_STRING:
				if (Symbol.NO_QUOTE_NL_DL.isCharIn(delimiter, c, is_eof)) {
					r = State.UNQUOTED_STRING;
				} else if (Symbol.DELIMITER.isCharIn(delimiter, c, is_eof)) {
					r = State.PUSH;
				} else if (Symbol.NL.isCharIn(delimiter, c, is_eof)) {
					r = State.START;
				} else if (Symbol.EOF.isCharIn(delimiter, c, is_eof)) {
					r = State.FINAL;
				}else {
					r = State.ERROR;
				}
				break;

			case QUOTED_STRING:
				if (limit!=null && csv_length > limit) {
					r = State.ERROR;
				} else if (Symbol.NO_QUOTE.isCharIn(delimiter, c, is_eof)) {
					r = State.QUOTED_STRING;
				} else if (Symbol.QUOTE.isCharIn(delimiter, c, is_eof)) {
					r = State.END_QUOTE;
				} else {
					r = State.ERROR;
				}
				break;

			case END_QUOTE:
				if (Symbol.QUOTE.isCharIn(delimiter, c, is_eof)) {
					r = State.QUOTED_STRING;
				} else if (Symbol.DELIMITER.isCharIn(delimiter, c, is_eof)) {
					r = State.PUSH;
				} else if (Symbol.NL.isCharIn(delimiter, c, is_eof)) {
					r = State.START;
				} else if (Symbol.EOF.isCharIn(delimiter, c, is_eof)) {
					r = State.FINAL;
				} else {
					r = State.ERROR;
				}
				break;

			case PUSH:
				if (is_relaxed && Symbol.RELAXED_NO_QUOTE_WS_DL.isCharIn(delimiter, c, is_eof)) {
					r = State.UNQUOTED_STRING;
				} else if (!is_relaxed && Symbol.NO_QUOTE_WS_DL.isCharIn(delimiter, c, is_eof)) {
					r = State.UNQUOTED_STRING;
				} else if (Symbol.QUOTE.isCharIn(delimiter, c, is_eof)) {
					r = State.QUOTED_STRING;
				} else if (Symbol.DELIMITER.isCharIn(delimiter, c, is_eof)) {
					r = State.PUSH;
				} else if (Symbol.NL.isCharIn(delimiter, c, is_eof)) {
					r = State.START;
				} else if (Symbol.EOF.isCharIn(delimiter, c, is_eof)) {
					r = State.FINAL;
				}else {
					r = State.ERROR;
				}
				break;

			case ERROR:
				if (Symbol.NL.isCharIn(delimiter, c, is_eof)) {
					r = State.START;
				} else if (Symbol.EOF.isCharIn(delimiter, c, is_eof)) {
					r = State.FINAL;
				} else {
					r = State.ERROR;
				}
				break;

			default:
				r = State.ERROR;
		}

		return r;
	}
}
