package csv.library;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FastCSVReader {

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

    public FastCSVReader(ReadableByteChannel channel, CharsetDecoder decoder, int bufferSize, char delimiter, Integer quotedLengthLimit, boolean relaxed) {
        this.channel = channel;
        this.decoder = decoder;
        this.bufferSize = bufferSize;
        this.delimiter = delimiter;
        this.quotedLengthLimit = quotedLengthLimit;
        this.relaxed = relaxed;
    }

    public Stream<CSVLine> stream() {
        validateArguments();
        return StreamSupport.stream(new CSVLineSpliterator(channel, decoder, bufferSize, delimiter, quotedLengthLimit, relaxed), false);
    }

    /**
     * Validates the channel and buffer size.
     */
    private void validateArguments() {
        if (channel==null) {
            throw new NullPointerException("A channel is required for parsing.");
        }
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("The buffer size must be a positive integer.");
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        /** The channel that contains all of the lines of CSV. */
        private ReadableByteChannel channel;

        /** The character set decoder to use when parsing the CSV lines text. */
        private CharsetDecoder decoder = Charset.defaultCharset().newDecoder();

        /** The internal buffer size used for reading data out of the nio channels.  The default is 1024. */
        private int bufferSize = 1024;

        /** The delimiter to use for separating, usually this is a comma.  The default is a comma. */
        private char delimiter = ',';

        /** This parameter is optional.  If not null, then a quoted string will be limited to this value.  Any lines that exceed a the limit are flagged as errors. */
        private Integer quotedLengthLimit;

        /** Configures the parser to be relaxed.  Relaxed mode allows a value to start with a tab or space. */
        private boolean relaxed = false;

        /**
         * Asigns the file that contains all of the CSV lines.
         *
         * @param filename The filename containing all of the CSV lines.
         * @return This Builder object.
         * @throws FileNotFoundException When the file is not found.
         */
        public Builder setFile(String filename) throws FileNotFoundException {
            setInputStream(new FileInputStream(filename));
            return this;
        }

        /**
         * Assigns the file contains all of the CSV lines.
         *
         * @param file The file containing all of the CSV lines.
         * @return This Builder object.
         * @throws FileNotFoundException When the file is not found.
         */
        public Builder setFile(File file) throws FileNotFoundException {
            setInputStream(new FileInputStream(file));
            return this;
        }

        /**
         * Assigns the all of the lines of text that will be parsed.
         *
         * @param text All of the lines of text that will be parsed.
         * @return This Builder object.
         */
        public Builder setText(String text) {
            setInputStream(new ByteArrayInputStream(text.getBytes()));
            return this;
        }

        /**
         * Assigns the input stream from the given classes resource.  This is shortcut for Class.class.getResourceAsStream(&quot;aResource&quot;).
         *
         * @param c The class containing the class path in which the resource will be searched.
         * @param resource The resource to obtain as an input stream.
         * @return This Builder object.
         * @see Class#getResourceAsStream(String)
         */
        public Builder setResource(Class c, String resource) {
            setInputStream(c.getResourceAsStream(resource));
            return this;
        }

        /**
         * Assigns the input stream that will be parsed.
         *
         * @param inputStream The input stream that will be parsed.
         * @return This CSV object.
         */
        public Builder setInputStream(InputStream inputStream) {
            this.channel  = Channels.newChannel(inputStream);
            return this;
        }

        public Builder setChannel(ReadableByteChannel channel) {
            this.channel = channel;
            return this;
        }

        public Builder setDecoder(CharsetDecoder decoder) {
            this.decoder = decoder;
            return this;
        }

        public Builder setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder setDelimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder setQuotedLengthLimit(Integer quotedLengthLimit) {
            this.quotedLengthLimit = quotedLengthLimit;
            return this;
        }

        public Builder setRelaxed(boolean relaxed) {
            this.relaxed = relaxed;
            return this;
        }

        public FastCSVReader build() {
            return new FastCSVReader(channel, decoder, bufferSize, delimiter, quotedLengthLimit, relaxed);
        }
    }
}
