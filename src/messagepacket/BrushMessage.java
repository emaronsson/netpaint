package messagepacket;

import java.io.Serializable;

/**
 * A message that is sent if brush properties changes.
 */
public class BrushMessage implements Serializable {

	private static final long serialVersionUID = -5673805286633443205L;
    private byte player_id;
    private byte r;
    private byte g;
    private byte b;
    private byte alpha; 
    private byte brush_size;
    private boolean[] effects;     

    public BrushMessage(byte player_id, byte r, byte g, 
    					byte b, byte alpha, byte brush_size, 
    					boolean[] effects ){
        this.player_id = player_id;          
        this.r = r;
        this.g = g;
        this.b = b;
        this.alpha = alpha;
        this.brush_size = brush_size;
        this.effects = effects;
    }
    
    /**
     *  Get the player id
     */
    public byte getPlayerId(){
        return player_id;     
    }
    
    /**
     * Get the correct r value
     */
    public short getColorR() {
        return (short)(r+128);
    }

    /**
     * Get the correct g value
     */
    public short getColorG() {
        return (short)(g+128);
    }

    /**
     * Get the correct b value
     */
    public short getColorB() {
        return (short)(b+128);
    }

    /**
     * Get the correct alpha value
     */
    public short getAlphaValue(){
        return(short)(alpha+128);
    }
    
    /**
     * Get the brush size
     */
    public byte getBrushSize(){
        return brush_size;
    }
    
    /**
     * List of boolean values of which effect is used
     */
    public boolean[] getEffects() {
        return effects;
    }
    
}
