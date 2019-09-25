package com.billing.paypal.fileprogress;

import com.jcraft.jsch.SftpProgressMonitor;

/*
 * File Transfer Listener that keeps track of the file transfer
 * Progress in 100% relative value and count of file transfer
 * in bytes.
 */

public class FileProgressListener implements SftpProgressMonitor {

	private long max = 1, count = 0, percent = 0;

	public FileProgressListener() {
	}

	@Override
	public boolean count(long byts) {
		this.count += byts;
		long percentNow = this.count * 100 / max;
		if (percentNow > this.percent) {
			this.percent = percentNow;

			System.out.println("File Progress :: >" + this.percent); // Progress
																		// 0,0
			System.out.println(" File Size :: > " + max); // Total Filesize
			System.out.println(" File Count in Bytes :: > " + this.count); 
		}

		return (true);
	}

	@Override
	public void end() {
		// Process in bytes from the total
		System.out.println("SMTP File Tranfer has ended :: Total Bytes Transferred" + " Count ===> " + this.count);
		System.out.println(" Progress :: >" + this.percent); // Progress
		System.out.println(" Total File Size  :: > " + max); // Total filesize
		System.out.println(" File Count in Bytes :: > " + this.count);
	}

	@Override
	public void init(int op, String src, String destination, long maximum) {
		System.out.println("File Transfer Listener Interface :: INITIATED");
		this.max = max;
	}

}
