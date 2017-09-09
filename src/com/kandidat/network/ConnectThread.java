//package com.kandidat.network;
//
//import android.content.Intent;
//import android.os.Handler;
//import android.os.Looper;
//
//import com.kandidat.gui.Main;
//import com.kandidat.gui.Main.MultiController;
//import com.kandidat.network.client.BluetoothHandling;
//import com.kandidat.network.client.DeviceListActivity;
//import com.kandidat.network.client.NetworkHandling;
//
///** 
// * Class for connecting to other users by server or bluetooth
// **/
//public class ConnectThread extends Thread {
//
//	private Handler handler = new Handler(Looper.getMainLooper());
//	private final String C_THREAD = "ConnectThread";
//
//	private BluetoothHandling bluetoothHandling;
//	private BluetoothHandler bluetoothHandler = new BluetoothHandler();
//	
//	private static NetworkHandling networkHandling;
//	private TCPHandler tcpHandler = new TCPHandler();
//	
//	private Main activity;
//	
//	public ConnectThread(Main activity) {
//		bluetoothHandling = Main.getBTHandler();
//		networkHandling   = Main.getTCPHandler();
//	}
//	
//	/**
//	 * TODO: Add description
//	 * @param C_THREAD 
//	 */
//	@SuppressWarnings("static-access")
//	@Override
//	public void run() {
//		setName(C_THREAD);
//		if(activity.controller == MultiController.TCP) {
//			networkHandling = new NetworkHandling(tcpHandler);
//			networkHandling.establishConnection();
//
//			try { 
//				//Wait until the lock is released - wait for connection
//				networkHandling.lock.acquire(); 
//			} catch (InterruptedException e) { }
//
//			finished(networkHandling.isConnected());
//		}
//
//		if(activity.controller == MultiController.BLUETOOTH) {
//			bluetoothHandling = new BluetoothHandling(activity.getApplicationContext(),bluetoothHandler);
//			bluetoothHandling.start();
//
//			Intent intent = new Intent(activity.getApplicationContext(),DeviceListActivity.class);
//			activity.startActivityForResult(intent, BluetoothHandler.REQUEST_ADDRESS);
//
//			try {
//				//Wait until the lock is released - wait for connection
//				BluetoothHandling.lock.acquire();
//			} catch (InterruptedException e) {
//			}
//
//			finished(bluetoothHandling.getState() == BluetoothHandling.STATE_CONNECTED);
//		}
//	}
//
//	/**
//	 * TODO: Add description
//	 */
//	public void finished(final boolean result) {
//		handler.post(new Runnable() {
//			public void run() {
//				if (result) {
//					if (activity.controller == MultiController.BLUETOOTH)
//						activity.finishActivity(BluetoothHandler.REQUEST_ADDRESS);
//					activity.startMultiPlayer();
//				} else {
//					activity.failedMultiPlayer();
//				}		
//			}});
//	}
//}
