package messagepacket;

import java.io.Serializable;

/**
 * Message sent when a player removes a path with the
 * undo function.
 */
public class UndoMessage implements Serializable {

	private static final long serialVersionUID = -3952502257294185318L;
    private short path_id;
    
    /**
    * Message to send if removal of specific path is required
    */
    public UndoMessage( short path_id ) {
        this.path_id = path_id;
    }
    
    /**
     * Return the path ID.
     */
    public short getPathId() {
        return path_id;
    }
    
}
