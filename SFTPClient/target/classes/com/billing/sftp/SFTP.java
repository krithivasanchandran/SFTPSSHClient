package com.billing.sftp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import com.billing.session.remote.RemoteShellSession;
import com.jcraft.jsch.Session;

/*
 *
	
	CLI -
	
	PUSH
	----
     -host reports.sandbox.paypal.com -port 22 -user sftpYZ_bamarnath-bizUSpaypal.com -passFile C:/Users/krichandran/Documents/kenanpwd/abc.txt -mode PUT -localdir  C:/Users/krichandran/Documents/regtest/ -remotedir /ppbatch2/incoming/	 
	
	 -host reports.sandbox.paypal.com -port 22 -user sftpYZ_bamarnath-bizUSpaypal.com -passFile C:/Users/krichandran/Documents/kenanpwd/abc.txt -mode PUT -localdir  C:/Users/krichandran/Documents/regtest/ -remotedir /ppbatch2/incoming/ -filename imple*
	
	GET
	---
     -host reports.sandbox.paypal.com -port 22 -user sftpYZ_bamarnath-bizUSpaypal.com -passFile C:/Users/krichandran/Documents/kenanpwd/abc.txt -mode GET -localdir  C:/Users/krichandran/Documents/regtest/ -remotedir /ppbatch2/outgoing/ -filename 2QZP37D82GK9J_results_2019-08-28-*.csv	
     
     -host reports.sandbox.paypal.com -port 22 -user sftpYZ_bamarnath-bizUSpaypal.com -passFile C:/Users/krichandran/Documents/kenanpwd/abc.txt -mode GET -localdir  C:/Users/krichandran/Documents/regtest/ -remotedir /ppbatch2/outgoing/
	
	
	WildCard Search :  
	 
	 1. Filename could be absolute name e.g. foo.txt
     2. Filename could be wildcard name e.g. f??.txt or f*.txt
 */

public class SFTP {

	static String hostname = null;
	static String SFTPUser = null;
	static String password = null;
	static String modeofOperation = null;
	static String localdir = null;
	static String remotedir = null;
	static String fileName = "";
	static int portno = 0;
	static final String currPathDotOperator = ".";
	static boolean executeLastModifiedFileWorkFlow = false;

