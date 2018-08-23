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

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DelimitTest {

//    @Test
//    public void testEOF () {
//        //State next = State.END_QUOTE.transition(',', -1, false, false, false, 2, 2);
//        assertEquals(next, State.FINAL);
//    }


	@Test
	public void testDelimit() {
		String[] r = CSV.parseLine("abc,def,hij");
		for (String v:r) {
			System.out.println(v);
		}
		System.out.println("-------");
		r = CSV.parseLine("\"abc\",d\tef,hij");
		for (String v:r) {
			System.out.println(v);
		}
		System.out.println("-------");
		r = CSV.parseLine("\"a,bc\",d\tef,hij");
		for (String v:r) {
			System.out.println(v);
		}
		System.out.println("-------");
		r = CSV.parseLine(",,");
		for (String v:r) {
			System.out.println(v);
		}

		System.out.println("-------");
		r = CSV.parseLine("\"a\"\"bc\",,hij");
		for (String v:r) {
			System.out.println(v);
		}
		System.out.println("-------");
		r = CSV.parseLine("");
		if (r != null) {
			for (String v:r) {
				System.out.println(v);
			}
		}
		System.out.println("-------");
		r = CSV.parseLine("\"abc\ndef\",\"hij\nklm\"");
		for (String v:r) {
			System.out.println(v);
		}
		System.out.println("-------");
	}

	@Test
	public void testDelimit2() throws InterruptedException, ExecutionException {

		StringBuilder cb = new StringBuilder();
		BlockingQueue<CSVLine> q = new LinkedBlockingQueue<CSVLine>();
		BlockingQueuePusher p = new BlockingQueuePusher(q);
		cb.append("abc,def,hij\n");
		cb.append("abc,\"z\"\"ef\",hij\n");
		cb.append("\"bbc\",d\tef,hij\n");
		cb.append("\"c,bc\",d\tef,hij\n");
		cb.append(",,\n");
		cb.append("\"d\"\"bc\",,hij\n");
		cb.append("\n");
		cb.append("\"ebc\ndef\",\"hij\nklm\"");

		CSV.newBuilder().setText(cb.toString()).setQueue(q).build().parseToQueue();

		CSVLine r;
		while ((r = q.poll())!=null) {
			for (String rv:r.getCsvs()) {
				System.out.println(rv);
			}
			System.out.println("-------");
		}
	}

	@Test
	public void testTabDelimit() {

		String[] r = CSV.newBuilder().setDelimiter('\t').setLine("abc\tdef\thij").build().parseLine();
		for (String v:r) {
			System.out.println(v);
		}
		System.out.println("-------");
		r = CSV.newBuilder().setDelimiter('\t').setLine("\"abc\"\td,ef\thij").build().parseLine();
		for (String v:r) {
			System.out.println(v);
		}
		System.out.println("-------");
		r = CSV.newBuilder().setDelimiter('\t').setLine("\"a\tbc\"\td,ef\thij").build().parseLine();
		for (String v:r) {
			System.out.println(v);
		}
		System.out.println("-------");
		r = CSV.newBuilder().setDelimiter('\t').setLine("\t\t").build().parseLine();
		for (String v:r) {
			System.out.println(v);
		}

		System.out.println("-------");
		r = CSV.newBuilder().setDelimiter('\t').setLine("\"a\"\"bc\"\t\thij").build().parseLine();
		for (String v:r) {
			System.out.println(v);
		}
		System.out.println("-------");
		r = CSV.newBuilder().setDelimiter('\t').setLine("").build().parseLine();
		if (r != null) {
			for (String v:r) {
				System.out.println(v);
			}
		}
		System.out.println("-------");
	}

	@Test
	public void testValueParsing() {

		Object[] expected = {true, (byte) 1, 123, 123.456f, 123.456d, 123L, 'a', "Hey man!!!"};
		Class[] types = {boolean.class, byte.class, int.class, float.class, double.class, long.class, char.class, String.class};
		String line = CSV.delimit(',', expected);
		Object[] actual = CSV.newBuilder().setTypes(types).setLine(line).build().parseLineToObjects();

		assertArrayEquals(actual, expected);
	}

	@Test
	public void testBadCsv() throws IOException {

		for (CSVLine line : CSV.newBuilder().setResource(DelimitTest.class, "/bad.csv").setQuotedLengthLimit(3).build().parseToArray()) {
			if (line.isError()) {
				System.out.println("error:  *" + line.getRaw() + "*");
			} else {
				System.out.println(line.getRaw());
			}
		}

		for (CSVLine line : CSV.newBuilder().setResource(DelimitTest.class, "/bad.csv").build().parseToArray()) {
			if (line.isError()) {
				System.out.println("error:  *" + line.getRaw() + "*");
			} else {
				System.out.println(line.getRaw());
			}
		}
	}

	@Test
	public void testGoodCsv() throws IOException {
		for (CSVLine line: CSV.newBuilder().setResource(DelimitTest.class, "/good.csv").build().parseToArray()) {
			System.out.println(line.getRaw());
		}
	}

	@Test
	public void testSimple() {
		String[] r = CSV.parseLine("1,2,3");
		assertNotNull(r);
		assertEquals(r.length, 3);
		assertArrayEquals(new String[] {"1", "2", "3"}, r);
	}

	@Test
	public void testEmptyValues() {
		String[] r = CSV.parseLine(",,");
		assertNotNull(r);
		assertEquals(r.length, 3);
		assertArrayEquals(new String[] {"", "", ""}, r);
	}

	@Test
	public void testEmpty() {
		String[] r = CSV.parseLine("");
		assertNotNull(r);
		assertEquals(r.length, 0);
		assertArrayEquals(new String[] {}, r);
	}

	@Test
	public void testOne() {
		String[] r = CSV.parseLine("\"\"");
		assertNotNull(r);
		assertEquals(r.length, 1);
		assertArrayEquals(new String[] {""}, r);
	}

	@Test
	public void testEmptyFile() throws IOException {
		CSVLine[] lines = CSV.newBuilder().setResource(DelimitTest.class, "/empty.csv").build().parseToArray();
		assertNotNull(lines);
		assertEquals(lines.length, 0);
	}

	@Test
	public void testOneFile() throws IOException {
		CSVLine[] lines = CSV.newBuilder().setResource(DelimitTest.class, "/one.csv").build().parseToArray();
		assertNotNull(lines);
		assertEquals(lines.length, 1);
	}

	@Test
	public void testSimpleFile() throws IOException {
		CSVLine[] lines = CSV.newBuilder().setResource(DelimitTest.class, "/simple.csv").build().parseToArray();
		assertNotNull(lines);
		assertEquals(lines.length, 3);
		assertArrayEquals(lines[0].getCsvs(), new String[] {"1", "2", "3"});
		assertArrayEquals(lines[1].getCsvs(), new String[] {"4", "5", "6"});
		assertArrayEquals(lines[2].getCsvs(), new String[] {"7", "8", "9"});
	}

	@Test
	public void testSimpleFileCallback() throws IOException {
		CSVLine[] lines = CSV.newBuilder().setResource(DelimitTest.class, "/simple.csv").build().parseToArray();
		assertNotNull(lines);
		assertEquals(lines.length, 3);
		assertArrayEquals(lines[0].getCsvs(), new String[] {"1", "2", "3"});
		assertArrayEquals(lines[1].getCsvs(), new String[] {"4", "5", "6"});
		assertArrayEquals(lines[2].getCsvs(), new String[] {"7", "8", "9"});
	}

	public void simpleExample() throws IOException {
		CSVLine[] lines = CSV.newBuilder().setFile("data.csv").setQuotedLengthLimit(3).build().parseToArray();
	}

	public void streamingExample() throws ExecutionException, FileNotFoundException {
		BlockingQueue<CSVLine> queue = CSV.newBuilder().setResource(CSV.class, "/data.csv").build().parseToQueue();
	}

	@Test
	public void test2050() throws IOException {
		final AtomicInteger count = new AtomicInteger();

		CSV.newBuilder().setResource(DelimitTest.class, "/2050.csv").setCallback(
				info -> {
					assertNotNull(info);
					assertNotNull(info.getCsvs());
					assertEquals(info.getCsvs(), new String[] {Integer.toString(count.getAndIncrement()), "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"});
				}
		).build().parseToCallback();

		assertEquals(count.get(), 2050);
	}

	@Test
	public void test4StarSingle() throws IOException {
		///Users/mvillalobos/dev/experimental/solr/feeds/fc_feed_4star.csv

		final AtomicInteger count = new AtomicInteger();

		CSV.newBuilder().setResource(DelimitTest.class, "/single.csv").setCallback(
				info -> {
					count.incrementAndGet();
					assertNotNull(info);
					assertNotNull(info.getCsvs());
					//assertEquals(info.getCsvs(), new String[] {Integer.toString(count.getAndIncrement()), "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"});
				}
		).build().parseToCallback();

		assertEquals(count.get(), 1);
	}
}
