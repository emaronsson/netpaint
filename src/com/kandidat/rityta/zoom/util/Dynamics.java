package com.kandidat.rityta.zoom.util;


public abstract class Dynamics {
	
    /** The maximum delta time, in milliseconds, between two updates */
    private static final int MAX_TIMESTEP = 50;
    /** The current position */
    protected float position;
    /** The current velocity */
    protected float velocity;
    /** The time of the last update */
    protected long lastTime = 0;
    /** The current maximum position */
    protected float maxPosition = Float.MAX_VALUE;
    /** The current minimum position */
    protected float minPosition = -Float.MAX_VALUE;

    /**
     * Sets the state of the dynamics object. Should be called before starting
     * to call update.
     */
    public void setState(final float position, final float velocity, final long now) {
        this.velocity = velocity;
        this.position = position;
        this.lastTime = now;
    }

    /**
     * Used to find out if the list is at rest, that is, has no velocity and is
     * inside the the limits. Normally used to know if more calls to update are
     * needed.
     */
    public boolean isAtRest(final float velocityTolerance, final float positionTolerance) {
        final boolean standingStill = Math.abs(velocity) < velocityTolerance;
        final boolean withinLimits  = position - positionTolerance < maxPosition
                				   && position + positionTolerance > minPosition;
                				   
        return standingStill && withinLimits;
    }

    /**
     * Updates the position and velocity.
     */
    public void update(final long now) {
        int dt = (int)(now - lastTime);
        if (dt > MAX_TIMESTEP) {
            dt = MAX_TIMESTEP;
        }

        onUpdate(dt);

        lastTime = now;
    }

    /**
     * Gets the distance to the closest limit (max and min position).
     */
    protected float getDistanceToLimit() {
        float distanceToLimit = 0;

        if (position > maxPosition) {
            distanceToLimit = maxPosition - position;
        } else if (position < minPosition) {
            distanceToLimit = minPosition - position;
        }

        return distanceToLimit;
    }

    /**
     * Updates the position and velocity.
     */
    abstract protected void onUpdate(int dt);
    
    // ------------------------ Getters & Setters ------------------------ //
    
    public float getPosition() {
        return position;
    }

    public float getVelocity() {
        return velocity;
    }
    
    public void setMaxPosition(final float maxPosition) {
        this.maxPosition = maxPosition;
    }

    public void setMinPosition(final float minPosition) {
        this.minPosition = minPosition;
    }
}
