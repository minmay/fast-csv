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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * The main entry point into this application for parsing and writing comma separated values.
 */
public class CSV {

	/** The quote character. */
	public final static char QUOTE_CHAR ='"';

	/** Regular expression for when quotes are needed in a string csv. */
	public final static String REQUIRES_QUOTE_REG_EX = "^\\s.*|.*\".*|.*\n.*|.*\\s$";

	/** A reference to the a generic types parser that supports all of the java.lang data-types. */
	private final static TypeParser TYPE_PARSER = new TypeParser();

	/** A single line to be parsed for CSV. */
	private final String line;

	/** The channel that contains all of the lines of CSV. */
	private final ReadableByteChannel channel;

	/** The character set decoder to use when parsing the CSV lines text. */
	private final CharsetDecoder decoder;

	/** The internal buffer size used for reading data out of the nio channels.  The default is 1024. */
	private final Integer bufferSize;

	/** The delimiter to use for separating, usually this is a comma.  The default is a comma. */
	private final Character delimiter;

	/** This parameter is optional.  If not null, then a quoted string will be limited to this value.  Any lines that exceed a the limit are flagged as errors. */
	private final Integer quotedLengthLimit;

	/** The executor service to use when parsing into a queue. */
	private final ExecutorService executorService;

	/** The blocking queue to use when parsing into a queue. */
	private final BlockingQueue<CSVLine> queue;

	/** The callback to call when a CSV line is parsed. */
	private final PushLineCallback callback;

	/** The type parser to use when a CSV line is parsed and converted into an object. */
	private final TypeParser typeParser;

	/** The names of the field parsers to use in the type parser for the ith row of the csv line when a field is converted into an object. */
	private final String[] types;

	/** Configures the parser to be relaxed.  Relaxed mode allows a value to start with a tab or space. */
	private final boolean relaxed;

	private CSV(
			String line, ReadableByteChannel channel, CharsetDecoder decoder,
			Integer bufferSize, Character delimiter, Integer quotedLengthLimit,
			ExecutorService executorService, BlockingQueue<CSVLine> queue, PushLineCallback callback, TypeParser typeParser,
			String[] types, boolean relaxed
	) {
		this.line = line;
		this.channel = channel;
		this.decoder = decoder;
		this.bufferSize = bufferSize;
		this.delimiter = delimiter;
		this.quotedLengthLimit = quotedLengthLimit;
		this.executorService = executorService;
		this.queue = queue;
		this.callback = callback;
		this.typeParser = typeParser;
		this.types = types;
		this.relaxed = relaxed;
	}

