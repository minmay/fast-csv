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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.CharsetDecoder;
import java.util.Spliterator;
import java.util.function.Consumer;

public class CSVLineSpliterator implements Spliterator<CSVLine> {

    /** The quote character. */
    public final static char QUOTE_CHAR ='"';

    /** The channel that contains all of the lines of CSV. */
    private final ReadableByteChannel channel;

    /** The character set decoder to use when parsing the CSV lines text. */
    private final CharsetDecoder decoder;

    /** The internal buffer size used for reading data out of the nio channels.  The default is 1024. */
    private final int bufferSize;

    /** The delimiter to use for separating, usually this is a comma.  The default is a comma. */
    private final char delimiter;

    /** This parameter is optional.  If not null, then a quoted string will be limited to this value.  Any lines that exceed a the limit are flagged as errors. */
    private final Integer quotedLengthLimit;

    /** Configures the parser to be relaxed.  Relaxed mode allows a value to start with a tab or space. */
    private final boolean relaxed;

    private final ParseStateValues psv;
    private final ByteBuffer buffer;
    private final CharBuffer charBuffer;

    public CSVLineSpliterator(
            ReadableByteChannel channel,
            CharsetDecoder decoder,
            int bufferSize,
            char delimiter,
            Integer quotedLengthLimit,
            boolean relaxed
    ) {
        this.channel = channel;
        this.decoder = decoder;
        this.bufferSize = bufferSize;
        this.delimiter = delimiter;
        this.quotedLengthLimit = quotedLengthLimit;
        this.relaxed = relaxed;

        this.buffer = ByteBuffer.allocateDirect(bufferSize);
        this.charBuffer = CharBuffer.allocate(bufferSize);
        this.psv = new ParseStateValues(delimiter, null, null, quotedLengthLimit, relaxed);
    }

    @Override
    public boolean tryAdvance(Consumer<? super CSVLine> action) {
        boolean hasRemainingElements;

        try {
            int read;
            if ((read = channel.read(buffer)) != -1 ) {
                if (read > 0) {
                    buffer.flip();
                    decoder.decode(buffer, charBuffer, false);
                    charBuffer.flip();

                    psv.in = charBuffer.asReadOnlyBuffer();
                    psv.current = parse(psv, action);

                    buffer.clear();
                    charBuffer.clear();
                }
                hasRemainingElements = true;
            } else {
                if (psv.current != State.START) {
                    accept(psv, action, State.ERROR == psv.current);
                }

                psv.isEOF = true;
                psv.current = psv.current.transition(psv, -1);

                if (psv.current!= State.FINAL) {
                    System.err.println(new StringBuilder().append("Did not end in ").append(State.FINAL).append(" state.  Ended in ").append(psv.current).append(" state.").toString());
                }
                hasRemainingElements = false;       //channel has reached end-of-stream
            }
        } catch (IOException e) {
            hasRemainingElements = false;
        }

        return hasRemainingElements;	// returns false if no remaining elements existed upon entry of this method, else true.
    }

    @Override
    public Spliterator<CSVLine> trySplit() {
        return null;					//returns null because a CSV file cannot be split
    }

    @Override
    public long estimateSize() {
        return Long.MIN_VALUE;  		// returns Long.MIN_VALUE because this size is unknown or too expensive to compute
    }

    @Override
    public int characteristics() {
        return 0;
    }

    /**
     * Implements the business logic used for parsing the contents of a character buffer for extracting valid CSV lines.
     *
     * @param psv A data-structure for keeping track of parse state.
     * @param action
     * @return The next transition state.
     */
    private State parse(final ParseStateValues psv, Consumer<? super CSVLine> action) {

        boolean isError = false;
        char c;
        State prev;

        loop:
        while(psv.in.hasRemaining()) {

            int as_int = psv.isEOF ? -1 : (int) psv.in.get();
            c = (char) as_int;

            prev = psv.current;
            psv.current = psv.current.transition(psv, as_int);

            psv.is_prev_char_cr = c == '\r';

            if (!psv.isEOF && psv.current!=State.START) {
                psv.appendToRaw(c);
            }

            switch(psv.current) {
                case START:
                    accept(psv, action, isError);
                    isError = false;
                    break;
                case CR:
                    psv.current = State.START;
                    break;
                case UNQUOTED_STRING:
                    psv.appendToCsv(c);
                    break;
                case QUOTED_STRING:
                    if (c != QUOTE_CHAR || State.END_QUOTE==prev && c==QUOTE_CHAR) {
                        psv.appendToCsv(c);
                    }
                    break;
                case END_QUOTE:
                    break;
                case PUSH:
                    psv.addToCsvs(psv.getCsv());
                    psv.resetCsv();																					//clears the current string
                    break;
                case FINAL:
                    accept(psv, action, isError);
                    break loop;
                case ERROR:
                    if (!psv.isEOF) {
                        psv.appendToCsv(c);
                    }
                    isError = true;
                    break;
            }
        }
        return psv.current;
    }

    /**
     * This method is called every time it has been determined that a CSV Line is ready to the list of csv lines.  It will
     * call the callback that will determine what to do with the line.
     *
     * @param error A flag indicating that error has been found in the current csv line.
     */
    private void accept(ParseStateValues psv, Consumer<? super CSVLine> action, boolean error) {
        psv.addToCsvs(psv.getCsv());
        psv.resetCsv();																								    //clears the current string

        final String[] csvs_a = psv.getCsvs();

        if (csvs_a != null) {
            final CSVLine csvLine = new CSVLine(psv.delimiter, error, psv.getRaw(), csvs_a);
            if (action != null) {
                action.accept(csvLine);                                                                           //output list of strings
            }
        }
        psv.resetCsvs();
        psv.resetRaw();
    }
}
