package com.kandidat.network;

import messagepacket.IdCode;
import messagepacket.MessagePacket;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.kandidat.R;
import com.kandidat.rityta.DrawingActivity;
import com.kandidat.rityta.multi.DrawingActivityMulti;

/**
 * Class for handling messages for the 
 * DrawingActivity sent via bluetooth.
 */
public class BluetoothHandler extends Handler {
	
    public static final int MESSAGE_DEVICE_NAME = 3;
    public static final int MESSAGE_TOAST = 4;
	public static final int MESSAGE_READ = 5;
	public static final int REQUEST_ENABLE_BLUETOOTH = 6;
	public static final int REQUEST_ADDRESS = 7;
	public static final int REQUEST_DEVICE_NAME = 8;
	
	public static final String DEVICE_NAME = "";
	public static final String TOAST = "";
	
	private final String CONNECTED_TO = "Ihopkopplad med: ";
	
	private DrawingActivityMulti draw = null;

	/**
	 * Initialize - send a message to oneself that the connecting went ok.
	 */
	public void init(DrawingActivity draw, byte ID) {
		this.draw = (DrawingActivityMulti) draw;
		
		MessagePacket msg = new MessagePacket(
				(byte) IdCode.JOIN_SUCCESS, ID, 
				(byte) 2);
		
		this.draw.handleMessage(msg);
	}
		
	/**
	 * Handle incoming messages of different types.
	 */
	@Override
	public void handleMessage(Message message) {
		if (draw == null)
			return;
		
		switch(message.what) {
			case MESSAGE_READ:
				MessagePacket msg = (MessagePacket) message.obj;
				draw.handleMessage(msg);
				break;
			
			case MESSAGE_DEVICE_NAME:
				Toast.makeText(draw, CONNECTED_TO + message.getData().getString(DEVICE_NAME), 
								Toast.LENGTH_SHORT).show();
				break;
				
	        case MESSAGE_TOAST:
	            int toastNR = message.getData().getInt(TOAST);
	            switch(toastNR){
	            
	            	case 0: Toast.makeText(draw, R.string.connect_fail,
	                           Toast.LENGTH_SHORT).show();
	            			break;
	            	case 1: Toast.makeText(draw, R.string.connect_lost,
	                           Toast.LENGTH_SHORT).show();
	            }
	            break;
		}
	}	
}
