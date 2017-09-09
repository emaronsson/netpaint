package com.kandidat.rityta.multi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import messagepacket.MessagePacket;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;

import com.kandidat.rityta.DrawingActivity;
import com.kandidat.rityta.DrawingPath;
import com.kandidat.rityta.DrawingView;


public class DrawingViewMulti extends DrawingView {

	// Debugging
	private final boolean D  = false;
	private final String tag = "DrawingViewMulti";
	
	/** Max number of points before a point block is sent */
	public static final int POINT_BLOCKSIZE = 20;
	
	/** Storage for drawn points */
	private Point[] points;
	private int     pIndex;
	
	// ------------------------- Constructors ---------------------------- //
	
	public DrawingViewMulti(Context context) {
		super(context);
	}
	
	public DrawingViewMulti(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}
	
	@Override
	protected void init(Context context) {
		super.init(context);
		
		points = new Point[POINT_BLOCKSIZE];
		pIndex = 0;
	}

	// ---------------------- Multiplayer methods ------------------------ //

	/**
	 * Removes the latest path this user has drawn and notifies
	 * others of its' removal.
	 */
	@Override
	public void undo() {
		final byte mRID = DrawingActivityMulti.getMyRID();
		
		if (D) {
			Log.d(tag, "---- undo --------------------"
				+"\n- my rID: "+mRID
				+"\n--- all paths in reverse order:");
			
			synchronized (DrawingActivity.path) {
				for (int i = DrawingActivity.path.size() - 1; i >= 0; i--) {
					DrawingPath dp = DrawingActivity.path.get(i);
					if(D){Log.d(tag+"-loop:all", "-- Path"
							+"\n- path ID: "+dp.getPID()
							+"\n- path rID: "+(byte) ((dp.getPID() & 0xF000) >>> 12));}
				}
			}
		}
		
		if (D) Log.d(tag, "--- searching all saved paths...");
		
		synchronized (DrawingActivity.path) {
			for (int i = DrawingActivity.path.size() - 1; i >= 0; i--) {
				final short pID = DrawingActivity.path.get(i).getPID();
				final byte  rID = (byte) ((pID & 0xF000) >>> 12);
				
				if (D) Log.d(tag+"-loop:delete", "-- compare path:"
						+"\n- path ID: "+pID
						+"\n- path rID: "+rID);
				
				if (rID == mRID) {
					if (D) Log.d(tag+"-loop:delete", "-- found path");
						
					undo(i);
					DrawingActivityMulti.sendMessage(new MessagePacket(pID));
					break;
				}
			}
		}
	}
	
	/**
	 * Redos the path at the specific index and returns it, 
	 * if the path isn't null we notify the others of the redo.
	 */
	@Override
	public DrawingPath redo(int index) {
		if (D) Log.d(tag, "--- redo --------------------");
			
		DrawingPath redo = super.redo(index);
		if (redo != null) {
			List<Point> points = redo.getCoords();
			
			if (D) {
				Log.d(tag+"-loop", "-- checking path coords...");
				for (Point p : points)
					Log.d(tag+"-loop", "- ("+p.x+", "+p.y+")");
			}
			
			short[] coords = DrawingActivityMulti
					.getContainerOut()
					.getCoordsListInt(points);

			final int color = redo.getBrush().getPaint().getColor();
			final short a = (short) ((color & 0xFF000000) >>> 24);
			final short r = (short) ((color & 0x00FF0000) >>> 16);
			final short g = (short) ((color & 0x0000FF00) >>> 8);
			final short b = (short)  (color & 0x000000FF);

			MessagePacket msg = new MessagePacket(
					coords,
					redo.getPID(),
					redo.getTimeStamp(),
					DrawingActivityMulti.convertToByte(a),
					DrawingActivityMulti.convertToByte(r), 
					DrawingActivityMulti.convertToByte(g), 
					DrawingActivityMulti.convertToByte(b), 
					(byte) redo.getBrush().getPaint().getStrokeWidth(),
					redo.getBrush().getActiveEffects());

			if (D) Log.d(tag, "sending redo message...");
			
			DrawingActivityMulti.sendMessage(msg);
		}
		
		return redo;
	}
	
	/**
	 * Draws a path and adds it to the path list.
	 */
	@Override
	protected void drawPath(DrawingPath path) {
		drawPath(path, true);
	}
	
	/**
	 * Draws a path and adds it to the path list if desired.
	 */
	protected void drawPath(DrawingPath path, boolean add) {
		final int i = findPathPlace(path.getTimeStamp());
		
		if (D) Log.d(tag, "--- drawPath --------------------"
				+ "\n- Add: "+add
				+ "\n- Index: "+i);
		
		if (i < 0) {
			canvas.drawPath(path.getPath(), path.getBrush().getPaint());

			if (add)
				synchronized (DrawingActivity.path) {
					DrawingActivity.path.add(path.clone());
				}
			
			invalidate();
		} else {
			if (add) {
				synchronized (DrawingActivity.path) {
					DrawingActivity.path.add(i, path.clone());
				}
	
				redrawPaths();
			}
		}
	}

