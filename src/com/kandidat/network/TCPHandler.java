package com.kandidat.network;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import messagepacket.MessagePacket;
import com.kandidat.rityta.DrawingActivity;
import com.kandidat.rityta.multi.DrawingActivityMulti;

/**
 * Class for handling messages for the DrawingActivity
 * sent via TCP.
 */
public class TCPHandler extends Handler {
	
	//Debugging
	private final boolean D  = false;
	private final String tag = "TCPHandler";
	
	public static final int MESSAGE_READ = 1;
	
	private DrawingActivityMulti draw = null;
	
	public void init(DrawingActivity draw) {
		if (D) Log.d(tag, "- onCreate() -");
		
		this.draw = (DrawingActivityMulti) draw;
	}

	@Override
	public void handleMessage(Message message) {
		if (D) Log.d(tag, "--- handleMessage");
		if (draw == null)
			return;
		
		switch(message.what) {
			case MESSAGE_READ:
				MessagePacket msg = (MessagePacket) message.obj;
				
				if (D) Log.d(tag, "- message type: "+msg.getMsgId());
				
				draw.handleMessage(msg);
				break;
		}		
	}
}