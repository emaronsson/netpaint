package messagepacket;

import java.io.Serializable;

/**
 * Message sent when a player perfoms
 * redo for a path.
 */
public class RedoMessage implements Serializable {
	
	private static final long serialVersionUID = -6645397537955699136L;
	private byte r;
	private byte g;
	private byte b;
	private byte alpha; 
	private byte brush_size;
	private boolean[] effects; 
	private short path_id;
	private short coordinates[];		
	private long timeStamp;
	
	public RedoMessage(short[] coords, short pID, long timeStamp,
					   byte alpha, byte r, byte g, byte b,
					   byte brush_size, boolean[] effects)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.alpha = alpha;
		this.brush_size = brush_size;
		this.effects = effects;
		this.path_id = pID;
		this.coordinates = coords;	
		this.timeStamp = timeStamp;
	}
	
	// -------------------- Getters & Setters --------------------- //
	
	public short getR() {
		return (short)(r+128);
	}

	public short getG() {
		return (short)(g+128);
	}

	public short getB() {
		return (short)(b+128);
	}

	public short getAlpha() {
		return (short)(alpha+128);
	}

	public byte getBrush_size() {
		return brush_size;
	}

	public boolean[] getEffects() {
		return effects;
	}

	public short getPath_id() {
		return path_id;
	}

	public short[] getCoordinates() {
		return coordinates;
	}

	public long getTimeStamp() {
		return timeStamp;
	}
}
