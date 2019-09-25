package com.billing.sftp;

import java.util.Scanner;
import java.util.Vector;

import com.billing.paypal.fileprogress.FileProgressListener;
import com.billing.session.remote.RemoteShellSession;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class FetchRemoteSFTPFiles {

	private static String SFTP_CHANNEL_OPEN = "sftp";
	private static int SET_FILE_TRANSFER_THRESHOLD_LIMIT = 20;
	private static int SET_PARALLEL_BULK_REQUEST = 10;

	/*
	 * Fetch The Last Modified File - by date and time stamp
	 */

	public void get(final Session ses_in, String localPath, String remotePath) {

		Channel channel = null;
		ChannelSftp channelSftp = null;

		try {

			channel = ses_in.openChannel(SFTP_CHANNEL_OPEN);
			channel.connect();
			System.out.println("SFTP Channel Opened and Connected.");

			channelSftp = (ChannelSftp) channel;

			/*
			 * Critical Check if the SFTP Source and Destination Directory
			 * Exists Assuming TargetDirFilePath is the SFTP Path.
			 */
			if (exists(channelSftp, remotePath)) {
				System.out.println("SFTP Directory Path Exists !! Success");
			} else {
				System.out.println("SFTP Directory Path Doesn't Exists.");
			}

			channelSftp.cd(remotePath);

			System.out.println("Changing Directory to cd:" + remotePath);

			Vector filelist = channelSftp.ls(remotePath);
			String fileNameToGet = "";

			for (int i = 0; i < filelist.size(); i++) {
				System.out.println(" --> " + filelist.get(i).toString());
				if (i == filelist.size() - 1) {
					fileNameToGet = filelist.get(i).toString();
				}
			}

			// -rw-r--r-- 1 tumbleweed ppreports 398 Sep 8 23:17
			// 2QZP37D82GK9J_results_2019-09-08-001.csv
			// Splitting the above String record obtained by LS by whitespace
			// character to get the file name

			final String[] a = fileNameToGet.split("\\s+");

			channelSftp.get(a[a.length - 1].trim(), localPath, new FileProgressListener());

			System.out.println("Filename ::==> " + localPath + " File transfered successfully to host. ::> "
					+ a[a.length - 1].trim());

		} catch (Exception ex) {
			System.out.println(ex.getMessage() + " Source Method: get() , Class Name :" + this.getClass().getName());
			ex.printStackTrace();
		} finally {
			channelSftp.exit();
			System.out.println("sftp Channel exited.");
			channel.disconnect();
			System.out.println("Channel disconnected.");
		}
	}

	public void get(final Session ses_in, String localPath, String remotePath, String filely) {

		Channel channel = null;
		ChannelSftp channelSftp = null;

		try {

			channel = ses_in.openChannel("sftp");
			channel.connect();
			System.out.println("SFTP Channel Opened and Connected.");

			channelSftp = (ChannelSftp) channel;

			/*
			 * Critical Check if the SFTP Source and Destination Directory
			 * Exists Assuming TargetDirFilePath is the SFTP Path.
			 */
			if (exists(channelSftp, remotePath)) {
				System.out.println("SFTP Directory Path Exists !! Success");
			} else {
				System.out.println("SFTP Directory Path Doesn't Exists.");
			}

			channelSftp.cd(remotePath);

			System.out.println("Changing Directory to cd:" + remotePath);

			Vector filelist = channelSftp.ls(filely);

			System.out.println("Size of filelist in case of wildcard file search :: ---> " + filelist.size());

			if (filelist.size() > SET_FILE_TRANSFER_THRESHOLD_LIMIT) {

				System.out.println("Total fetched file size is :: " + filelist.size());

				System.out.println("Do you want to continue transferring files :: Press Y for Yes or N for No");

				Scanner scanInput = new Scanner(System.in);

				String choice = null;
				while (scanInput.hasNext()) {
					choice = scanInput.next();
					break;
				}

				if (choice.equalsIgnoreCase("y")) {

					/*
					 * Setting Bulk Requests for parallel sending of files.
					 */
					channelSftp.setBulkRequests(SET_PARALLEL_BULK_REQUEST);

					transferFiles(filelist, channelSftp, localPath);

				} else if (choice.equalsIgnoreCase("n")) {

					System.out.println("Sorry Dropping Off. Not proceeding file transfer.");
					RemoteShellSession.terminateSSHSessions();

					System.exit(0);

				} else {

					System.out.println("Not a Valid Option Entered. Reloading Context !! ");
					get(ses_in, localPath, remotePath, filely);
				}
			} else {

				transferFiles(filelist, channelSftp, localPath);

			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage() + " Source Method: get() , Class Name :> " + this.getClass().getName());
			ex.printStackTrace();
		} finally {
			channelSftp.exit();
			System.out.println("sftp Channel exited.");
			channel.disconnect();
			System.out.println("Channel disconnected.");
		}
	}

	private boolean exists(ChannelSftp channelSftp, String path) {
		Vector res = null;
		try {
			res = channelSftp.ls(path);
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				return false;
			}
			System.out.println(
					"Unexpected exception during ls files on sftp: [{ " + e.id + " }:{ " + e.getMessage() + " }] ");
			e.printStackTrace();
		}
		return res != null && !res.isEmpty();
	}

	private void transferFiles(Vector filelist, ChannelSftp channelSftp, String localPath) throws SftpException {

		for (int i = 0; i < filelist.size(); i++) {
			System.out.println(" --> " + filelist.get(i).toString());

			String recordFileListings = filelist.get(i).toString();
			final String[] filearr = recordFileListings.split("\\s+");

			channelSftp.get(filearr[filearr.length - 1], localPath, new FileProgressListener());

			System.out.println("Filename ::==> " + localPath + " File transfered successfully to host. ::> "
					+ filearr[filearr.length - 1].trim());

		}

	}
}
