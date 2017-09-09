package com.kandidat.rityta.zoom;

import java.util.Observable;


public class ZoomRatio extends Observable {

	private float zoomRatio = 1f;
	
	/**
	 * Calculates the ratio between given view and content size, if this has changed from
	 * previously we mark this observable as changed.
	 */
	public void updateZoomRatio(float viewWidth, float viewHeight, float contentWidth, float contentHeight) {
		final float ratio = (contentWidth / contentHeight) / (viewWidth / viewHeight);
		
		if (zoomRatio != ratio) {
			zoomRatio = ratio;
			setChanged();
		}
	}
	
	// ------------------------- Getters & Setters ----------------------- //
	
	public float get() {
		return zoomRatio;
	}
}
