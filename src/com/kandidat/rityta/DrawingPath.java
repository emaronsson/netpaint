package com.kandidat.rityta;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;

/**
 * Class for representing a path with a certain brush.
 */
public class DrawingPath implements Cloneable, Comparable<DrawingPath> {
	
	// Debugging
	private final boolean D  = false;
	private final String tag = "DrawingPath";
		
	private Path  path;
	private Brush brush;
	private List<Point> coords;
	
	private long  timeStamp;
	private short pID;
	
	public DrawingPath() {
		this(0, Short.MIN_VALUE);
	}
	
	public DrawingPath(long timeStamp, short pID) {
		this.timeStamp = timeStamp;
		this.pID = pID;
		
		path  = new Path();
		brush = new Brush(10f, Color.BLACK);
		coords = new ArrayList<Point>();
	}
	
	public void createPath(short[] coords, long timeStamp, short pID) {
		this.timeStamp = timeStamp;
		this.pID = pID;

		if (D) Log.d(tag, "--- createPath" +
				"\n- timeStamp: "+timeStamp +
				"\n- pID: "+pID +
				"\n- points: "+ coords.length);
		
		// Fetch first point
		Point point = new Point(coords[0], coords[1]);
				
		// move to start point on path
		path.moveTo(point.x, point.y);

		// Store points
		int mX = point.x;
		int mY = point.y;
		
		for (int i = 2; i < coords.length - 3; i += 2) {
			// Fetch next point
			point = new Point(coords[i], coords[i+1]);

			// Add coordinate to path and save points
			path.quadTo(mX, mY, (point.x + mX)/2, (point.y + mY)/2);
			mX = point.x;
			mY = point.y;
		}
		// Finish path with lineTo last coordinate
		path.lineTo(mX, mY);
	}
	
	public void addPoint(float x, float y) {
		coords.add(new Point((int) x, (int) y));
	}
	
	public List<Point> getCoords() {
		return coords;
	}
	
	public void clearCoords() {
		coords.clear();
	}
	
	// ------------------------- Compareable ----------------------------- //

	@Override
	public int compareTo(DrawingPath another) {
		if (timeStamp > another.timeStamp)
			return 1;
		else if (timeStamp < another.timeStamp)
			return -1;
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		DrawingPath other = (DrawingPath) obj;
		if (pID != other.pID)
			return false;
		return true;
	}
	
	// -------------------------- Cloneable ------------------------------ //

	@Override
	public DrawingPath clone() {
		try {
			DrawingPath result = (DrawingPath) super.clone();
			result.brush  = this.brush.clone();
			result.path   = new Path(path);
			result.coords = new ArrayList<Point>(coords.size());
			for (Point p : coords)
				result.coords.add(new Point(p));
			
			return result;
		} catch (CloneNotSupportedException ignored) { return null; }
	}
	
	// ----------------------- Getters & Setters ------------------------- //
	
	public Path getPath() {
		return path;
	}
	
	public void setPath(Path path) {
		this.path = path;
	}
	
	public void addPath(Path path) {
		this.path.addPath(path);
	}
	
	public Brush getBrush() {
		return brush;
	}
	
	public void setBrush(Brush brush) {
		this.brush = brush;
	}

	public long getTimeStamp() {
		return timeStamp;
	}
	
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public short getPID() {
		return pID;
	}
	
	public void setPID(short pID) {
		this.pID = pID;
	}

	public Point getLastPoint() {
		return new Point(coords.get(coords.size() - 1));
	}
}