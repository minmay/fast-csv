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

import java.util.concurrent.BlockingQueue;

/**
 * A line pusher that puts the comma separated value info into a blocking queue for for deferred, thread-safe, multi-threaded processing.
 */
public class BlockingQueuePusher implements PushLineCallback{

	/** The blocking queue that will contain the comma separated values. */
	private BlockingQueue<CSVLine> out;

	/**
	 * Creates an instance of this class.
	 *
	 * @param out The blocking queue that will contain the comma separated values.
	 */
	public BlockingQueuePusher(BlockingQueue<CSVLine> out) {
		this.out = out;
	}

	/**
	 * Puts the comma separated value info into a blocking queue for deferred, thread-safe, multi-threaded processing.
	 *
	 * @param info The comma separated value line.
	 */
	public void pushLine(CSVLine info) {
		if (info!=null) {
			if (out!=null) {
				try {
					out.put(info);
				} catch (InterruptedException e) {
					throw new RuntimeException("Failed to put CSV line in out queue.", e);
				}
			}
		}
	}
}
