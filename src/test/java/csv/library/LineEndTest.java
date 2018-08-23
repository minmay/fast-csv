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


import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class LineEndTest {

	private final String resources = "build/resources/test";

	@Test
	public void testCRLineEnd() {
		String filename = "/slash-cr-end.csv";
		createNewLineFile(resources + filename, "\r");
		executeTest(filename);
	}

	@Test
	public void testCRLFLineEnd() {
		String filename = "/slash-crlf-slash-n-end.csv";
		createNewLineFile(resources + filename, "\r\n");
		executeTest(filename);
	}

	@Test
	public void testLFLineEnd() {
		String filename = "/slash-lf-end.csv";
		createNewLineFile(resources + filename, "\n");
		executeTest(filename);
	}

	private void executeTest(String filename) {
		try {
			CSVLine[] csv = FastCSVReader.newBuilder().setResource(LineEndTest.class, filename).build().stream().collect(Collectors.toList()).toArray(new CSVLine[] {});
			assertEquals(csv.length, 4);
			for (CSVLine c : csv) {
				assertEquals(c.getCsvs().length, 4);
				for (int i = 0;i < c.getCsvs().length; i++) {
					String v = c.getCsvs()[i];
					switch (i) {
						case 0:
							assertEquals(v, "one");
							break;
						case 1:
							assertEquals(v, "two");
							break;
						case 2:
							assertEquals(v, "three");
							break;
						case 3:
							assertEquals(v, "four");
							break;
					}
				}
			}
		} catch (RuntimeException e) {
			fail("Unexpected exception.");
		}
	}

	private void createNewLineFile(String filename, String newline) {
		FileWriter fileWriter = null;
		File file = new File(filename);
		try {
			if (!file.exists()) {
				fileWriter = new FileWriter(file);
				String csvs = FastCSVWriter.delimit(',', "one", "two", "three", "four");

				for (int i = 0; i < 3; i++) {
					fileWriter.write(csvs);
					fileWriter.write(newline);
				}
				fileWriter.write(csvs);
				System.out.println("Creating:  " + file.getAbsolutePath());
			} else {
				System.out.println("Already exists:  " + file.getAbsolutePath());
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (Exception e) {
					//empty
				}
			}
		}
	}
}
