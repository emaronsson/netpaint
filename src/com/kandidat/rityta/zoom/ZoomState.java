package com.kandidat.rityta.zoom;

import java.util.Observable;


public class ZoomState extends Observable {
	
	private float zoomScale;
	private float panX;
	private float panY;
	
	public float getZoomX(float ratio) {
		return Math.min(zoomScale, zoomScale * ratio);
	}
	
	public float getZoomY(float ratio) {
		return Math.min(zoomScale, zoomScale / ratio);
	}
	
	// ------------------------ Getters & Setters ------------------------ //
	
	public float getZoom() {
		return zoomScale;
	}
	
	public void setZoom(float zoomScale) {
		if (this.zoomScale != zoomScale) {
			this.zoomScale = zoomScale;
			setChanged();
		}
	}
	
	public float getPanX() {
		return panX;
	}
	
	public void setPanX(float panX) {
		if (this.panX != panX) {
			this.panX = panX;
			setChanged();
		}
	}
	
	public float getPanY() {
		return panY;
	}
	
	public void setPanY(float panY) {
		if (this.panY != panY) {
			this.panY = panY;
			setChanged();
		}
	}
}
