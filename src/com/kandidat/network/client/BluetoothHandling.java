package com.kandidat.network.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import com.kandidat.network.BluetoothHandler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import messagepacket.MessagePacket;

/**
 * Class for handling the bluetooth communication between
 * two devices.
 */
public class BluetoothHandling {
    
	//Debugging
	private static final String TAG = "BluetoothHandling";
    private static final boolean D = false;

    private static final String NAME = "BluetoothPeer";

    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private int state;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;  

    public static final int MESSAGE_TOAST = 4; 
    public static final int MESSAGE_DEVICE_NAME = 6;
    public static final int MESSAGE_READ = 7;
    
    public static String DEVICE_NAME = "0";
    public static String DEVICE_NAME_MAC_OTHER = "0";
    public static String TOAST = "Cheers";
    
    public final Semaphore lock = new Semaphore(0);
    
    public BluetoothHandling(Context context, Handler handler) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        state = STATE_NONE;
        this.handler = handler;
    }

    /**
     * Give the new state to the Handler so the UI Activity can update
     * handler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
     */
    private synchronized void setState(int state) {
        if (D) 
        Log.d(TAG, "setState() " + state + " -> " + state);
       
        this.state = state;
    }

    /**
     * Return the current state of the handler.
     */
    public synchronized int getState() {
        return state;
    }
    
    /**
     * TODO: Add description
     */
    public synchronized byte getId() {
    	return (byte) Math.min(1,
    			Math.max(0,
    			bluetoothAdapter.getAddress().compareTo(DEVICE_NAME_MAC_OTHER)));
    }
    
    /**
     * Return the handler.
     */
    public synchronized Handler getHandler() {
    	return handler;
    }

    /**
     * Start a new AcceptThread, and set state to listen. 
     */
    public synchronized void start() {
        if (D) 
        	Log.d(TAG, "start");
        
        if (connectThread != null)
        	connectThread.cancel(); connectThread = null;
        
        if (connectedThread != null) 
        	connectedThread.cancel(); connectedThread = null;
        
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
        
        setState(STATE_LISTEN);
    }

    /**
     * Start a new ConnectThread, and set the state to connect.
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) 
        	Log.d(TAG, "connect to: " + device);
        
        if (state == STATE_CONNECTING) {
            if (connectThread != null) {
            	connectThread.cancel(); 
            	connectThread = null;
            }
        }
        
        if (connectedThread != null) {
        	connectedThread.cancel(); 
        	connectedThread = null;
        }
        
        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start a new ConnectedThread which reads from the given 
     * socket. Send a message with the other user's name, for 
     * the handler to manage. Changes the state to connected. 
     * Releases the lock, so the user comes to the drawing surface.
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) 
        	Log.d(TAG, "connected");
        
        if (connectThread != null) {
        	connectThread.cancel(); 
        	connectThread = null;
        }
        
        if (connectedThread != null) {
        	connectedThread.cancel(); 
        	connectedThread = null;
        }
        
        if (acceptThread != null) {
        	acceptThread.cancel(); 
        	acceptThread = null;
        }
        
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        DEVICE_NAME_MAC_OTHER = device.getAddress();
        
        Message msg = handler.obtainMessage(BluetoothHandling.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothHandling.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        handler.sendMessage(msg);

        setState(STATE_CONNECTED);
        
        lock.release();
    }

    /**
     * Cancel threads and set state to none.
     */
    public synchronized void stop() {
        if (D) 
        	Log.d(TAG, "stop");
        
        if (connectThread != null) {
        	connectThread.cancel(); 
        	connectThread = null;
        }
        
        if (connectedThread != null) {
        	connectedThread.cancel(); 
        	connectedThread = null;
        }
        
        if (acceptThread != null) {
        	acceptThread.cancel(); 
        	acceptThread = null;
        }
        
        setState(STATE_NONE);
    }

    /**
     * If connected, send out the given MessagePacket.
     */
    public void write(MessagePacket out) {
        ConnectedThread r;
        
        synchronized (this) {
            if (state != STATE_CONNECTED) return;
            r = connectedThread;
        }
        r.write(out);
    }

    /**
     * Set state to none, and send message to the handler
     * in order to show a toast telling the user that the connection
     * failed.
     */
    private void connectionFailed() {
        setState(STATE_NONE);

        if(D) Log.d(TAG,"ConnectionFailed");
        
        stop();
        
        lock.release();
        
        Message msg = handler.obtainMessage(BluetoothHandler.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putInt(BluetoothHandler.TOAST, 0);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    /**
     * Set state to none, and send a message to the handler
     * in order to show a toast telling the user that the connection
     * was lost. 
     */
    private void connectionLost() {
        setState(STATE_NONE);

        if(D) Log.d(TAG,"ConnectionLost");
        
        Message msg = handler.obtainMessage(BluetoothHandler.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putInt(BluetoothHandler.TOAST, 1);
        msg.setData(bundle);
        handler.sendMessage(msg);
        
    }

    /**
     * Thread for getting acception for connection with 
     * another device.
     */
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;
        
        /**
         * Set server socket.
         */
        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
            	if(D)
                Log.e(TAG, "listen() failed", e);
            }
            serverSocket = tmp;
        }

        /**
         * While not connected, try to establish connection with
         * server socket (the other user's socket). If 
         * trying to connect - start reading from socket. 
         * When done, close the socket.
         */
        public void run() {
            if (D) Log.d(TAG, "BEGIN acceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            while (state != STATE_CONNECTED) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    if(D)Log.e(TAG, "accept() failed", e);
                    break;
                }

                if (socket != null) {
                    synchronized (BluetoothHandling.this) {
                        switch (state) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            try {
                                socket.close();
                            } catch (IOException e) {
                               if(D)Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END acceptThread");
        }

        /**
         * Close the server socket
         */
        public void cancel() {
            if (D) Log.d(TAG, "cancel " + this);
            try {
                serverSocket.close();
            } catch (IOException e) {
               if(D) Log.e(TAG, "close() of server failed", e);
            }
        }
    }

    /**
     * Thread for trying to connect to another device.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket clientSocket;
        private final BluetoothDevice remoteDevice;

        /**
         * Start a connection to the remote device.
         */
        public ConnectThread(BluetoothDevice device) {
            remoteDevice = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                if(D)Log.e(TAG, "create() failed", e);
            }
            clientSocket = tmp;
        }

        /**
         * Connect to the remote device, and then start
         * an AcceptionThread. When accepted, start a new 
         * ConnectedThread.
         */
        public void run() {
            Log.i(TAG, "BEGIN connectThread");
            setName("ConnectThread");

            bluetoothAdapter.cancelDiscovery();

            try {
                clientSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                try {
                    clientSocket.close();
                } catch (IOException e2) {
                   if(D) Log.e(TAG, "unable to close() socket during connection failure", e2);
                }

                return;
            }

            synchronized (BluetoothHandling.this) {
                connectThread = null;
            }

           if(D) Log.d(TAG,"About to call connected, device: " + remoteDevice);
            connected(clientSocket, remoteDevice);
        }
        
        /**
         * Close the connection to the other device.
         */
        public void cancel() {
            try {
                clientSocket.close();
            } catch (IOException e) {
                if(D)Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * Thread for reading and writing messages 
     * to the other device.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket clientSocket;
        private final ObjectInputStream in;
        private final ObjectOutputStream out;

        /**
         * Create in- and output streams.
         */
        public ConnectedThread(BluetoothSocket socket) {
           if(D) Log.d(TAG, "create ConnectedThread");
            clientSocket = socket;
            ObjectInputStream tmpIn = null;
            ObjectOutputStream tmpOut = null;

            try {
            	tmpOut = new ObjectOutputStream(clientSocket.getOutputStream());
                tmpOut.flush();
            	tmpIn = new ObjectInputStream(clientSocket.getInputStream());

            } catch (IOException e) {
               if(D) Log.e(TAG, "temp sockets not created", e);
               try {
				clientSocket.close();
				connectionLost();
			} catch (IOException e1) {
				if(D) Log.d(TAG,"ConnectionLost in ConnectedThread, closing socket");
			}
               
            }

            out = tmpOut;           
            in = tmpIn;

        }

        /**
         * Read messages from the other device.
         */
        public void run() {
            if(D)Log.i(TAG, "BEGIN connectedThread");
            setName("connectedThread");

            while (true) {
                try {
                	if(D) Log.d(TAG,"Just about to read message in ConnectedThread");
                	
                	MessagePacket messageP = (MessagePacket) in.readObject();
                    if(D) Log.d(TAG,"Read MessagePacket " + messageP.getMsg());
                    
                    handler.obtainMessage(BluetoothHandler.MESSAGE_READ,messageP)
                            .sendToTarget();
                } catch (IOException e) {
                   if(D) Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                } catch(ClassNotFoundException e1) {
                	if(D)Log.e(TAG,"disconnected",e1);
                	connectionLost();
                	break;
                } catch(NullPointerException e2) {
                	if(D) Log.e(TAG,"disconnected",e2);
                	connectionLost();
                	break;
                }
            }
        }

        /*
         * Write a message to the other user's socket.
         */
        public void write(MessagePacket msg) {
            try {
            	if(D)Log.d(TAG,"Inside the write function");
            	
            	out.writeObject(msg);
            	out.flush();
            	
            } catch (IOException e) {
                if(D)Log.e(TAG, "Exception during write", e);
            }
        }

        /**
         * Close the socket.
         */
        public void cancel() {
            try {
                clientSocket.close();
            } catch (IOException e) {
                if(D)Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
