# fast-csv
Fluent comma separated value (csv) parser with streaming support. 
I hosted this project on Google Code in 2010. I moved it to github in August 2019. 
I refactored the original API use JDK 1.8 functional Java API with streaming support.
The original project is at https://code.google.com/archive/p/csv-library/.

## Description
This utility library parses and writes comma separated values (CSV) with a simple fluent API and functional Java.

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
3. Uses the java.util.stream API.
4. Implemented with NIO.
5. The delimiter is configurable.
6. Configurable quoted value length to prevent the parser from reading to the End Of File when a quote is not closed.

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

```build()``` then, ```stream()```.

All parsing implementations allow you to configure the buffer size, delimiter, and the quoted value length limit. This is done with the following methods.
```
setBufferSize(int)
setDelimiter(char)
setQuotedLengthLimit(int) // if there is an error closing a quote, this will prevent the parser from reading to the end of file.
```
## Examples
### Iterate over each element 
```java
FastCSVReader.newBuilder()
    .setFile("data.csv")
    .build()
    .stream()
    .map(CSVLine::getCsvs)              // map into a String array
    .forEach(line -> {
        System.out.println(line[0]);    // print the first column
    });
```
### Collect into CSVLine Array 
```java
CSVLine[] lines = FastCSVReader.newBuilder()
    .setFile("data.csv")
    .build()
    .stream()
    .collect(Collectors.toList())       // collect into memory as a List
    .toArray(new CSVLine[]{});          // convert into an array
```
### Collect into multi-dimensional String array. 
```java
String[][] lines = FastCSVReader.newBuilder()
    .setFile("data.csv")
    .build()
    .stream()
    .map(CSVLine::getCsvs)              // map into a String array
    .collect(Collectors.toList())       // collect arrays into a List
    .toArray(new String[][]{});         // convert the List of of arrays into a String two-dimensional array
```
### Collect into multi-dimensional Object array.
```java
Object[][] lines = FastCSVReader.newBuilder()
    .setFile("data.csv")
    .build()
    .stream()                           // line below declares the type of each field, and then maps into an Object array.
    .map(TypeParser.newInstance().add(boolean.class, byte.class, int.class, float.class, double.class, long.class, char.class, String.class))
    .collect(Collectors.toList())       // collect the arrays into a List
    .toArray(new Object[][]{});         // convert the List of of arrays into a Object two-dimensional array
```
### First as an object array.
```java 
Object[] parsed = FastCSVReader
    .newBuilder()
    .setText("true,1,123,123.456,123.456,123,a,Hey man!!!")
    .build()
    .stream()                           // line below declares the type of each field, and then maps into an Object array.
    .map(TypeParser.newInstance().add(boolean.class, byte.class, int.class, float.class, double.class, long.class, char.class, String.class))
    .findFirst()                        // finds the first row
    .orElse(null);                      // returns new Object[] {true, (byte) 1, 123, 123.456f, 123.456d, 123L, 'a', "Hey man!!!"};
       
```
