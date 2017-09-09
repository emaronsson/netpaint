package com.kandidat.network;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;

/**
 * Class for storing the outgoing coordinates of drawn lines.
 */
public class PointContainerOut {

	private List<Point> coords;
	
	public PointContainerOut() {
		coords = new ArrayList<Point>();
	}
	
	/**
	 * Add an array of coordinates to point list, trims array if off odd length 
	 **/
	public synchronized void addPoints(short[] array) {
	//	if (array.length % 2 != 0)
	//		addPoints(Arrays.copyOf(array, array.length - 1));
		
		for (int i = 0; i < (array.length-1); i += 2) {
			if (array[i] > 0 && array[i+1] > 0)
				coords.add(new Point(array[i], array[i+1]));
		}
	}
	
	/**
	 * Add an array of points to point list 
	 **/
	public synchronized void addPoints(Point[] array) {
		for (Point p : array) {
			if (p != null && (p.x > 0 && p.y > 0))
				coords.add(new Point(p));
		}
	}
	
	/**
	 * Return true if the list of coordinates is empty.
	 */
	public synchronized boolean isEmpty() {
		return coords.isEmpty();
	}
	
	/**
	 * Clear the list of coordinates.
	 */
	public synchronized void clearPoints() {
		coords.clear();
	}
	
	/**
	 * Return an array of shorts from the coordinate list.
	 */
	public synchronized short[] getCoordsListInt() {
		return getCoordsListInt(coords);
	}
	
	/**
	 * Return an array of shorts from the coordinate list.
	 */
	public synchronized short[] getCoordsListInt(List<Point> points) {
		short[] shortCoords = new short[points.size() * 2];
		int j = 0;
		Point p;
		for (int i = 0; i < points.size(); i++) {
			p = points.get(i); 
			shortCoords[j++] = (short) p.x;
			shortCoords[j++] = (short) p.y;
		}
		
		return shortCoords;
	}
}
