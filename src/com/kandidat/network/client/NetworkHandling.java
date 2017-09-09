package com.kandidat.network.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import android.os.Handler;
import android.util.Log;

import com.kandidat.network.TCPHandler;
import messagepacket.MessagePacket;

/**
 * Class for handling the TCP communication from and to the server.
 * An interface for the client to use for the server.
 * 
 * Note: This file has been modified by boemma
 */
public class NetworkHandling {
	
	//Debugging
	private final boolean D  = false;
	private final String tag = "NetworkHandling";
	
	public static Semaphore lock = new Semaphore(0);
	
	private static ObjectOutputStream outToServer = null;
	private static ObjectInputStream inFromServer = null;
	private static Socket clientSocket = null;
	private static Object msg = null;
	
    private static final int server_port = 0; //Dummy
	private static final String server_ip = new String("127.0.0.1"); //Dummy	
	
	private Handler handler = null;
	private boolean running = true;
	private Thread listenThread = null;
	
	public NetworkHandling(Handler handler) {
		this.handler = handler;
	}
	
	/**
	 * Establish a connection between the user and the server.
	 */
	public void establishConnection() {
    	establishConnection(server_ip, server_port);
	}
	
	/**
	 * Establish a connection to the server with the given IP and
	 * port number.
	 */
	public void establishConnection(final String server_ip, final int server_port) {
	    	try {
				clientSocket = new Socket(server_ip,server_port);
				outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
				inFromServer = new ObjectInputStream(clientSocket.getInputStream());
			} catch (Exception e) {
				if(D) Log.e(tag, "Failed to establish connection", e);
			}
	    	
	    	if(clientSocket != null && outToServer != null && inFromServer != null) {
				listenThread = new ListenThread(inFromServer);
				listenThread.start();	
	    	}
	    		
	    	lock.release();
	}
	
	/**
	 * Send the given MessagePacket to the server.
	 */
	public boolean writeToServer(MessagePacket msg) {
		try {
			outToServer.writeObject(msg);
			outToServer.flush();
			return true;
		} catch (NullPointerException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Wait for message from server, then it to
	 * the handler to take care of it.
	 */
	private class ListenThread extends Thread {
		private ObjectInputStream inFromServer;
		
		public ListenThread(ObjectInputStream in) {
			inFromServer = in;
		}
		
		public void run() {
			setName("NetworkListenThread");
			
			if (D) Log.d(tag+"-thread:run", "--- starting listening thread...");
			while(running) {
				if (D) Log.d(tag+"-thread:loop", "-- wainting for message...");
				try {
					msg = inFromServer.readObject();
				} catch (NullPointerException ignored) {
				} catch (ClassNotFoundException ignored) {
				} catch (IOException ignored) {
				}
				
				if (D) Log.d(tag+"-thread:loop", "-- got a message!" +
						"\n- null? "+(msg == null));
				
				if (msg != null && running) {
					if (D) Log.d(tag+"-thread:loop", "- sending message to handler...");
					
					handler.obtainMessage(TCPHandler.MESSAGE_READ, msg)
						   .sendToTarget();
				}
			}
		}
	}
		
	/**
	 * Disconnect from the server and close all sockets.
	 */
	public void disconnect() {
		try {
			running = false;
			listenThread.interrupt();
			
			outToServer.close();
			inFromServer.close();
			clientSocket.close();

		} catch (IOException e) {
		}
	}
	
	/**
	 * Returns true if connected.
	 */
	public boolean isConnected() {
		return outToServer != null &&
			   inFromServer != null &&
			   clientSocket != null;
	}
	
	/**
	 * Return the handler.
	 */
	public Handler getHandler() {
		return handler;
	}
}