	/**
	 * Creates a new builder.
	 *
	 * @return a builder.
	 */
	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder {

		/** A single line to be parsed for CSV. */
		private String line;

		/** The channel that contains all of the lines of CSV. */
		private ReadableByteChannel channel;

		/** The character set decoder to use when parsing the CSV lines text. */
		private CharsetDecoder decoder = Charset.defaultCharset().newDecoder();

		/** The internal buffer size used for reading data out of the nio channels.  The default is 1024. */
		private Integer bufferSize = 1024;

		/** The delimiter to use for separating, usually this is a comma.  The default is a comma. */
		private Character delimiter = ',';

		/** This parameter is optional.  If not null, then a quoted string will be limited to this value.  Any lines that exceed a the limit are flagged as errors. */
		private Integer quotedLengthLimit;

		/** The executor service to use when parsing into a queue. */
		private ExecutorService executorService;

		/** The blocking queue to use when parsing into a queue. */
		private BlockingQueue<CSVLine> queue;

		/** The callback to call when a CSV line is parsed. */
		private PushLineCallback callback;

		/** The type parser to use when a CSV line is parsed and converted into an object. */
		private TypeParser typeParser;

		/** The names of the field parsers to use in the type parser for the ith row of the csv line when a field is converted into an object. */
		private String[] types;

		/** Configures the parser to be relaxed.  Relaxed mode allows a value to start with a tab or space. */
		private boolean relaxed = false;

		/**
		 * Creates an instance of this builder.
		 */
		private Builder() {
			//empty
		}

		/**
		 * Assigns the single line that will be parsed.
		 *
		 * @param line The single line to parse.
		 * @return This Builder object.
		 * @see #parseLine()
		 * @see #parseLineToObjects()
		 */
		public Builder setLine(String line) {
			this.line = line;
			return this;
		}

		/**
		 * Asigns the file that contains all of the CSV lines.
		 *
		 * @param filename The filename containing all of the CSV lines.
		 * @return This Builder object.
		 * @throws FileNotFoundException When the file is not found.
		 * @see #parseToArray()
		 * @see #parseToQueue()
		 * @see #parseToCallback()
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
		 * @see #parseToArray()
		 * @see #parseToQueue()
		 * @see #parseToCallback()
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
		 * @see #parseToArray()
		 * @see #parseToQueue()
		 * @see #parseToCallback()
		 */
		public Builder setText(String text) {
			setInputStream(new ByteArrayInputStream(text.getBytes()));
			return this;
		}

		/**
		 * Assigns the input stream that will be parsed.
		 *
		 * @param inputStream The input stream that will be parsed.
		 * @return This CSV object.
		 * @see #parseToArray()
		 * @see #parseToQueue()
		 * @see #parseToCallback()
		 */
		public Builder setInputStream(InputStream inputStream) {
			this.channel  = Channels.newChannel(inputStream);
			return this;
		}

		/**
		 * Assigns the readable byte channel that will be parsed.
		 *
		 * @param channel The readable byte channel that will be parsed.
		 * @return This Builder object.
		 * @see #parseToArray()
		 * @see #parseToQueue()
		 * @see #parseToCallback()
		 */
		public Builder setChannel(ReadableByteChannel channel) {
			this.channel = channel;
			return this;
		}

		/**
		 * Assigns the input stream from the given classes resource.  This is shortcut for Class.class.getResourceAsStream(&quot;aResource&quot;).
		 *
		 * @param c The class containing the class path in which the resource will be searched.
		 * @param resource The resource to obtain as an input stream.
		 * @return This Builder object.
		 * @see #parseToArray()
		 * @see #parseToQueue()
		 * @see #parseToCallback()
		 * @see Class#getResourceAsStream(String)
		 */
		public Builder setResource(Class c, String resource) {
			setInputStream(c.getResourceAsStream(resource));
			return this;
		}


		/**
		 * Assigns the character set decoder used for decoding the text.
		 *
		 * @param decoder The character set decoder used for decoding the text.
		 * @return This Builder object.
		 */
		public Builder setDecoder(CharsetDecoder decoder) {
			this.decoder = decoder;
			return this;
		}

		/**
		 * Assigns the string name of the character set decoder used for decoding the text.
		 *
		 * @param charset The string name of the character set decoder used for decoding the text.
		 * @return This Builder object.
		 */
		public Builder setDecoder(String charset) {
			return setDecoder(Charset.forName(charset).newDecoder());
		}

		/**
		 * Assigns the internal buffer size used for reading data out of the nio channels.  The default is 1024.
		 *
		 * @param bufferSize The internal buffer size used for reading data out of the nio channels.
		 * @return This Builder object.
		 * @see #parseToArray()
		 * @see #parseToQueue()
		 * @see #parseToCallback()
		 */
		public Builder setBufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
			return this;
		}

		/**
		 * Assigns the delimiter use to separate fields between the separated values.
		 *
		 * @param delimiter the delimiter use to separate fields between the separated values.
		 * @return This Builder object.
		 * @see #parseToArray()
		 * @see #parseToQueue()
		 * @see #parseToCallback()
		 */
		public Builder setDelimiter(char delimiter) {
			this.delimiter = delimiter;
			return this;
		}

