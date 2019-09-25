package com.billing.session.remote;

import java.util.LinkedList;
import java.util.Queue;

import javax.xml.bind.MarshalException;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class RemoteShellSession {
	
	protected final static JSch javashell = new JSch();
	protected final static int timeout = 3 * 60 * 1000;
	protected final static int serverAliveCountMax = 3 * 60 * 1000;
	private final static int maxTotalSessions = 50;
	protected static final Queue<Session> sessionQueue = new LinkedList<Session>();
	/*
	 * A Session represents a connection to a SSH server. 
	 * One session can contain multiple Channels of various 
	 * types, created with openChannel(java.lang.String).
	 * 
	 * JSchException - if this session is already connected.
	 */
	
	public static Session establishConnection(final String user, final String hostname, final int portno, final String password){
		try{
			final Session session = javashell.getSession(user, hostname, portno);
			session.setPassword(password.getBytes());
			System.out.println("Host connection successfully Established : " + hostname);
			
			final java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.setTimeout(timeout);
			session.setServerAliveCountMax(serverAliveCountMax);
			session.connect();
			
			System.out.println("-----------------------------------------------------------");
			System.out.println("Connection Successful - SSH - Remote Sessions Established  ");
			System.out.println("***********************************************************");
			
			if(session.isConnected() && sessionQueue.size() < maxTotalSessions){
				sessionQueue.add(session);
				return session;
			}else{
				throw new Exception("Request for SSH Sessions has exceeded the maximium threshold limit :: " + maxTotalSessions);
			}
			
		}catch(JSchException sejsch){
			System.out.println(" Session is already connected" + sejsch.getMessage());
			System.out.println("Source : " + RemoteShellSession.class.getCanonicalName() + " Issue ::> "+ sejsch.getCause().getMessage());
		}catch(Exception remoteShell_err_x){
			System.out.println(remoteShell_err_x.getMessage());
		}
		return null;
	}
	
	//Singleton Session.
	private RemoteShellSession(){}
	
	public static void terminateSSHSessions(){
		if(sessionQueue.isEmpty()){
			System.out.println("No SSH Sessions is there to terminate ");
		}else{
			Session session = null;
			while((session = sessionQueue.poll() ) != null){
				session.disconnect();
			}
			System.out.println("-------------------------------------------------------");
			System.out.println("Finished Operation - SSH - Remote Sessions Disconnected");
			System.out.println("********************************************************");
		}
	}
}
