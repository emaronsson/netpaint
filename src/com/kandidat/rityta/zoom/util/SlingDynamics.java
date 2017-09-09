package com.kandidat.rityta.zoom.util;

public class SlingDynamics extends Dynamics {

    /** Friction factor */
    private float friction;

    /** Spring stiffness factor */
    private float stiffness;

    /** Spring damping */
    private float damping;

    /**
     * Calculate acceleration at the current state
     */
    private float calculateAcceleration() {
        float acceleration;

        final float distanceFromLimit = getDistanceToLimit();
        if (distanceFromLimit != 0)
            acceleration = distanceFromLimit * stiffness - damping * velocity;
        else
            acceleration = -friction * velocity;

        return acceleration;
    }

    @Override
    protected void onUpdate(int dt) {
    	
        // Calculate dt in seconds as float
        final float fdt = dt / 1000f;

        // Calculate current acceleration
        final float a = calculateAcceleration();

        // Calculate next position based on current velocity and acceleration
        position += velocity * fdt + .5f * a * fdt * fdt;

        // Update velocity
        velocity += a * fdt;
    }
    
    // ------------------------ Getters & Setters ------------------------ //
    
    public void setFriction(float friction) {
        this.friction = friction;
    }

    public void setSpring(float stiffness, float dampingRatio) {
        this.stiffness = stiffness;
        this.damping = dampingRatio * 2 * (float)Math.sqrt(stiffness);
    }
}
