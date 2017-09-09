package com.kandidat.rityta.multi;

import java.util.Queue;

import android.graphics.Point;

/**
 * Class for holding a task - point to be drawn.
 */
public class PointPainterTask {

	private Queue<Point> list;
	private long timeStamp;
	private short pID;
	private byte ID;
	
	public PointPainterTask(Queue<Point> list, long timeStamp, short pID, byte ID) {
		this.list = list;
		this.timeStamp = timeStamp;
		this.pID = pID;
		this.ID = ID;
	}

	/**
	 * return the points.
	 */
	public Queue<Point> getList() {
		return list;
	}

	/**
	 * Return the timestamp.
	 */
	public long getTimeStamp() {
		return timeStamp;
	}
	
	/**
	 * Return the path ID.
	 */
	public short getpID() {
		return pID;
	}
	
	/**
	 * Return player ID.
	 */
	public byte getID() {
		return ID;
	}
}
