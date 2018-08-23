package csv.library;


import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.*;


public class FastCSVReaderTest {

    @Test
    public void testSimpleFile() throws IOException {

        List<CSVLine> lines = FastCSVReader.newBuilder()
                .setResource(DelimitTest.class, "/simple.csv")
                .build()
                .stream().
                        collect(Collectors.toList());

        assertNotNull(lines);
        assertEquals(lines.size(), 3);
        assertArrayEquals(lines.get(0).getCsvs(), new String[]{"1", "2", "3"});
        assertArrayEquals(lines.get(1).getCsvs(), new String[]{"4", "5", "6"});
        assertArrayEquals(lines.get(2).getCsvs(), new String[]{"7", "8", "9"});
    }

    @Test
    public void testDelimit() {
        String[] r = parseLine("abc,def,hij");
        assertArrayEquals(new String[] {"abc", "def", "hij"}, r);


        r = parseLine("\"abc\",d\tef,hij");
        assertArrayEquals(new String[] {"abc", "d\tef", "hij"}, r);

        r = parseLine("\"a,bc\",d\tef,hij");
        assertArrayEquals(new String[] {"a,bc", "d\tef", "hij"}, r);

        r = parseLine(",,");
        assertArrayEquals(new String[] {"", "", ""}, r);

        System.out.println("-------");
        r = parseLine("\"a\"\"bc\",,hij");
        assertArrayEquals(new String[] {"a\"bc", "", "hij"}, r);


        r = parseLine("");
        assertNull(r);


        r = parseLine("\"abc\ndef\",\"hij\nklm\"");
        assertArrayEquals(new String[] {"abc\ndef", "hij\nklm"}, r);
    }

    @Test
    public void testTabDelimit() {

        String[] r = parseTabDelimitedLine("abc\tdef\thij");
        assertArrayEquals(new String[] {"abc", "def", "hij"}, r);

        r = parseTabDelimitedLine("\"abc\"\td,ef\thij");
        assertArrayEquals(new String[] {"abc", "d,ef", "hij"}, r);


        r = parseTabDelimitedLine("\"a\tbc\"\td,ef\thij");
        assertArrayEquals(new String[] {"a\tbc", "d,ef", "hij"}, r);

        r = parseTabDelimitedLine("\t\t");
        assertArrayEquals(new String[] {"", "", ""}, r);

        r = parseTabDelimitedLine("\"a\"\"bc\"\t\thij");
        assertArrayEquals(new String[] {"a\"bc", "", "hij"}, r);
    }

    @Test
    public void testValueParsing() {

        final Object[] expected = {true, (byte) 1, 123, 123.456f, 123.456d, 123L, 'a', "Hey man!!!"};
        final Class[] types = {boolean.class, byte.class, int.class, float.class, double.class, long.class, char.class, String.class};
        final String line = CSV.delimit(',', expected);
        final TypeParser parser = new TypeParser();

        Optional<Object[]> actual = FastCSVReader.newBuilder().setText(line).build().stream().findFirst().map(CSVLine::getCsvs).map(csv -> {

            Object[] values = null;

            values = new Object[csv.length];
            for (int i = 0; i < csv.length; i++) {
                String s = csv[i];
                Class type = i < types.length ? types[i] : null;
                Object v = null;
                try {
                    v = type != null ? parser.parse(type, s) : s;
                    values[i] = v;
                } catch (RuntimeException e) {
                    System.err.println("Error parsing type = " + type + " csv[" + i + "] = " + csv[i]);
                    throw e;
                }
            }

            return values;
        });

        assertNotNull(actual);
        assertTrue(actual.isPresent());
        assertArrayEquals(expected, actual.get());
    }

    @Test
    public void testBadCsv() throws IOException {

        List<CSVLine> bad = FastCSVReader.newBuilder()
                .setResource(DelimitTest.class, "/bad.csv")
                .setQuotedLengthLimit(3)
                .build()
                .stream().collect(Collectors.toList());

        assertEquals(2, bad.stream().filter(CSVLine::isError).count());

        assertArrayEquals(new String[] {"1", "2", "3", "4", "5"}, bad.get(0).getCsvs());
        assertArrayEquals(new String[]{"1", "2\n1,2,4,5,6"}, bad.get(1).getCsvs());
        assertTrue(bad.get(1).isError());
        assertArrayEquals(new String[] {"9", "8", "7\""}, bad.get(2).getCsvs());
        assertTrue(bad.get(2).isError());
        assertArrayEquals(new String[] {""}, bad.get(3).getCsvs());

        List<CSVLine> good = FastCSVReader.newBuilder()
                .setResource(DelimitTest.class, "/bad.csv")
                .build()
                .stream().collect(Collectors.toList());

        assertTrue(bad.get(1).isError());

        assertEquals(0, good.stream().filter(CSVLine::isError).count());

        assertArrayEquals(new String[] {"1", "2", "3", "4", "5"}, good.get(0).getCsvs());
        assertArrayEquals(new String[] {"1", "2\n1,2,4,5,6\n9,8,7"}, good.get(1).getCsvs());
        assertArrayEquals(new String[] {""}, good.get(2).getCsvs());
    }

