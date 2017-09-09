package messagepacket;

import java.io.Serializable;

/**
 * Message sent with new coordinates to be drawn.
 */
public class DataMessage implements Serializable {
    
	private static final long serialVersionUID = 6997430608261170111L;
    private byte player_id;
    private short path_id;
    private short coordinates[];		
    private long timeStamp;
    

    public DataMessage(byte player_id, short path_id, 
    				   short[] coordinates, long timeStamp ) {
        this.player_id = player_id;
        this.path_id = path_id;
        this.coordinates = coordinates;	
        this.timeStamp = timeStamp;
	}
        
    /**
     * Player id
     */
    public byte getPlayerId(){
       return player_id;     
    }
    
    /**
     * Path ID
     */
    public short getPathId() {
       return path_id;
    }
        
    /**
     * Coordinates to draw
     */
    public short[] getCoordinates() {
       return coordinates;
    }
        
    /**
     * Timestamp	
     */
    public long getTimeStamp() {
       return timeStamp;
    }
}