	public static void main(String[] args) throws IOException {

		SFTPConfigurations config = new SFTPConfigurations();

		/*
		 * Check Directory Permissions for Read/Write Access
		 */
		try {

			CommandLineParser parser = new DefaultParser();
			CommandLine cli = parser.parse(config.getOptions(), args);

			if (cli.hasOption("host")) {
				hostname = cli.getOptionValue("host").trim();

				if (hostname == null) {
					System.out.println("Hostname is empty ! So Exiting");
					return;
				}
			}

			System.out.println("SFTP Host Name is :" + hostname);

			if (cli.hasOption("port")) {
				try {
					portno = Integer.parseInt(cli.getOptionValue("port").trim());
				} catch (NumberFormatException e) {
					System.out.println();
					/*
					 * Assigning Default Port due to Wrong Input Type
					 */
					portno = 22;
				}
			}

			System.out.println("SFTP Port Number is :" + portno);

			if (cli.hasOption("user")) {
				SFTPUser = cli.getOptionValue("user").trim();

				if (SFTPUser == null) {
					System.out.println("SFTP User is empty! So Exiting");
					return;
				}
			}

			System.out.println("SFTPUser :: " + SFTPUser);

			if (cli.hasOption("passFile")) {
				String inbound_pwd_file_Path = cli.getOptionValue("passFile").trim();

				if (inbound_pwd_file_Path == null) {
					System.out.println("Password File is empty ! So Exiting");
					return;
				}
				password = retreivePasswordFromFile(inbound_pwd_file_Path.trim()).trim();

				if (password.isEmpty()) {
					System.out.println("The file is empty or contains NULL passwords");
				}
			}

			// System.out.println("Base64 Encoded Password :: " +
			// Base64.getEncoder().encode(password.getBytes()));

			if (cli.hasOption("mode")) {
				modeofOperation = cli.getOptionValue("mode").trim();

				if (modeofOperation == null) {
					System.out.println("Mode of operation supported : PUT or GET ! Exiting !");
					return;
				}
			}

			System.out.println("Operation mode :: " + modeofOperation);

			if (cli.hasOption("localdir")) {
				localdir = cli.getOptionValue("localdir").trim();

				if (localdir == null) {
					System.out.println("Source File Path to pick the FILE from is empty !! So Exiting");
					return;
				}

				if (localdir.equalsIgnoreCase(currPathDotOperator)) {
					localdir = getCurrentDirectoryPath();
				}
			}
		
			System.out.println("Local Directory Path :: --> " + localdir);

			if (cli.hasOption("remotedir")) {
				remotedir = cli.getOptionValue("remotedir").trim();

				if (remotedir == null) {
					System.out.println("Target File Path is empty !! So Exiting");
					return;
				}
			}

			System.out.println(" Remote Directory Path ::" + remotedir);

			if (cli.hasOption("filename")) {
				fileName = cli.getOptionValue("filename");
				
				System.out.println(" File Name is :: " + fileName);
				
			}else{
				executeLastModifiedFileWorkFlow = true;
			}

		} catch (ParseException e) {
			System.out.println("Command Line Parse Exception Occured ! Cause ->" + e.getMessage());
		}
		
		if(!localdir.endsWith("/")) localdir = localdir.concat("/");

		if(!remotedir.endsWith("/")) remotedir = remotedir.concat("/");

		/*
		 * Core Operation - SSH Remote
		 */

		final Session sess = RemoteShellSession.establishConnection(SFTPUser, hostname, portno, password);

		if (modeofOperation.equalsIgnoreCase("PUT")) {

			if (executeLastModifiedFileWorkFlow) {

				new PushFilesRemotely().sendLastModifidFile(sess, localdir, remotedir);

			} else {

				new PushFilesRemotely().send(sess, localdir, remotedir, fileName);

			}

		} else if (modeofOperation.equalsIgnoreCase("GET")) {

			if (executeLastModifiedFileWorkFlow) {

				new FetchRemoteSFTPFiles().get(sess, localdir, remotedir);

			} else {

				new FetchRemoteSFTPFiles().get(sess, localdir, remotedir, fileName);

			}

		} else {

			System.out.println("PUT or GET Operation modes are only Supported! Please enter either of those.");

			RemoteShellSession.terminateSSHSessions();

			return;
		}

		/*
		 * Terminate All SSH Sessions
		 */
		if (sess.isConnected()) {
			RemoteShellSession.terminateSSHSessions();
		}
	}

	public static Boolean isDir(final String dir) {
		return new File(dir).isDirectory();
	}

	public static String retreivePasswordFromFile(String fileName) throws IOException {

		File inFileHandler = new File((String) fileName);
		boolean isValidFile = inFileHandler.isFile() && inFileHandler.canRead() && !(inFileHandler.isDirectory());

		if (isValidFile) {
			RandomAccessFile file = null;
			try {
				file = new RandomAccessFile(fileName, "r");

				String fileptr = null;

				while ((fileptr = file.readLine()) != null) {
					final String caseSensitivebldr = new String(fileptr);
					return caseSensitivebldr.toString().trim();
				}
			} catch (FileNotFoundException e) {
				System.out.println(e.getMessage());
			} catch (SecurityException security_e) {
				/*
				 * SecurityException - if a security manager exists and its
				 * checkRead method denies read access to the file or the mode
				 * is "rw" and the security manager's checkWrite method denies
				 * write access to the file
				 */
				System.out.println(security_e.getMessage());

			} finally {
				try {
					file.close();
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		} else {
			System.out.println("Password File cannot be Read or File Doesn't Exist in the FilePath");
		}
		return "";
	}

	public static String getCurrentDirectoryPath() {

		Path path = FileSystems.getDefault().getPath(".").toAbsolutePath();
		return path.toString();

	}

}