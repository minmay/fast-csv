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

import java.util.*;
import java.util.function.Function;

/**
 * A data-structure that contains parsers for a variety of types.
 */
public class TypeParser implements Function<CSVLine, Object[]> {

	/**
	 * A map of types and parsers.
	 * The key is the class that the parser will valueOf.
	 * The value is the actual parser for given type.
	 */
	private final Map<String, FieldParser> typeParserMap = new HashMap<>();

	/** The list of types in parse order. */
	private final List<String> types = new ArrayList<>();

	/**
	 * Creates an instance of the type parser, and initializes
	 * it with support for all of the java.lang types.
	 */
	public TypeParser() {
		typeParserMap.put(Boolean.class.getName(), new BooleanParser(false));
		typeParserMap.put(Byte.class.getName(), new ByteParser(false));
		typeParserMap.put(Character.class.getName(), new CharacterParser(false));
		typeParserMap.put(Double.class.getName(), new DoubleParser(false));
		typeParserMap.put(Float.class.getName(), new FloatParser(false));
		typeParserMap.put(Integer.class.getName(), new IntegerParser(false));
		typeParserMap.put(Long.class.getName(), new LongParser(false));
		typeParserMap.put(Short.class.getName(), new ShortParser(false));
		typeParserMap.put(String.class.getName(), new StringParser());

		typeParserMap.put(boolean.class.getName(), new BooleanParser(true));
		typeParserMap.put(byte.class.getName(), new ByteParser(true));
		typeParserMap.put(char.class.getName(), new CharacterParser(true));
		typeParserMap.put(double.class.getName(), new DoubleParser(true));
		typeParserMap.put(float.class.getName(), new FloatParser(true));
		typeParserMap.put(int.class.getName(), new IntegerParser(true));
		typeParserMap.put(long.class.getName(), new LongParser(true));
		typeParserMap.put(short.class.getName(), new ShortParser(true));
	}

	/**
	 * Parses the string with the given classes parser.
	 *
	 * @param type The class type that will be parsed.
	 * @param v The value to valueOf.
	 * @return The class instance value that was parsed.
	 * @throws NullPointerException When the parser for this type is not supported.
	 *
	 */
	public Object parse(Class type, String v) {
		return parse(type.getName(), v);
	}

	/**
	 * Parses the string with the given classes parser.
	 *
	 * @param parserId The class type that will be parsed.
	 * @param v The value to valueOf.
	 * @return The class instance value that was parsed.
	 * @throws NullPointerException When the parser for this type is not supported.
	 *
	 */
	public Object parse(String parserId, String v) {
		return typeParserMap.get(parserId).valueOf(v);
	}

	/**
	 * Adds a value parser by id in order to this type parser.
	 *
	 * @param parserId The id of parser to use.
	 * @return this TypeParser.
	 */
	public TypeParser add(String parserId) {
		types.add(parserId);
		return this;
	}

	/**
	 * Adds a value parser by type name in order to this type parser.
	 *
	 * @param type The type, which name is associated with a parser.
	 * @return this TypeParser.
	 */
	public TypeParser add(Class type) {
		return add(type.getName());
	}

	/**
	 * Adds many value parsers by type name in order to this type parser.
	 *
	 * @param types The types, in which each name is associated with a parser.
	 * @return this TypeParser.
	 */
	public TypeParser add(Class ... types) {
		if (types != null) {
			for (Class type : types) {
				add(type);
			}
		}
		return this;
	}

	/**
	 * Adds a field parser for the given type.
	 *
	 * @param type The type of class that this value parser supports.
	 * @param p the value parser implementation.
	 */
	public TypeParser addFieldParser(Class type, FieldParser p) {
		addFieldParser(type.getName(), p);
		return this;
	}

	/**
	 * Adds a field parser for the given type.
	 *
	 * @param parserId The type of class that this field parser supports.
	 * @param p the field parser implementation.
	 */
	public TypeParser addFieldParser(String parserId, FieldParser p) {
		typeParserMap.put(parserId, p);
		return this;
	}

	/**
	 * Creates a new instance of a TypeParser.
	 *
	 * @return a new instance.
	 */
	public static TypeParser newInstance() {
		return new TypeParser();
	}

	@Override
	public Object[] apply(CSVLine line) {
		final String[] csv = line.getCsvs();
		final Object[] values = new Object[csv.length];

		for (int i = 0; i < csv.length; i++) {
			final String s = csv[i];
			final String type = i < types.size() ? types.get(i) : null;
			try {
				final Object v = type != null ? parse(type, s) : s;
				values[i] = v;
			} catch (RuntimeException e) {
				System.err.println("Error parsing type = " + type + " csv[" + i + "] = " + csv[i]);
				throw e;
			}
		}

		return values;
	}

	private class BooleanParser extends LangFieldParser<Boolean> {
		private BooleanParser(boolean primitive) {
			super(primitive);
		}

		@Override
		public  Boolean valueOf(String v) {
			return !isPrimitive && NULL.equals(v) ? null : Boolean.valueOf(v);
		}
	}

	private class ByteParser extends LangFieldParser<Byte> {
		private ByteParser(boolean primitive) {
			super(primitive);
		}

		@Override
		public Byte valueOf(String v) {
			return !isPrimitive && NULL.equals(v) ? null : Byte.valueOf(v);
		}
	}

	private class CharacterParser extends LangFieldParser<Character> {
		private CharacterParser(boolean primitive) {
			super(primitive);
		}

		@Override
		public Character valueOf(String v) {
			if (v.length()!=1) {
				throw new IllegalArgumentException("Expected character.");
			}
			return !isPrimitive && NULL.equals(v) ? null : Character.valueOf(v.charAt(0));
		}
	}

	private class DoubleParser extends LangFieldParser<Double> {
		private DoubleParser(boolean primitive) {
			super(primitive);
		}

		@Override
		public Double valueOf(String v) {
			return !isPrimitive && NULL.equals(v) ? null : Double.valueOf(v);
		}
	}

	private class FloatParser extends LangFieldParser<Float> {
		private FloatParser(boolean primitive) {
			super(primitive);
		}

		@Override
		public Float valueOf(String v) {
			return !isPrimitive && NULL.equals(v) ? null : Float.valueOf(v);
		}
	}

	private class IntegerParser extends LangFieldParser<Integer> {
		private IntegerParser(boolean primitive) {
			super(primitive);
		}

		@Override
		public Integer valueOf(String v) {
			return !isPrimitive && NULL.equals(v) ? null : Integer.valueOf(v);

		}
	}

	private class LongParser extends LangFieldParser<Long> {
		private LongParser(boolean primitive) {
			super(primitive);
		}

		@Override
		public Long valueOf(String v) {
			return !isPrimitive && NULL.equals(v) ? null : Long.valueOf(v);
		}
	}

	private class ShortParser extends LangFieldParser<Short> {
		private ShortParser(boolean primitive) {
			super(primitive);
		}
		@Override
		public Short valueOf(String v) {
			return !isPrimitive && NULL.equals(v) ? null : Short.valueOf(v);
		}
	}

	private class StringParser extends FieldParser<String> {
		@Override
		public String valueOf(String v) {
			return v;
		}
	}
}
