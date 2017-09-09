package messagepacket;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Message sent when history of a room needs 
 * to be received by a new player.
 * TODO: Class not finished or used.
 */
public class HistoryMessage implements Serializable {
    
	private static final long serialVersionUID = -4540562496687599068L;
    private ArrayList<Object> history;
    
    public HistoryMessage( ArrayList<Object> history ) {
        this.history = history;
    }
    
    /**
     * Return history of the room.
     */
    public ArrayList<Object> getRoomHistory() {
        return history;
    }
}
