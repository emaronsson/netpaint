package com.kandidat.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kandidat.R;
import com.kandidat.network.BluetoothHandler;
import com.kandidat.network.TCPHandler;
import com.kandidat.network.client.BluetoothHandling;
import com.kandidat.network.client.DeviceListActivity;
import com.kandidat.network.client.NetworkHandling;
import com.kandidat.rityta.multi.DrawingActivityMulti;
import com.kandidat.rityta.single.DrawingActivitySingle;

/**
 * Class for the main menu.
 */
public class Main extends Activity {
	
	// Debugging
	private final boolean D  = false;
	private final String tag = "Main";
	
	/** The different types of connection supported */
	public enum MultiController {
		TCP, BLUETOOTH;
	};
	
	/** Boolean used to prevent the lock releasing when it's not supposed to */
	private boolean finReq = false;
	
	/** TCP will be selected for transmission when starting the application*/
	public static MultiController controller = MultiController.TCP;
	
	/** Buttons used to access the single, multi and options parts, respectively */
	private Button singleplayer;
	private Button multiplayer;
	private Button options;
	
	/** Special font used for the interactive menus */
	private Typeface tf;
	
	/** The handlers for all incoming/outgoing messages via a bluetooth connection */
	private static BluetoothHandling bluetoothHandling = null;
	private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private BluetoothDevice remoteDevice = null;
	private BluetoothHandler bluetoothHandler = new BluetoothHandler();
	
