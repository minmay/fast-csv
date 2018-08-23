# fast-csv
Comma separated value (CSV) parser with streaming support.

## Description
A library for parsing and writing comma separated values (CSV) with a JDK 8 functional Java API.

This will parse or write CSV as specified on http://en.wikipedia.org/wiki/Comma-separated_values and allow for use of any delimiter.

Please allow me to explain some of the unique aspects of comma separated values.

1. They are surprisingly complex to parse.
2. Fields that contain commas, double-quotes, or line-breaks must be quoted.
3. A quote within a field must be escaped with an additional quote immediately preceding the literal quote.
4. Space before and after delimiter commas may not be trimmed.

This CSV parser has the following features.

## Features
1. Takes care of all of the unique aspects of CSV listed above.
2. Highly configurable with many different input sources.
3. Provides three types of parsing:
4. all in memory.
5. streaming (non-blocking).
6. event based callback.
7. The delimiter is configurable.
8. Configurable quoted value length to prevent the parser from reading to the End Of File when a quote is not closed.

## Usage
First assign an input source of CSV lines to parse.
```
setText(String)
setFile(String)
setFile(File)
setInputStream(InputStream)
setChannel(ReadableByteChannel)
setResource(Class,String)
```

Then, parse the input into a data-structure or event callback.

CSVLine[] parseToArray()
BlockingQueue``<CSVLine> parseToQueue() - note that this will use a thread to populate the queue. Hence, this is a non-blocking call.
parseToCallback()
All parsing implementations allow you to configure the buffer size, delimiter, and the quoted value length limit. This is done with the following methods.
```
setBufferSize(int)
setDelimiter(char)
setQuotedLengthLimit(int) // if there is an error closing a quote, this will prevent the parser from reading to the end of file.
```
## Examples
### Collect into Array
This example parses a file, puts all of the contents in memory within an array of CSV lines. 
```java
CSVLine[] lines = CSV().newBuilder()
    .setFile("data.csv")
    .build()
    .stream()
    .collect(Collectors.toList())
    .toArray(new CSVLine[]{});
```

The FAST CSV library uses the builder design pattern to configure what and how you want to parse.
