package com.kandidat.rityta.multi;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import android.graphics.Point;
import android.util.Log;

import com.kandidat.rityta.DrawingActivity;
import com.kandidat.rityta.DrawingPath;
import com.kandidat.rityta.DrawingView;

/**
 * Fetches PointPainterTasks and performs them.
 */
public class PointPainter {
	
	// Debugging
	private final boolean D  = false;
	private final String tag = "PointPainter"+(threadID++);
	
	/** Thread used to continually draw received points */
	private Thread painterThread;
	
	/** Integer used to number the painter threads */
	private static int threadID = 0;
	
	/** Integer used to number the worker runnables */
	private static int runnableIndex = 0; 
	
	/** Used to keep track of when the drawing jobs' finished */
	private Semaphore finished = new Semaphore(0);
	
	/** Queue containing all the paint-tasks waiting to be drawn */
	private Queue<PointPainterTask> taskQueue;
	
	/** Reference to view we will draw too */
	private DrawingView view;
	
	/** Boolean that's true if there are more paint-tasks to be drawn */
	private boolean run = false;
	
	/** Boolean that's true while the painter thread should be running */
	private boolean running = true;
	
	public PointPainter(DrawingView view) {
		this.view = view;
		taskQueue = new LinkedList<PointPainterTask>();
		
		painterThread = new Thread(new PaintRunnable());
		painterThread.setName("PaintRunnableThread"+threadID);
		painterThread.start();
	}
	
	// -------------------------- Get & Put ------------------------------ //
	
	/**
	 * Wait for a task to be available, and then return it.
	 */
	public synchronized PointPainterTask get() {
		if (!run) {
			try { wait(); }
			catch (InterruptedException ignored) { }
		}
		
		PointPainterTask task = taskQueue.poll();
		if (task == null)
			return null;
		
		if (D) Log.d(tag, "--- get"
				+ "\n--Task\n- player id: "+task.getID()
				+ "\n- path id: "+task.getpID()
				+ "\n- time stamp: "+task.getTimeStamp());
		
		if (taskQueue.isEmpty()) {
			if (D) Log.d(tag, "-- Queue is now empty");
			
			run = false;
		}
		
		return task;
	}

	/**
	 * Add a task to the queue, and notify that it is available.
	 */
	public synchronized void put(PointPainterTask ppt) {
		if (D) Log.d(tag, "--- put"
				+ "\n--Task\n- player id: "+ppt.getID()
				+ "\n- path id: "+ppt.getpID()
				+ "\n- time stamp: "+ppt.getTimeStamp());
		
		taskQueue.add(ppt);
		run = true;
		
		notify();
	}
	
	// -------------------------- Kill tasks ----------------------------- //
	
	/**
	 * Kill the painting thread.
	 */
	public synchronized void kill() {
		if (D) Log.d(tag, "- killing painter runnable...");
		
		running = false;
		painterThread.interrupt();
	}
	
	// ------------------------ Paint Runnable --------------------------- //
	
	/**
	 * Get new tasks, check which player it belongs to, check if the player's 
	 * current path is a new one, then save the old one.
	 */
	private class PaintRunnable implements Runnable {
		
		@Override
		public void run() {
			while (running) {
				if (D) Log.d(tag+"-runnable", "-## Fetching task...");
				PointPainterTask task = get();
				if (task != null) {
					
					if (D) Log.d(tag+"-runnable", "-## Got new task!"
							+ "\n-# Task player ID: "+task.getID()
							+ "\n-# Task path ID: "+task.getpID());
					
					// Fetch task owner
					Player player = DrawingActivityMulti.getPlayer(task.getID()); 
					
					if (D) Log.d(tag+"-runnable", "-## Got path owner"
							+ "\n-# Player ID: "+player.getPlayerID()
							+ "\n-# Player path ID: "+player.getPath().getPID());
					
					// If it is a new path, store the old
					if (player.getPath().getPID() < task.getpID()) {

						if (D) Log.d(tag+"-runnable", "-## Adding path and updating values...");
						
						// Store old path
						if (player.getPath().getPID() >= 0) {
							synchronized (DrawingActivity.path) {
								DrawingActivity.path.add(player.getPath().clone());
								
								if (D) {
									Log.d(tag+"-runnable:loop", "-## All current paths in list:");
									for (DrawingPath dp : DrawingActivity.path)
										Log.d(tag+"-runnable:loop", "-# Path ID: "+dp.getPID());
						}}}
						
						// Update and reset old path to new settings
						player.getPath().setPID(task.getpID());
						player.getPath().setTimeStamp(task.getTimeStamp());
						player.getPath().getPath().reset();
					}
					
					if (D) Log.d(tag+"-runnable", "-## Launching new draw runnable...");
					
					//Draw the path
					new DrawRunnable(player, task.getList(), runnableIndex++).run();
					
					if (D) Log.d(tag+"-runnable", "-## Waiting for task to finish...");
					
					// Wait for task to finish
					try { finished.acquire(); }
					catch (InterruptedException ignored) { }
					
					if (D) Log.d(tag+"-runnable", "-## Task is done!");
				}
			}

			if (D) Log.d(tag+"-runnable", "-## Painter died...");
		}

		/**
		 * Private class used to construct Paths from given list of points and
		 * append these to the given player's path. Draws them to the surface.
		 */
		private class DrawRunnable implements Runnable {
			private Queue<Point> points;
			private Player player;
			private int startSize;
			private int id;
			private float mX;
			private float mY;

			public DrawRunnable(Player player, Queue<Point> list, int id) {
				this.points  = new LinkedList<Point>(list);
				this.player  = player;
				this.id      = id;
				startSize    = points.size();
			}
			
			//Make the points into a path, and draw it
			@Override
			public void run() {
				if (startSize < 1)
					return;
				
				view.post(new Runnable() {
					public void run() {
						final DrawingPath path = player.getPath();

						if (D) Log.d(tag+"-draw"+id, "-%%% Running drawing runnable..."
									+ "\n-% Path ID: "+path.getPID()
									+ "\n-%% First point in path..."
									+ "\n-% Empty: "+path.getPath().isEmpty());
						
						// Fetch first point
						Point point = points.poll();
						
						// If new path; move to start point, otherwise continue on path
						if (path.getPath().isEmpty())
							path.getPath().moveTo(point.x, point.y);
						else
							path.getPath().lineTo(point.x, point.y);

						// Store points
						mX = point.x;
						mY = point.y;
						
						while (!points.isEmpty()) {
							if (D) Log.d(tag+"-draw"+id, "-%% Building on existing path..."
									+ "\n-% Points left: "+points.size());

							// Fetch next point
							point = points.poll();
							
							// Add coordinate to path and save points
							path.getPath().quadTo(mX, mY, (point.x + mX)/2, (point.y + mY)/2);
							mX = point.x;
							mY = point.y;
						}
						
						if (D) Log.d(tag+"-draw"+id, "-%% Finishing path...");

						// Finish path with lineTo last coordinate
						path.getPath().lineTo(mX, mY);

						// Draw the finish path to the surface
						((DrawingViewMulti) view).drawPath(path, false);

						// Invalidate and signal that we're done
						finished.release();
						view.invalidate();
				}});
				
				if (D) Log.d(tag+"-draw"+id, "-%% Runnable dies...");
			}
		}
	}
}