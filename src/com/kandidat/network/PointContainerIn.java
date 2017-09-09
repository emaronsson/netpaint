package com.kandidat.network;

import java.util.LinkedList;
import java.util.Queue;

import messagepacket.DataMessage;
import android.graphics.Point;
import android.util.Log;

import com.kandidat.rityta.DrawingActivity;
import com.kandidat.rityta.multi.DrawingActivityMulti;
import com.kandidat.rityta.multi.Player;
import com.kandidat.rityta.multi.PointPainterTask;

/**
 * Class for storing the incoming coordinates of drawn lines.
 */
public class PointContainerIn {
	
	// Debugging
	private final boolean D  = false;
	private final String tag = "PointContainerIn";
	
	private DrawingActivityMulti activity;
	
	public PointContainerIn(DrawingActivity activity) {
		this.activity = (DrawingActivityMulti) activity;
	}
	
	/**
	 * Create a Player and a PointPainterTask from a message.
	 * Assign the painter of the player to perform the PointPainterTask.
	 */
	public synchronized void processMessage(DataMessage msg) {
		final byte ID = msg.getPlayerId();
		final short pID = msg.getPathId();
		final long timeStamp = msg.getTimeStamp();
		
		Player player = activity.getPlayerSafely(ID);
		
		if (D) Log.d(tag, "-- Player" +
				"\n- player id: "+player.getPlayerID() +
				"\n- is real? "+(player!=null) +
				"\n-- Task" +
				"\n- path id: "+pID +
				"\n- time stamp: "+timeStamp);
		
		if (player != null) {
			PointPainterTask ppt = new PointPainterTask(
							createPointList(msg.getCoordinates()),
							timeStamp,
							pID,
							ID);
			
			player.getPainter().put(ppt);
		}
	}
	
	/**
	 * Create a queue out of an array.
	 */
	private Queue<Point> createPointList(short[] array) {
			Queue<Point> list = new LinkedList<Point>();
			for (int i = 0; i < array.length; i += 2)
				list.add(new Point((int) array[i], (int) array[i+1]));
			
			return list;
	}
}