	/**
	 * Calculates where in the path list the time stamp belongs, 
	 * and returns that index.
	 */
	public int findPathPlace(final long timeStamp) {
		if (DrawingActivity.path.isEmpty())
			return -1;
		if (DrawingActivity.path.get(DrawingActivity.path.size() - 1).getTimeStamp() < timeStamp)
			return -1;

		synchronized (DrawingActivity.path) {
			int i = DrawingActivity.path.size() - 1;
			for (; i > 0 && DrawingActivity.path.get(i).getTimeStamp() > timeStamp; i--);
			return i;
		}
	}
	
	/**
	 * Calculates where in the specific list the time stamp belongs, 
	 * and returns that index.
	 */
	private int findPlaceSorted(final long timeStamp, List<DrawingPath> list) {
		if (list.isEmpty())
			return -1;
		if (list.get(list.size() - 1).getTimeStamp() < timeStamp)
			return -1;

		int i = list.size() - 1;
		for (; i > 0 && list.get(i).getTimeStamp() > timeStamp; i--);
		return i;
	}
	
	/**
	 * Redraws all drawn paths as well as the sent path at the
	 * specified index, and background, to the canvas and invalidates.
	 */
	@Override
	public void redrawPaths() {
		if (D) Log.d(tag, "--- redrawPaths --------------------");
		
		canvas.drawColor(0xFFFFFFFF);
		
		// Copy background to buffer
		if(background != null) {
			if (D) Log.d(tag, "- drawing background...");
			bitmap.copyPixelsFromBuffer(background);
		}
		
		if (D) Log.d(tag, "-- fetching all paths to draw...");
		
		// Fetch all saved paths to a new list
		List<DrawingPath> paths;
		synchronized (DrawingActivity.path) {
			if (D) Log.d(tag+"-fetch", "- path buffer empty: "+DrawingActivity.path.isEmpty());
			
			if (DrawingActivity.path.isEmpty())
				paths = new ArrayList<DrawingPath>();
			else
				paths = new ArrayList<DrawingPath>(DrawingActivity.path);
			
			if (D) Log.d(tag+"-fetch", "- nmbr of fetched paths: "+paths.size());
		}
		
		// Since not all paths have been saved yet, fetch these and insert
		// them at their proper place
		synchronized (DrawingActivityMulti.players) {
			if (paths.isEmpty()) {
				if (D) Log.d(tag+"-fetch", "- adding all player paths and sorting");
				
				for (Player p : DrawingActivityMulti.players)
					if (!p.getPath().getPath().isEmpty())
						paths.add(p.getPath());
				Collections.sort(paths);
						
				if (D) {
					
				}
			} else {
				if (D) Log.d(tag+"-fetch", "- adding all players in the correct spot");
				
				for (Player p : DrawingActivityMulti.players) {
					if (p.getPath().getPath().isEmpty())
						continue;
					
					final int index = findPlaceSorted(p.getPath().getTimeStamp(), paths);
					
					if (D) {Log.d(tag+"-fetch", "-- Added Player"
							+ "\n- player ID: "+p.getPlayerID()
							+ "\n- path empty: "+p.getPath().getPath().isEmpty()
							+ "\n- time_stamp: "+p.getPath().getTimeStamp()
							+" \n- index: "+index);}
					
					if (index > 0)
						paths.add(index, p.getPath());
					else
						paths.add(p.getPath());	
				}
			}
		}
		
		if (D) {
			Log.d(tag+"-loop", "-- Paths to draw: ");
			if (paths.isEmpty())
				Log.d(tag+"-loop", "- none, paths is empty...");
			for (DrawingPath dp : paths)
				Log.d(tag+"-loop:paths", "- Path ID: "+dp.getPID()
						+ "\n- Path time_stamp: "+dp.getTimeStamp());
		}
		
		// And finally draw the paths
		for (DrawingPath dp : paths)
			canvas.drawPath(dp.getPath(), dp.getBrush().getPaint());
		
		invalidate();
	}
	
	// ------------------------- Touch methods --------------------------- //
	
	@Override
	protected void saveCoordinates(float x, float y) {
		super.saveCoordinates(x, y);
		
		// Store intercepted points
		storePoints((int) x, (int) y);
		if (zoomEnabled)
			scaledPath.addPoint(x, y);
		else
			currPath.addPoint(x, y);
	}
	
	@Override
	public void touch_down(float x, float y) {
		super.touch_down(x, y);
		
		// Set the pID of the new drawing path
		short newPID = DrawingActivityMulti.getNewPID();
		currPath.setPID(newPID);
		scaledPath.setPID(newPID);
	}

	@Override
	public void touch_up() {
		super.touch_up();
		
		// Store and force-send saved points
		storePoints(getLastPoint());
		sendPoints(currPath.getPID(), currPath.getTimeStamp());
	}
	
	// -------------------- PointContainer methods ----------------------- //
	
	private void storePoints(PointF p) {
		storePoints((int) p.x, (int) p.y);
	}
	
	private void storePoints(int x, int y) {
		if (pIndex == POINT_BLOCKSIZE) {
			sendPoints(currPath.getPID(), currPath.getTimeStamp());
			clearPoints();
		}
		
		points[pIndex++] = new Point(x, y);
	}
	
	private void clearPoints() {
		pIndex = 0;
		Point tmp = new Point(-1, 0);
		for (int i = 0; i < points.length; i++)
			points[i] = tmp;
	}

	private void sendPoints(short index, long time) {
		DrawingActivityMulti.addPointsToContainer(points);
		DrawingActivityMulti.sendPointContainer(index, time);
		clearPoints();
	}
}
