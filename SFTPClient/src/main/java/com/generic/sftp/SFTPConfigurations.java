package com.billing.sftp;
import org.apache.commons.cli.Options;

public class SFTPConfigurations {
	
	/*
	 * Maximum file count allowed for push or pulling files around.
	 */
	public static final int maxFileAllowedGETorPUTOperations = 25;
	
	Options options = null;
	
	/*
	 * Reads the Command Line Arguments
	 */
	public SFTPConfigurations() {
		options = new Options();

		options.addOption("h","host",true,"Hostname to connect to remote sftp host");
		options.addOption("port","port",true,"Port to Connect remotely. Default is 22");
		options.addOption("u","user",true,"Username to connect to remote sftp host");
		options.addOption("p","passFile",true,"Password file to connect to remote sftp host");
		options.addOption("m","mode",true,"Mode of operation supported PUT or GET");
		options.addOption("src","localdir",true,"Local Directory Path");
		options.addOption("target","remotedir",true,"Remote Directory Path");
		options.addOption("fname","filename",true,"Name of file to be moved around");
		
	}
	
	public Options getOptions() {	
		if(options != null){
			return options;
		}else{
		    System.out.println("Command Line Initializaiton Exception");
		    return new SFTPConfigurations().getOptions();
		}
	}

}
