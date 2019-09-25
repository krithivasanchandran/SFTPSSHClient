package com.billing.sftp;

import java.io.File;
import java.io.IOException;

public class SFTPTestCases {
	
	public static void main(String args[]) throws IOException{
		
		System.out.println(SFTP.retreivePasswordFromFile("C:/Users/userid/Desktop/abbcasd.html"));
		
		SFTP sftp = new SFTP();
		String path = sftp.getCurrentDirectoryPath();
		System.out.println("path name ---> " + path);
		
		System.out.println(" Is it a directory :: " + new File(path.split(".classpath")[0]).isDirectory());
		
	}

}