		/**
		 * Assigns a limit for the length of a quoted string.
		 * This prevents the parser from reading to the end of the file when
		 * the input does not terminate a quoted value.  If the limit is assigned null,
		 * then there is no limit imposed.  The default is null.
		 *
		 * @param quotedLengthLimit A limit for the length of a quoted string.  If null, then there is no limit.
		 * @return This Builder object.
		 * @see #parseToArray()
		 * @see #parseToQueue()
		 * @see #parseToCallback()
		 */
		public Builder setQuotedLengthLimit(Integer quotedLengthLimit) {
			this.quotedLengthLimit = quotedLengthLimit;
			return this;
		}

		/**
		 * Assigns the executor service to use when parsing into a queue.
		 * This parameter is optional, however, if not assigned, a singleThreadExecutor will be created.
		 *
		 * @param executorService The executor service to use when parsing into a queue.
		 * @return This Builder object.
		 * @see #parseToQueue()
		 */
		public Builder setExecutorService(ExecutorService executorService) {
			this.executorService = executorService;
			return this;
		}

		/**
		 * Assigns the blocking queue to use when parsing into a queue.
		 * This parameter is optional, however, if not assigned, a LinkedBlockingQueue will be created.
		 *
		 * @param queue The blocking queue to use when parsing into a queue.
		 * @return This Builder object.
		 * @see #parseToQueue()
		 */
		public Builder setQueue(BlockingQueue<CSVLine> queue) {
			this.queue = queue;
			return this;
		}

		/**
		 * This assigns the callback to call when a CSV line is parsed.
		 *
		 * @param callback The callback to use when a CSV line is parsed.
		 * @return This Builder object.
		 * @see #parseToCallback()
		 */
		public Builder setCallback(PushLineCallback callback) {
			this.callback = callback;
			return this;
		}

		/**
		 * This assigns the type parser to use when a CSV line is parsed and converted into an object.
		 * If none is assigned, then a default type parser that supports all primitives will be used.
		 * Type parsers are only used when the types array is assigned.
		 *
		 * @param typeParser The type parser to use when a CSV line is parsed and converted into an object.
		 * @return This Builder object.
		 * @see #parseToArray()
		 * @see #parseToQueue()
		 * @see #parseToCallback()
		 * @see #setTypes(String[])
		 * @see #setTypes(Class[])
		 * @see TypeParser
		 * @see FieldParser
		 */
		public Builder setTypeParser(TypeParser typeParser) {
			this.typeParser = typeParser;
			return this;
		}

		/**
		 * Builds the type parser that will be used.
		 *
		 * If none is assigned, then a default type parser that supports all primitives will be used.
		 * Type parsers are only used when the types array is assigned.
		 *
		 * @return The type parser that will be used.
		 * @see #parseToArray()
		 * @see #parseToQueue()
		 * @see #parseToCallback()
		 * @see #setTypes(String[])
		 * @see #setTypes(String[])
		 * @see TypeParser
		 * @see FieldParser
		 */
		private TypeParser buildTypeParser() {
			if (typeParser==null) {
				typeParser = TYPE_PARSER;
			}
			return typeParser;
		}

		/**
		 * Assigns the names of the field parsers to use in the type parser for the ith row of the csv line when a field is converted into an object.
		 *
		 * @param types The names of the field parsers to use in the type parser for the ith row of the csv line when a field is converted into an object.
		 * @return This Builder object.
		 * @see #parseToArray()
		 * @see #parseToQueue()
		 * @see #parseToCallback()
		 * @see #setTypes(String[])
		 * @see #setTypes(String[])
		 * @see TypeParser
		 * @see FieldParser
		 */
		public Builder setTypes(String[] types) {
			this.types = types;
			return this;
		}

		/**
		 * Assigns the class types that the ith row of the csv line should be parsed / converted to.
		 *
		 * @param types The class types that the ith row of the csv line should be parsed / converted to.
		 * @return This Builder object.
		 * @see #parseToArray()
		 * @see #parseToQueue()
		 * @see #parseToCallback()
		 * @see #setTypes(String[])
		 * @see #setTypes(String[])
		 * @see TypeParser
		 * @see FieldParser
		 */
		public Builder setTypes(Class[] types) {
			if (types == null) {
				this.types = null;
			} else if (this.types == null || this.types.length != types.length) {
				this.types = new String[types.length];
			}

			if (types != null) {
				for (int i = 0; i < types.length; i++) {
					String type = types[i].getName();
					this.types[i] = type;
				}
			}
			return this;
		}

