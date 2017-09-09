package com.kandidat.rityta.multi;

import com.kandidat.rityta.DrawingPath;

/**
 * Class for representing a player.
 */
public class Player {

	/** This players ID */
	private final int playerID;
	
	/** Painter used to draw players paths */
	private final PointPainter painter;
	
	/** DrawingPath connected to this player, used when drawing */
	private final DrawingPath path;
	
	
	public Player(int ID, PointPainter pp, DrawingPath dp) {
		this.playerID = ID;
		this.painter = pp;
		this.path = dp;
	}
	
	// ----------------------- Getters & Setters ------------------------- //

	public int getPlayerID() {
		return playerID;
	}

	public PointPainter getPainter() {
		return painter;
	}

	public DrawingPath getPath() {
		return path;
	}
}
