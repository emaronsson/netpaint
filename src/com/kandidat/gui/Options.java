package com.kandidat.gui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kandidat.R;
import com.kandidat.network.BluetoothHandler;

/**
 * Class for the options activity.
 */
public class Options extends Activity {
	
	private Toast umts_3g, bluetooth;
	
	private Typeface tf;
	
	private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	private RadioGroup radioGroup;
	private RadioButton umts;
	private RadioButton blue;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_options);
		
		//Set the typeface for title and radio buttons.
		tf = Typeface.createFromAsset(getAssets(),
                "fonts/PAINP___.TTF");
		
        TextView tv = (TextView) findViewById(R.id.header_options);
        tv.setTypeface(tf);
        
        TextView tv2 = (TextView) findViewById(R.id.header_options2);
        tv2.setTypeface(tf);
        
        umts = (RadioButton) findViewById(R.id.btn_umts);
        umts.setTypeface(tf);
        umts.setText(getString(R.string.btn_umts1) + "\n"+
        			 getString(R.string.btn_umts2));
        
        blue = (RadioButton) findViewById(R.id.btn_bt);
        blue.setTypeface(tf);
        blue.setText(getString(R.string.btn_bt) + "\n"+
   			 getString(R.string.btn_bt2));
		
		if (Main.controller == Main.MultiController.TCP)
			umts.setChecked(true);
		else if (Main.controller == Main.MultiController.BLUETOOTH)
			blue.setChecked(true);
		
		radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			/**
			 * Determine what will happen when selecting an alternative for transmission.
			 */
			@Override
			public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
				if (umts.isChecked()) {
					Main.controller = Main.MultiController.TCP;
					umts_3g.show();
				}
				else if(blue.isChecked()) {
				     if (bluetoothAdapter.getScanMode() !=
				    		 BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
				             Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				             discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
				             startActivity(discoverableIntent);
				     }	
						Main.controller = Main.MultiController.BLUETOOTH;
						bluetooth.show();
				}
			}
		});
		
		// set up Toasts for future use
		initToasts();
	}
	
	/**
	 * If one cannot connect via bluetooth, 3G is chosen instead.
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
			case BluetoothHandler.REQUEST_ENABLE_BLUETOOTH:
				if(resultCode == Activity.RESULT_CANCELED) {
					Main.controller = Main.MultiController.TCP;
					Toast.makeText(this, R.string.bt_error, Toast.LENGTH_SHORT).show();
				}
				break;
		}
	}	
	
	/**
	 * Set the text of the toasts.
	 */
	private void initToasts() {
		umts_3g   = Toast.makeText(Options.this, 
						R.string.txt_umts_chosen,
						Toast.LENGTH_SHORT);
		bluetooth = Toast.makeText(Options.this, 
						R.string.txt_btooth_chosen, 
						Toast.LENGTH_SHORT);
	}
}
