package com.kandidat.network.client;

import java.util.Set;

import com.kandidat.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/*
 * Activity for searching for and selecting devices 
 * to connect to.
 */
public class DeviceListActivity extends Activity {
	public static String DEVICE_ADDRESS = "device_address";
	
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> pairedDevices;
    private ArrayAdapter<String> newDevices;
    
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	setContentView(R.layout.device_list);
    	
    	setResult(Activity.RESULT_CANCELED);
    	
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                discover();
                v.setVisibility(View.VISIBLE);
            }
        });
        
        // Initiate and assign ArrayAdapters to the ListViews
        pairedDevices = new ArrayAdapter<String>(this,R.layout.device_name);
        newDevices = new ArrayAdapter<String>(this,R.layout.device_name);
        
        // ListView for the known devices
        ListView pairedDevicesList = (ListView) findViewById(R.id.paired_devices);
        pairedDevicesList.setAdapter(pairedDevices);
        pairedDevicesList.setOnItemClickListener(listViewClickListener);
        
        // ListView for the new devices
        ListView newDevicesList = (ListView) findViewById(R.id.new_devices);
        newDevicesList.setAdapter(newDevices);
        newDevicesList.setOnItemClickListener(listViewClickListener);
        
        // To receive a broadcast that a bluetooth device has been found
        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(discoveryReceiver,foundFilter);
        
        // To receive a broadcast that the bluetooth adapter has stopped scanning for devices
        IntentFilter finishedFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(discoveryReceiver,finishedFilter);
        
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> alreadyPaired = bluetoothAdapter.getBondedDevices();
        
        if(alreadyPaired.size() > 0) {
        	findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
        	for(BluetoothDevice device : alreadyPaired) {
        		pairedDevices.add(device.getName() + "\n" + device.getAddress());
        	}
        }
        else {
    		String noPairedDevices = getResources().getText(R.string.none_paired).toString();
    		pairedDevices.add(noPairedDevices);
        }
    }
    
    /**
     * Scan for other devices with bluetooth.
     */
    private void discover() {
    	setProgressBarIndeterminateVisibility(true);
    	setTitle(R.string.scanning);
    	
    	findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
    	
    	// If the device is already in progress of discovering new devices, cancel this new attempt
    	if(bluetoothAdapter.isDiscovering()) {
    		bluetoothAdapter.cancelDiscovery();
    	}
    	
    	// Go ahead and start discovering
    	bluetoothAdapter.startDiscovery();
    }
    
    /**
     * A device to connect to has been chosen.
     * Quit this activity.
     */
    private OnItemClickListener listViewClickListener = new OnItemClickListener() {
    	public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
    		// When we answer a call to this function the user has already selected a device
    		// to connect to. Therefore we don't need to keep discovering new devices.
    		bluetoothAdapter.cancelDiscovery();
    		
    		// Get the device name and address from the TextView the user just selected
    		String deviceInfo = ((TextView) v).getText().toString();
    		String macAddress = deviceInfo.substring(deviceInfo.length() - 17);
    		
    		// Create a new intent and assign the chosen device MAC address to it
    		Intent addressIntent = new Intent();
    		addressIntent.putExtra(DEVICE_ADDRESS, macAddress);
    		
    		// Us setResult to inform the parent activity (NetworkOptions) that an address has been
    		// found. NetworkOptions will answer this in startActivityForResult.
    		setResult(Activity.RESULT_OK, addressIntent);
    		finish();
    	}
    };
    
    /**
     * Receive messages if a device was found during searching, or if 
     * searching has finished.
     */
    private BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			// The BluetoothDevice signaled that another device has been found
			if(BluetoothDevice.ACTION_FOUND.equals(action)) {
				// From the intent, get the device info
				BluetoothDevice newDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				boolean exists = false;
				
				for(int i = 0; i < newDevices.getCount(); i++) {
					String device = newDevices.getItem(i);
					
					if(device.equalsIgnoreCase(newDevice.getName() + "\n" + newDevice.getAddress())) {
						exists = true;
						break;
					}
				}
				
				// If the device already exists as a previously paired device, do not add it to the
				// newDevices ArrayAdapter, else go ahead and put it in.
				if(newDevice.getBondState() != BluetoothDevice.BOND_BONDED && !exists) {
					newDevices.add(newDevice.getName() + "\n" + newDevice.getAddress());
				}
			}
			
			// The BluetoothDevice signaled that it has finished scanning for new devices
			else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
		    	// Change the statusbar
				setProgressBarIndeterminateVisibility(false);
		    	setTitle(R.string.select_device);
		    	
		    	// If no new devices turned up, just add the string constant that no new devices were found
		    	if(newDevices.getCount() == 0) {
		    		String noNewDevices = getResources().getText(R.string.none_found).toString();
		    		newDevices.add(noNewDevices);
		    	}
			}
			
		}
	};
	
	/**
	 * Cancel the searching for devices when activity is closed.
	 */
	protected void onDestroy() {
		super.onDestroy();
		
		if(bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
			bluetoothAdapter.cancelDiscovery();
		}
		
		this.unregisterReceiver(discoveryReceiver);
	}
}