		/**
		 * Configures the parser to be relaxed.  Relaxed mode allows a value to start with a tab or space.
		 *
		 * @param relaxed if true, the parser will be relaxed.
		 * @return This Builder object.
		 */
		public Builder setRelaxed(boolean relaxed) {
			this.relaxed = relaxed;
			return this;
		}

		public CSV build() {
			return new CSV(line, channel, decoder, bufferSize, delimiter, quotedLengthLimit, executorService, queue, callback, buildTypeParser(), types, relaxed);
		}
	}

	/**
	 * Parses a single line into comma separated values as a string array.
	 *
	 * This uses a , as the delimiter, and has no quote length limit.
	 *
	 * @param line The line to parse.
	 * @return The comma separated values as string array.
	 */
	public static String[] parseLine(String line) {
		return newBuilder().setLine(line).build().parseLine();
	}

	/**
	 * Parses a single line into comma separated values as a string array.
	 *
	 * This requires line to be configured.
	 * This optionally accepts delimiter, and quote length limit to be configured.
	 *
	 * @return The comma separated values as a string array.
	 */
	public String[] parseLine() {
		return parse(delimiter, line, quotedLengthLimit, relaxed);
	}

	/**
	 * Parses a single line into parsed objects.
	 *
	 * This requires line to be configured.
	 * This optionally accepts delimiter, quote length limit, types, and the types parser to be configured.
	 *
	 * @return The parsed objects.
	 */
	public Object[] parseLineToObjects() {
		String csv[] = parse(delimiter, line, quotedLengthLimit, relaxed);
		return parse(types, typeParser, csv);
	}

	/**
	 * Parses all of the text of multiple csv lines into an array of CSV lines.
	 *
	 * This uses a , as the delimiter, and has no quote length limit.
	 *
	 * @param text All of the CSV lines to parse.
	 * @return An array of CSV lines.
	 * @throws IOException When there is an IO error.
	 */
	public static CSVLine[] parseToArray(String text) throws IOException {
		return newBuilder().setText(text).build().parseToArray();
	}

	/**
	 * Parses all of the text of multiple csv lines into a blocking queue of CSV lines.
	 *
	 * This uses a , as the delimiter, and has no quote length limit.
	 *
	 * @param text All of the CSV lines to parse.
	 * @return A blocking queue of CSV lines.
	 * @throws ExecutionException When there is an error parsing the text.
	 */
	public static BlockingQueue<CSVLine> parseToQueue(String text) throws ExecutionException {
		return newBuilder().setText(text).build().parseToQueue();
	}

	/**
	 * Parses the input stream for CSV lines and returns them as an array.
	 *
	 * This uses a , as the delimiter, and has no quote length limit.
	 *
	 * @param is The input stream containing the CSV lines.
	 * @return An array of CSV lines.
	 * @throws IOException When there is an IO error.
	 */
	public static CSVLine[] parseToArray(InputStream is) throws IOException {
		return newBuilder().setInputStream(is).build().parseToArray();
	}

	/**
	 * Parses the input stream for CSV lines and returns them as an array.
	 *
	 * This uses a , as the delimiter, and has no quote length limit.
	 *
	 * @param is The input stream containing the CSV lines.
	 * @return An array of CSV lines.
	 * @throws ExecutionException When there is an error parsing the input stream.
	 */
	public static BlockingQueue<CSVLine> parseToQueue(InputStream is) throws ExecutionException {
		return newBuilder().setInputStream(is).build().parseToQueue();
	}

	/**
	 * Parses the input CSV lines to an array of CSV line objects.
	 *
	 * The input CSV lines can be assigned through assigning either text, file, resource, input stream, or channel, of which one is required.
	 *
	 * This optionally requires buffer size, delimiter, types, type parser, and quote length limit to be configured.
	 *
	 * @return An array of CSV lines.
	 * @throws IOException When there is an IO error.
	 */
	public CSVLine[] parseToArray() throws IOException {
		validateArguments();
		return parseToArray(channel, decoder, bufferSize, delimiter, types, typeParser, quotedLengthLimit, relaxed);
	}

	/**
	 * Parses the input CSV lines into a blocking queue of CSV lines objects.
	 *
	 * The input CSV lines can be assigned through assigning either text, file, resource, input stream, or channel, of which one is required.
	 *
	 * This optionally requires executor service, queue, buffer size, delimiter, types, type parser, and quote length limit to be configured.
	 *
	 * @return The blocking queue of CSV lines.
	 * @throws ExecutionException When there is an error parsing the CVS lines.
	 */
	public BlockingQueue<CSVLine> parseToQueue() throws ExecutionException {
		boolean executorServiceNotAssigned = executorService == null;
		ExecutorService es = executorServiceNotAssigned ? Executors.newSingleThreadExecutor() : executorService;
		validateArguments();
		try {
			return parseToQueue(es, queue, channel, decoder, bufferSize, delimiter, types, typeParser, quotedLengthLimit, relaxed);
		} finally {
			if (executorServiceNotAssigned) {
				es.shutdown();
			}
		}
	}

	/**
	 * Parses the input CSV lines and delegates to the configured callback what to do with each CSV line object.
	 *
	 * The input CSV lines can be assigned through assigning either text, file, resource, input stream, or channel, of which one is required.
	 *
	 * This optionally requires buffer size, delimiter, types, type parser, callback, and quote length limit to be configured.
	 *
	 * @throws IOException When there is an error parsing the CVS lines.
	 */
	public void parseToCallback() throws IOException {
		validateArguments();
		parse(channel, decoder, bufferSize, delimiter, types, typeParser, callback, quotedLengthLimit, relaxed);
	}

	public void parseToConsumer(Consumer<CSVLine> line) throws IOException {

	}

	/**
	 * Validates the channel and buffer size.
	 */
	private void validateArguments() {
		if (channel==null) {
			throw new IllegalArgumentException("A channel is required for parsing.");
		}
		if (bufferSize <= 0) {
			throw new IllegalArgumentException("The buffer size must be a positive integer.");
		}
	}

	/**
	 * Parses and converts a comma separated values line.
	 *
	 * @param delimiter The delimiter to use for separating, usually this is a comma.
	 * @param types An array containing the id of the type parser to use for the ith element.  Note, primitives and java.lang objects are already supported.
	 * @param parser The data-structure containing the type parsers.
	 * @param line The line to parse.
	 * @param quoted_length_limit This parameter is optional.  If not null, then a quoted string will be limited to this value.  Any lines that exceed a the limit are flagged as errors.
	 * @return An array of strongly typed converted objects.
	 */
	private static Object[] parse(char delimiter, String[] types, TypeParser parser , String line, Integer quoted_length_limit, boolean relaxed) {

		String csv[] = parse(delimiter, line, quoted_length_limit, relaxed);
		return parse(types, parser, csv);
	}

	/**
	 * Converts each value in the csv array into an object type.
	 *
	 * @param types An array containing the id of the type parser to use for the ith element.  Note, primitives and java.lang objects are already supported.
	 * @param parser The data-structure containing the type parsers.
	 * @param csv The array of comma separated values, each value is the ith element that will be parsed with the type parser specified by the types[] array.
	 * @return An array of strongly typed converted objects.
	 */
	private static Object[] parse(String[] types, TypeParser parser, String[] csv) {

		Object[] values = null;
		if (types != null && parser != null && csv != null) {
			values = new Object[csv.length];
			for (int i=0;i<csv.length;i++) {
				String s = csv[i];
				String type = i < types.length ? types[i] : null;
				Object v = null;
				try {
					v = type != null ? parser.parse(type, s) : s;
					values[i] = v;
				} catch (RuntimeException e) {
					System.err.println("Error parsing type = " + type + " csv[" + i + "] = " + csv[i]);
					throw e;
				}
			}
		}

		return values;
	}

	/**
	 * Parses and converts a comma separated values line.  This will use the default types parser that comes with support for primitives and java.lang objects.
	 *
	 * @param delimiter The delimiter to use for separating, usually this is a comma.
	 * @param types An array containing the id of the type parser to use for the ith element.  Note, primitives and java.lang objects are already supported.
	 * @param line The line to parse.
	 * @param quote_length_limit This parameter is optional.  If not null, then a quoted string will be limited to this value.  Any lines that exceed a the limit are flagged as errors.
	 * @return An array of strongly typed converted objects.
	 */
	private static Object[] parse(char delimiter, String[] types, String line, Integer quote_length_limit, boolean relaxed) {
		return parse(delimiter, types, TYPE_PARSER, line, quote_length_limit, relaxed);
	}

	/**
	 * Parses the comma separated values line into a String array of values.
	 *
	 * @param delimiter The delimiter, usually a comma.
	 * @param line The line to parse.
	 * @param quote_length_limit This parameter is optional.  If not null, then a quoted string will be limited to this value.  Any lines that exceed a the limit are flagged as errors.
	 * @return Comma separated values.
	 */
	private static String[] parse(char delimiter, String line, Integer quote_length_limit, boolean is_relaxed) {

		CSVLine r;
		State current = State.START;
		CharBuffer cb = CharBuffer.allocate(line.length());

		cb.append(line);
		cb.flip();

		ParseStateValues psv = new ParseStateValues(delimiter, null, null, new SingleLinePush(), quote_length_limit, is_relaxed);
		psv.current = current;
		psv.in = cb;
		psv.current = CSV.parse(psv);

		if (psv.current != State.START) {
			pushLine(psv, State.ERROR == psv.current);
		}
		psv.isEOF = true;
		psv.current = psv.current.transition(psv, -1);

		r = ((SingleLinePush)psv.callback).getInfo();
		return r == null ? new String[]{} : r.getCsvs();
	}

	/**
	 * Parses an input stream for comma separated values into an array of CSV lines.
	 *
	 * @param channel The channel that contains all of the lines of CSV.
	 * @param decoder The character set decoder to use when parsing the CSV lines text.
	 * @param bufferSize The internal buffer size used for reading data out of the nio channels.
	 * @param delimiter The delimiter to use for separating, usually this is a comma.
	 * @param types The names of the field parsers to use in the type parser for the ith row of the csv line when a field is converted into an object.
	 * @param typeParsers The type parser to use when a CSV line is parsed and converted into an object.
	 * @param quote_length_limit This parameter is optional.  If not null, then a quoted string will be limited to this value.  Any lines that exceed a the limit are flagged as errors.
	 * @return An array of CSV lines.
	 * @throws IOException When there is an IO error.
	 */
	private static CSVLine[] parseToArray(final ReadableByteChannel channel, CharsetDecoder decoder, int bufferSize, final char delimiter, String types[], TypeParser typeParsers, final Integer quote_length_limit, boolean relaxed) throws IOException {
		final List<CSVLine> list = new ArrayList<CSVLine>();
		parse(channel, decoder, bufferSize, delimiter, types, typeParsers, new PushLineCallback() {
			public void pushLine(CSVLine info) {
				list.add(info);
			}
		}, quote_length_limit, relaxed);
		CSVLine[] lines = new CSVLine[list.size()];
		return list.toArray(lines);
	}

	/**
	 * Parses an input stream for comma separated values, by launching a thread into an executor service, and populating a blocking queue with csv lines.
	 *
	 * @param executor The executor service that the parsing thread will be submitted to.
	 * @param queue The blocking queue to use when parsing into a queue.
	 * @param channel The channel that contains all of the lines of CSV.
	 * @param decoder The character set decoder to use when parsing the CSV lines text.
	 * @param bufferSize The internal buffer size used for reading data out of the nio channels.
	 * @param delimiter The delimiter to use for separating, usually this is a comma.
	 * @param types The names of the field parsers to use in the type parser for the ith row of the csv line when a field is converted into an object.
	 * @param typeParsers The type parser to use when a CSV line is parsed and converted into an object.
	 * @param quote_length_limit This parameter is optional.  If not null, then a quoted string will be limited to this value.  Any lines that exceed a the limit are flagged as errors.
	 * @return A blocking queue with CSV lines.
	 * @throws ExecutionException When there in the the parsing thread.
	 */
	private static BlockingQueue<CSVLine> parseToQueue(ExecutorService executor, BlockingQueue<CSVLine> queue, final ReadableByteChannel channel, final CharsetDecoder decoder, final int bufferSize, final char delimiter, final String types[], final TypeParser typeParsers, final Integer quote_length_limit, final boolean relaxed) throws ExecutionException {

		final BlockingQueue<CSVLine> q = queue == null ? new LinkedBlockingQueue<CSVLine>() : queue;

		Callable<Boolean> work = new Callable<Boolean>() {

			/**
			 * Computes a result, or throws an exception if unable to do so.
			 *
			 * @return computed result
			 * @throws Exception if unable to compute a result
			 */
			public Boolean call() throws Exception {
				parse(channel, decoder, bufferSize, delimiter, types, typeParsers, new PushLineCallback() {
					public void pushLine(CSVLine info) {
						q.offer(info);
					}
				}, quote_length_limit, relaxed);

				try { channel.close(); } catch (IOException e) { }
				return true;
			}
		};

		Future<Boolean> future = executor.submit(work);

		return q;
	}

	/**
	 * Parses a readable byte channel for comma separated values, and delegates to a callback whenever a CSV line is encountered.
	 *
	 * @param channel The readable byte channel containing all of the CSV data.
	 * @param decoder The character set decoder to use when parsing the CSV lines text.
	 * @param bufferSize The buffer size to use for the NIO buffer that will read from the input stream.
	 * @param delimiter The delimiter to use for separating, usually this is a comma.
	 * @param types The names of the field parsers to use in the type parser for the ith row of the csv line when a field is converted into an object.
	 * @param typeParsers The type parser to use when a CSV line is parsed and converted into an object.
	 * @param callback The callback that will handle the business logic to perform when a CSV line is encountered.
	 * @param quote_length_limit This parameter is optional.  If not null, then a quoted string will be limited to this value.  Any lines that exceed a the limit are flagged as errors.
	 * @throws IOException When there is an io error.
	 */
	private static void parse(ReadableByteChannel channel, CharsetDecoder decoder, int bufferSize, char delimiter, String types[], TypeParser typeParsers, PushLineCallback callback, Integer quote_length_limit, boolean relaxed) throws IOException {

		ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
		CharBuffer charBuffer = CharBuffer.allocate(bufferSize);

		ParseStateValues psv = new ParseStateValues(delimiter, types, typeParsers, callback, quote_length_limit, relaxed);

		while ((channel.read(buffer)) != -1 ) {
			buffer.flip();
			decoder.decode(buffer, charBuffer, false);
			charBuffer.flip();

			psv.in = charBuffer.asReadOnlyBuffer();
			psv.current = CSV.parse(psv);

			buffer.clear();
			charBuffer.clear();
		}

//		while (buffer.hasRemaining()) {
//			buffer.flip();
//			decoder.decode(buffer, charBuffer, false);
//			charBuffer.flip();
//
//			CharBuffer readOnlyCopy = charBuffer.asReadOnlyBuffer();
//			psv.in = readOnlyCopy;
//			psv.current = CSV.parse(psv);
//		}


		if (psv.current != State.START) {
			pushLine(psv, State.ERROR == psv.current);
		}
		psv.isEOF = true;
		psv.current = psv.current.transition(psv, -1);

		if (psv.current!= State.FINAL) {
			System.err.println(new StringBuilder().append("Did not end in ").append(State.FINAL).append(" state.  Ended in ").append(psv.current).append(" state.").toString());
		}
	}

	/**
	 * Implements the business logic used for parsing the contents of a character buffer for extracting valid CSV lines.
	 *
	 * @param psv A data-structure for keeping track of parse state.
	 * @return The next transition state.
	 */
	private static State parse(final ParseStateValues psv) {

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
					pushLine(psv, isError);
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
					pushLine(psv, isError);
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
	private static void pushLine(ParseStateValues psv, boolean error) {

//		if (psv.getCsv() != null) {
			psv.addToCsvs(psv.getCsv());
			psv.resetCsv();																								//clears the current string
//		}

		String[] csvs_a = psv.getCsvs();

		if (csvs_a != null) {
			Object[] values =  null;                                                                                        //converts the strings into objects [type parsing]
			try {
				values = parse(psv.types, psv.typeParsers, csvs_a);
			} catch (Exception e) {
				System.err.println("error during parsing of " + psv.getRaw() + ".  " + e);
			}

			CSVLine csvLine = new CSVLine(psv.delimiter, error, psv.getRaw(), csvs_a, values);
			if (psv.callback != null) {
				psv.callback.pushLine(csvLine);                                                                                 //output list of strings
			}
		}
		psv.resetCsvs();
		psv.resetRaw();
	}


	/**
	 * This method is called every time it has been determined that a CSV Line is ready to the list of csv lines.  It will
	 * call the callback that will determine what to do with the line.
	 *
	 * @param delimiter The delimiter to use for separating, usually this is a comma.
	 * @param error A flag indicating that error has been found in the current csv line.
	 * @param csvs The comma separated values that belong to a line that are being built.
	 * @param csv The current comma separated that is being built.
	 * @param raw The raw, un-parsed csv line that is being built.
	 * @param types The names of the field parsers to use in the type parser for the ith row of the csv line when a field is converted into an object.
	 * @param typeParsers The type parser to use when a CSV line is parsed and converted into an object.
	 * @param callback The callback that will handle the business logic to perform when a CSV line is encountered.
	 */
	@Deprecated
	private static void pushLine(char delimiter, boolean error, List<String> csvs, StringBuilder csv, StringBuilder raw, String types[], TypeParser typeParsers, PushLineCallback callback) {
		csvs.add(csv.toString());
		csv.delete(0, csv.length());                                                                                    //clears the current string

		String[] csvs_a = csvs.toArray(new String[csvs.size()]);

		Object[] values =  null;                                                                                        //converts the strings into objects [type parsing]
		try {
			values = parse(types, typeParsers, csvs_a);
		} catch (Exception e) {
			System.err.println("error during parsing of " + raw + ".  " + e);
		}

		CSVLine csvLine = new CSVLine(delimiter, error, raw.toString(), csvs_a, values);
		if (callback != null) {
			callback.pushLine(csvLine);                                                                                 //output list of strings
		}
		csvs.clear();                                                                                                   //clear current list
		raw.delete(0,raw.length());
	}

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

	/**
	 * Reads an input stream into a string.
	 *
	 * @param is The input stream to read.
	 * @param decoder The character set decoder to use when parsing the CSV lines text.
	 * @return A string containing the contents of the input stream.
	 * @throws IOException When there is an IO error.
	 */
	public static String readInputStreamAsString(InputStream is, CharsetDecoder decoder) throws IOException{
		StringBuilder builder = new StringBuilder();
		ReadableByteChannel channel = null;
		try {
			channel = Channels.newChannel(is);

			int capacity = 1024;
			ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);
			CharBuffer charBuffer = CharBuffer.allocate(capacity);


			while ((channel.read(buffer)) != -1) {
				buffer.flip();
				decoder.decode(buffer, charBuffer, false);
				charBuffer.flip();

				builder.append(charBuffer.toString());

				buffer.clear();
				charBuffer.clear();
			}

		} finally {
			if (channel!=null) {
				try { channel.close(); } catch(IOException ie) { /* let's get out of here */}
			}
			if (is!=null) {
				try { is.close(); } catch(IOException ie) {/* let's get out of here */}
			}
		}

		return builder.toString();
	}

}
