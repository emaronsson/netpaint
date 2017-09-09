package messagepacket;

import java.io.Serializable;

/**
 * Message sent to get playerID, and to get the 
 * number of players in the room.
 */
public class InfoMessage implements Serializable {

  private static final long serialVersionUID = -6208374288152220989L;
  private byte player_id; 
  private byte members;

   /**
    * Message about room info
    */
    public InfoMessage( byte player_id, byte members ) {
        this.player_id = player_id;
        this.members = members;
    }
 
    /**
     * Get the player id.
     */
    public byte getPlayerId(){
        return player_id;     
    }
    
    /**
     * Members in a room.
     */
    public byte getMembers() {
        return members;
    }
    
}
