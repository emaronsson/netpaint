package com.kandidat.rityta;

import java.util.List;

/**
 * Class for keeping the list of paths
 * and current brush.
 */
public class DrawingState {
	
	private final List<DrawingPath> path;
	private final List<DrawingPath> redo;
	private final Brush brush;
	
	public DrawingState(DrawingView v) {
		//TODO: These needs to be cloned.
		path  = DrawingActivity.path;
		redo  = DrawingActivity.redo;
		brush = v.getCurrentBrush();
	}
	
	/**
	 * Return the drawn paths.
	 */
	public List<DrawingPath> getPathListFromSavedState() {
		return path;
	}
	
	/**
	 * Return the redo list.
	 */
	public List<DrawingPath> getRedoListFromSavedState() {
		return redo;
	}
	
	/**
	 * Return the saved brush.
	 */
	public Brush getBrushFromSavedState() {
		return brush;
	}
}