	/** The handlers for all incoming/outgoing messages via a TCP connection */
	private static NetworkHandling networkHandling = null;
	private TCPHandler tcpHandler = new TCPHandler();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.menu_main);
       
        tf = Typeface.createFromAsset(getAssets(),
                "fonts/PAINP___.TTF");
        TextView tv = (TextView) findViewById(R.id.title);
        tv.setTypeface(tf);
        TextView tv2 = (TextView) findViewById(R.id.title2);
        tv2.setTypeface(tf);
        
        boundButtons();
    }
    
    /** Binds listeners to the view's buttons*/
    private void boundButtons() {
    	singleplayer = (Button) findViewById(R.id.single);
    	singleplayer.setTypeface(tf);
    	singleplayer.setOnClickListener(new OnClickListener() {
    		
    		/** Start a drawing surface for one player*/
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Main.this, DrawingActivitySingle.class);
				startActivity(intent);
		}});
    	
    	multiplayer = (Button) findViewById(R.id.multi);
    	multiplayer.setTypeface(tf);
    	multiplayer.setOnClickListener(new OnClickListener() {
    		
    		/** Connect to other users to start a drawing surface*/
			@Override
			public void onClick(View v) {
				if (multiplayer.isEnabled()) {
					multiplayer.setEnabled(false);
					
					initMultiPlayer();
				}
		}});
    	
    	options = (Button) findViewById(R.id.options);
    	options.setTypeface(tf);
    	options.setOnClickListener(new OnClickListener() {
    		
    		/** Start the options menu */
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Main.this, Options.class);
				startActivity(intent);
		}});
    }

    // ----------------------- Key press handler ------------------------- //
    
    /** Stop this activity when pressing the back-button*/
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	
    	// Handle back button
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		new AlertDialog.Builder(this)
    		.setTitle(R.string.quit_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(R.string.quit_text)
            .setPositiveButton(R.string.quit_yes, new DialogInterface.OnClickListener() {
            	
            	@Override
                public void onClick(DialogInterface dialog, int which) {

                    //Stop the activity
                    Main.this.finish();    
            }})
            .setNegativeButton(R.string.quit_no, null)
            .show();
    		
    		return true;
        }
        else
            return super.onKeyDown(keyCode, event);
	}
	
    /** Set the default value of the transmission - TCP*/
	@Override
	protected void onDestroy() {
		super.onDestroy();
	
		Main.controller = Main.MultiController.TCP;
	}
	
	// -------------------------- Connect task --------------------------- //

	/**
	 * Starts the thread that connects to the multiplayer server
	 */
	public void initMultiPlayer() {
		new ConnectThread().start();
	}
	
	/** 
	 * Start the drawing surface in multiplayer mode
	 **/
	public void startMultiPlayer() {
		multiplayer.setEnabled(true);
		Intent intent = new Intent(Main.this, DrawingActivityMulti.class);
		startActivityForResult(intent, DrawingActivityMulti.APPLICATION_ID);
	}
	
	/**
	 * Show toast if connection error
	 */
	public void failedMultiPlayer() {
		multiplayer.setEnabled(true);
		Toast.makeText(this, R.string.con_err, Toast.LENGTH_LONG)
		 .show();
	}
	
	/**
	 * If a device has been chosen, try to connect with it.
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D) Log.d(tag, "--- got an result!");
		
		switch(requestCode) {			
			case BluetoothHandler.REQUEST_ADDRESS:
				if (D) Log.d(tag, "-- BT message...");
				
				if (resultCode == Activity.RESULT_OK) {
					String macAddressHis = data
							.getExtras()
							.getString(DeviceListActivity.DEVICE_ADDRESS);
					
					remoteDevice = bluetoothAdapter.getRemoteDevice(macAddressHis);
					bluetoothHandling.connect(remoteDevice);
				} else {
					multiplayer.setEnabled(true);
					if(finReq) {
						bluetoothHandling.lock.release();
						bluetoothHandling.stop();
					}
				}
				break;
				
			case DrawingActivityMulti.APPLICATION_ID:
				if (D) Log.d(tag, "-- TCP message...");
				
				if (resultCode == Activity.RESULT_OK) {
					if (D) Log.d(tag, "- trying to start new game...");
					
					multiplayer.setEnabled(false);
					initMultiPlayer();
				}
				break;
		}
	}
	
	/** 
	 * Class for connecting to other users by server or bluetooth
	 **/
	private class ConnectThread extends Thread {

		private Handler handler = new Handler(Looper.getMainLooper());
		private final String C_THREAD = "ConnectThread";
		
		/**
		 * TODO: Add description
		 * @param C_THREAD 
		 */
		@SuppressWarnings("static-access")
		@Override
		public void run() {
			setName(C_THREAD);
			if(controller == MultiController.TCP) {
				networkHandling = new NetworkHandling(tcpHandler);
				networkHandling.establishConnection();
				
				try { 
					//Wait until the lock is released - wait for connection
					networkHandling.lock.acquire(); 
				} catch (InterruptedException e) { }
		
				finished(networkHandling.isConnected());
			}
			
			if(controller == MultiController.BLUETOOTH) {
				bluetoothHandling = new BluetoothHandling(getApplicationContext(),bluetoothHandler);
				bluetoothHandling.start();
				
				Intent intent = new Intent(getApplicationContext(),DeviceListActivity.class);
				startActivityForResult(intent, BluetoothHandler.REQUEST_ADDRESS);
				
				finReq = true;
				
				try {
					//Wait until the lock is released - wait for connection
					bluetoothHandling.lock.acquire();
				} catch (InterruptedException e) {
				}
								
				finished(bluetoothHandling.getState() == BluetoothHandling.STATE_CONNECTED);
			}
		}
		
		/**
		 * TODO: Add description
		 */
		public void finished(final boolean result) {
			handler.post(new Runnable() {
				public void run() {
				if (result) {
					if (controller == MultiController.BLUETOOTH) {
						finishActivity(BluetoothHandler.REQUEST_ADDRESS);
						finReq = false;
					}
					startMultiPlayer();
				} else {
					failedMultiPlayer();
					finReq =  false;
				}		
			}});
		}
	}
	
	/**
	 * Return the bluetooth handler
	 **/
	public static BluetoothHandling getBTHandler() {
		return bluetoothHandling;
	}
	
	/** 
	 * Return the network handler
	 **/
	public static NetworkHandling getTCPHandler() {
		return networkHandling;
	}
}