    @Test
    public void testGoodCsv() throws IOException {
        List<String[]> actual = FastCSVReader.newBuilder()
                .setResource(FastCSVReaderTest.class, "/good.csv")
                .build()
                .stream()
                .map(CSVLine::getCsvs)
                .collect(Collectors.toList());


        assertArrayEquals(new String[]{"1", "2", "3", "4", "5"}, actual.get(0));
        assertArrayEquals(new String[]{""}, actual.get(1));
        assertArrayEquals(new String[]{"1", "2\n", "3", "4"}, actual.get(2));
        assertArrayEquals(new String[]{"1", "2", "4", "5", "6"}, actual.get(3));
    }

    @Test
    public void testSimple() {
        String[] r = FastCSVReader.newBuilder()
                .setText("1,2,3")
                .build()
                .stream()
                .findAny().map(CSVLine::getCsvs).orElse(null);

        assertNotNull(r);
        assertEquals(r.length, 3);
        assertArrayEquals(new String[]{"1", "2", "3"}, r);
    }

    @Test
    public void testEmptyValues() {
        String[] r = FastCSVReader.newBuilder()
                .setText(",,")
                .build()
                .stream()
                .findAny().map(CSVLine::getCsvs).orElse(null);

        assertNotNull(r);
        assertEquals(r.length, 3);
        assertArrayEquals(new String[]{"", "", ""}, r);
    }

    @Test
    public void testEmpty() {
        String[] r = FastCSVReader.newBuilder()
                .setText("")
                .build()
                .stream()
                .findAny().map(CSVLine::getCsvs)
                .orElse(null);

        assertNull(r);
    }

    @Test
    public void testOne() {

        String[] r = FastCSVReader.newBuilder()
                .setText("\"\"")
                .build()
                .stream()
                .findAny().map(CSVLine::getCsvs).orElse(null);

        assertNotNull(r);
        assertEquals(r.length, 1);
        assertArrayEquals(new String[]{""}, r);
    }

    @Test
    public void testEmptyFile() throws IOException {
        CSVLine[] lines = FastCSVReader.newBuilder()
                .setResource(DelimitTest.class, "/empty.csv")
                .build()
                .stream()
                .collect(Collectors.toList()).toArray(new CSVLine[]{});
        assertNotNull(lines);
        assertEquals(0, lines.length);
    }

    @Test
    public void testOneFile() throws IOException {

        CSVLine[] lines = FastCSVReader.newBuilder()
                .setResource(DelimitTest.class, "/one.csv")
                .build()
                .stream()
                .collect(Collectors.toList()).toArray(new CSVLine[]{});

        assertNotNull(lines);
        assertEquals(lines.length, 1);
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

        FastCSVReader.newBuilder()
                .setResource(FastCSVReaderTest.class, "/2050.csv")
                .build()
                .stream().forEach(info -> {
            assertNotNull(info);
            assertNotNull(info.getCsvs());
            assertArrayEquals(info.getCsvs(), new String[]{Integer.toString(count.getAndIncrement()), "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"});
        });

        assertEquals(count.get(), 2050);
    }

    @Test
    public void test4StarSingle() throws IOException {

        final AtomicInteger count = new AtomicInteger();

        FastCSVReader.newBuilder()
                .setResource(FastCSVReaderTest.class, "/single.csv").build().stream().forEach(
                info -> {
                    count.incrementAndGet();
                    assertNotNull(info);
                    assertNotNull(info.getCsvs());
                }
        );

        assertEquals(count.get(), 1);
    }

    private String[] parseLine(String line) {
        return FastCSVReader.newBuilder()
                .setText(line)
                .build()
                .stream()
                .findAny()
                .map(CSVLine::getCsvs)
                .orElse(null);
    }

    private String[] parseTabDelimitedLine(String line) {
        return FastCSVReader.newBuilder()
                .setText(line)
                .setDelimiter('\t')
                .build()
                .stream()
                .findAny()
                .map(CSVLine::getCsvs)
                .orElse(null);
    }
}
