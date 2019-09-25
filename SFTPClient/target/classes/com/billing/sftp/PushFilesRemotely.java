package com.billing.sftp;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.billing.paypal.fileprogress.FileProgressListener;
import com.billing.paypal.regularexpression.RegExConverter;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class PushFilesRemotely {

	static int sftpPort = 22;
	static String sftpIncomingDir = "/ppbatch2/incoming/";
	static final List<String> fileContainerHandler = new LinkedList<String>();
	public static final Set<String> regexquantifiers = new HashSet<String>();
	// Prevents buffer clogging in case network delays
	private short avoidCloggingBuffers = 100;

	int totalFileTransferred = 0;

	public void send(final Session ses_, String localpath, String remotedirpath, String filely) {

		Channel channel = null;
		ChannelSftp channelSftp = null;

		try {

			channel = ses_.openChannel("sftp");
			channel.connect();

			System.out.println("SFTP Channel Opened and Connected.");

			channelSftp = (ChannelSftp) channel;

			/*
			 * Critical Check if the SFTP Source and Destination Directory
			 * Exists Assuming TargetDirFilePath is the SFTP Path.
			 */
			if (exists(channelSftp, remotedirpath)) {
				System.out.println("SFTP Directory Path Exists ! Success");
			} else {
				System.out.println("Critical Check Failed !! SFTP Directory Path Doesn't Exists.");
			}

			channelSftp.cd(remotedirpath);

			System.out.println("Changing Directory <RemoteDirectoryPath> to cd:" + remotedirpath);

			Collection<File> matchedFileNames = getAllFilesThatMatchFilenameExtension(localpath, filely);

			System.out.println("Executing File Pattern Matching Flow :: ");

			try {
				System.out.println(" matchedFileNames LIST SIZE =======> " + matchedFileNames.size());

				for (File fmatched : matchedFileNames) {
					System.out.println("Vector Value :::-----> " + fmatched.getName());
					channelSftp.put(localpath + fmatched.getName(), remotedirpath, new FileProgressListener());
					totalFileTransferred++;
				}

			} catch (Exception e) {
				System.out.println(
						"Exception Occured during file transfer operation ,Inbound FileName (Maybe a WildCard Input Also ::> "
								+ filely);
				e.printStackTrace();
				System.out.println(e.getMessage());
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage() + " Source Method: send() , Class Name :" + this.getClass().getName());
			ex.printStackTrace();
		} finally {
			channelSftp.disconnect();
			System.out.println("SFTP Channel Disconnected.");
			channel.disconnect();
			System.out.println("Channel disconnected.");
		}
	}

	public void sendLastModifidFile(final Session ses_, String localpath, String remotedirpath) {

		Channel channel = null;
		ChannelSftp channelSftp = null;

		try {

			channel = ses_.openChannel("sftp");
			channel.connect();

			System.out.println("SFTP Channel Opened and Connected.");

			channelSftp = (ChannelSftp) channel;

			/*
			 * Critical Check if the SFTP Source and Destination Directory
			 * Exists Assuming TargetDirFilePath is the SFTP Path.
			 */
			if (exists(channelSftp, remotedirpath)) {
				System.out.println("SFTP Directory Path Exists ! Success");
			} else {
				System.out.println("SFTP Directory Path Doesn't Exists . ");
			}

			channelSftp.cd(remotedirpath);
			System.out.println("Changing Directory <RemoteDirectoryPath> to cd:" + remotedirpath);

			final String csvFileNameWithPath = getLatestCSVFile(localpath).get(1);
			final String latestcsvFileNameOnly = getLatestCSVFile(localpath).get(0);

			channelSftp.put(csvFileNameWithPath, remotedirpath, new FileProgressListener());

			System.out.println("Filename ::==> " + latestcsvFileNameOnly
					+ " File transfered successfully to host SFTP => " + remotedirpath);

		} catch (Exception ex) {
			System.out.println(ex.getMessage() + " Source Method: send() , Class Name :" + this.getClass().getName());
			ex.printStackTrace();
		} finally {
			channelSftp.disconnect();
			System.out.println("SFTP Channel Disconnected.");
			channel.disconnect();
			System.out.println("Channel Disconnected.");
		}
	}

	/*
	 * Last modified File -->
	 * C:\Users\krichandran\Documents\sftp\2QZP37D82GK9J_results_2019-08-28-006.
	 * csv Example Method Return Type.
	 */

	public List<String> getLatestCSVFile(String srcPath) {

		fileContainerHandler.clear();

		File dir = new File(srcPath);
		File[] files = dir.listFiles();
		File lastModifiedFile = files[0];

		for (int i = 1; i < files.length; i++) {
			if (lastModifiedFile.lastModified() < files[i].lastModified()) {
				lastModifiedFile = files[i];
			}
		}

		System.out.println("The latest file to be pushed is :: " + lastModifiedFile.getName());

		fileContainerHandler.add(0, lastModifiedFile.getName());
		fileContainerHandler.add(1, lastModifiedFile.toString());

		return fileContainerHandler;
	}

	private boolean exists(ChannelSftp channelSftp, String path) {
		Vector res = null;
		try {
			res = channelSftp.ls(path);
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				return false;
			}
			System.out
					.println("Unexpected exception during ls files on sftp: [{" + e.id + "}:{" + e.getMessage() + "}]");
		}
		return res != null && !res.isEmpty();
	}

	/*
	 * Apache Commons IO Example : return FileUtils.listFiles(directory, new
	 * WildcardFileFilter("*.cfg"), null);
	 * http://commons.apache.org/proper/commons-io/javadocs/api-release/org/
	 * apache/commons/io/IOCase.html Windows is case sensitive and Unix is case
	 * insensitive.
	 * http://commons.apache.org/proper/commons-io/javadocs/api-release/org/
	 * apache/commons/io/filefilter/WildcardFileFilter.html
	 * 
	 * The wildcard matcher uses the characters '?' and '*' to represent a
	 * single or multiple wildcard characters.
	 */

	Collection<File> getAllFilesThatMatchFilenameExtension(String directoryName, String extension) {
		File directory = new File(directoryName);
		return FileUtils.listFiles(directory, new WildcardFileFilter(extension, IOCase.INSENSITIVE), null);
	}

	/*
	 * Regular Expression File Search for Future Use.
	 */

	public static List<String> ImplementWildCardSearch(String path, String filenameWithPattern) {

		File[] files = new File(path).listFiles();

		if (files.length == 0) {
			System.out.println("No Files found in the directory :--> " + path);
			System.out.println("Source Class :: " + PushFilesRemotely.class.getName()
					+ " Source Method ::> ImplementWildCardSearch() ");
			return null;
		}

		final List<String> foundFilenames = new ArrayList<String>();

		if (filenameWithPattern.contains("*")) {
			filenameWithPattern = filenameWithPattern.replace("*", "(.*)");
		}

		if (filenameWithPattern.contains("?")) {

			RegExConverter regexConv = new RegExConverter();
			int questionmarkCounter = regexConv.countQuestionmarks(filenameWithPattern);

			if (questionmarkCounter > 0) {

				String replacers = regexConv.buildReplacement(questionmarkCounter);
				String pattern = "[a-zA-Z0-9!@#$&(){}]{" + questionmarkCounter + "}";
				filenameWithPattern = filenameWithPattern.replace(replacers, pattern);
			}
		}

		System.out.println(
				"Wild Card Search Regular Expression to be submitted for Matchers Clause is :: " + filenameWithPattern);

		final Pattern loadFilePatternWildCardSearch = Pattern.compile(filenameWithPattern);

		for (File f : files) {
			String fname = f.getName();

			Matcher m = loadFilePatternWildCardSearch.matcher(fname);
			boolean matchFinder = m.matches();

			if (matchFinder) {
				System.out.println(
						"Match Found for the Pattern :: " + filenameWithPattern + " Found file name -----> " + fname);
				foundFilenames.add(fname);
			}
		}
		return foundFilenames;
	}

}