package com.kandidat.rityta.zoom;

import java.util.Observable;
import java.util.Observer;

import android.os.Handler;
import android.os.SystemClock;

import com.kandidat.rityta.zoom.util.SlingDynamics;

public class DynamicZoomControl implements Observer {

    /** Minimum zoom level limit */
    private static final float MIN_ZOOM = 1;

    /** Maximum zoom level limit */
    private static final float MAX_ZOOM = 10;

    /** Velocity tolerance for calculating if dynamic state is resting */
    private static final float REST_VELOCITY_TOLERANCE = 0.004f;

    /** Position tolerance for calculating if dynamic state is resting */
    private static final float REST_POSITION_TOLERANCE = 0.01f;

    /** Factor applied to pan motion outside of pan snap limits. */
    private static final float PAN_OUTSIDE_SNAP_FACTOR = .4f;
    
    /** Target FPS when animating behavior such as fling and snap to */
    private static final int FPS = 50;

    /** Zoom state under control */
    private final ZoomState mState = new ZoomState();

    /** Object holding aspect quotient of view and content */
    private ZoomRatio zoomRatio;

    /**
     * Dynamics object for creating dynamic fling and snap to behavior for pan
     * in x-dimension.
     */
    private final SlingDynamics panDynamicsX = new SlingDynamics();

    /**
     * Dynamics object for creating dynamic fling and snap to behavior for pan
     * in y-dimension.
     */
    private final SlingDynamics panDynamicsY = new SlingDynamics();

    /** Minimum snap to position for pan in x-dimension */
    private float panMinX;

    /** Maximum snap to position for pan in x-dimension */
    private float panMaxX;

    /** Minimum snap to position for pan in y-dimension */
    private float panMinY;

    /** Maximum snap to position for pan in y-dimension */
    private float panMaxY;

    /** Handler for posting runnables */
    private final Handler handler = new Handler();

    public DynamicZoomControl() {
        panDynamicsX.setFriction(2f);
        panDynamicsY.setFriction(2f);
        panDynamicsX.setSpring(50f, 1f);
        panDynamicsY.setSpring(50f, 1f);
    }
    
    public void zoom(float f, float x, float y) {
        final float aspectQuotient = zoomRatio.get();

        final float prevZoomX = mState.getZoomX(aspectQuotient);
        final float prevZoomY = mState.getZoomY(aspectQuotient);

        mState.setZoom(mState.getZoom() * f);
        limitZoom();

        final float newZoomX = mState.getZoomX(aspectQuotient);
        final float newZoomY = mState.getZoomY(aspectQuotient);

        // Pan to keep x and y coordinate invariant
        mState.setPanX(mState.getPanX() + (x - .5f) * (1f / prevZoomX - 1f / newZoomX));
        mState.setPanY(mState.getPanY() + (y - .5f) * (1f / prevZoomY - 1f / newZoomY));

        updatePanLimits();

        mState.notifyObservers();
    }

    public void pan(float dx, float dy) {
        final float aspectQuotient = zoomRatio.get();

        dx /= mState.getZoomX(aspectQuotient);
        dy /= mState.getZoomY(aspectQuotient);

        if (mState.getPanX() > panMaxX && dx > 0 || mState.getPanX() < panMinX && dx < 0) {
            dx *= PAN_OUTSIDE_SNAP_FACTOR;
        }
        if (mState.getPanY() > panMaxY && dy > 0 || mState.getPanY() < panMinY && dy < 0) {
            dy *= PAN_OUTSIDE_SNAP_FACTOR;
        }

        final float newPanX = mState.getPanX() + dx;
        final float newPanY = mState.getPanY() + dy;

        mState.setPanX(newPanX);
        mState.setPanY(newPanY);

        mState.notifyObservers();
    }

    /**
     * Runnable that updates dynamics state
     */
    private final Runnable updateRunnable = new Runnable() {
        public void run() {
            final long startTime = SystemClock.uptimeMillis();
            panDynamicsX.update(startTime);
            panDynamicsY.update(startTime);
            
            final boolean isAtRest = 
            		   panDynamicsX.isAtRest(REST_VELOCITY_TOLERANCE,REST_POSITION_TOLERANCE)
                    && panDynamicsY.isAtRest(REST_VELOCITY_TOLERANCE, REST_POSITION_TOLERANCE);
            mState.setPanX(panDynamicsX.getPosition());
            mState.setPanY(panDynamicsY.getPosition());

            if (!isAtRest) {
                final long stopTime = SystemClock.uptimeMillis();
                handler.postDelayed(updateRunnable, 1000 / FPS - (stopTime - startTime));
            }

            mState.notifyObservers();
        }
    };

    /**
     * Release control and start pan fling animation
     * 
     * @param vx Velocity in x-dimension
     * @param vy Velocity in y-dimension
     */
    public void startFling(float vx, float vy) {
        final float aspectQuotient = zoomRatio.get();
        final long now = SystemClock.uptimeMillis();

        panDynamicsX.setState(mState.getPanX(), vx / mState.getZoomX(aspectQuotient), now);
        panDynamicsY.setState(mState.getPanY(), vy / mState.getZoomY(aspectQuotient), now);

        panDynamicsX.setMinPosition(panMinX);
        panDynamicsX.setMaxPosition(panMaxX);
        panDynamicsY.setMinPosition(panMinY);
        panDynamicsY.setMaxPosition(panMaxY);

        handler.post(updateRunnable);
    }

    /**
     * Stop fling animation
     */
    public void stopFling() {
        handler.removeCallbacks(updateRunnable);
    }

    /**
     * Help function to figure out max delta of pan from center position.
     * 
     * @param zoom Zoom value
     * @return Max delta of pan
     */
    private float getMaxPanDelta(float zoom) {
        return Math.max(0f, .5f * ((zoom - 1) / zoom));
    }

    /**
     * Force zoom to stay within limits
     */
    private void limitZoom() {
        if (mState.getZoom() < MIN_ZOOM)
            mState.setZoom(MIN_ZOOM);
        else if (mState.getZoom() > MAX_ZOOM)
            mState.setZoom(MAX_ZOOM);
    }

    /**
     * Update limit values for pan
     */
    private void updatePanLimits() {
        final float ratio = zoomRatio.get();

        final float zoomX = mState.getZoomX(ratio);
        final float zoomY = mState.getZoomY(ratio);

        panMinX = .5f - getMaxPanDelta(zoomX);
        panMaxX = .5f + getMaxPanDelta(zoomX);
        panMinY = .5f - getMaxPanDelta(zoomY);
        panMaxY = .5f + getMaxPanDelta(zoomY);
    }

    // -------------------------- Observer methods ----------------------- //

    public void update(Observable observable, Object data) {
        limitZoom();
        updatePanLimits();
    }
    
	// ------------------------ Getters & Setters ------------------------ //
    
    public ZoomState getZoomState() {
        return mState;
    }
    
    public void setZoomRatio(ZoomRatio zoomRatio) {
		if (this.zoomRatio != null)
			this.zoomRatio.deleteObservers();
		
		this.zoomRatio = zoomRatio;
		this.zoomRatio.addObserver(this);
	}
}
