package messagepacket;

import java.io.Serializable;

/**
 * Class representing a message.
 * Can be of different types - depends on
 * the purpose.
 */
public class MessagePacket implements Serializable {

  private static final long serialVersionUID = 6774447527662045877L;

  private byte msg_id;
  private Object message;
	
  /**
   * To send data package with coordinates
   */
	public MessagePacket(byte player_id, short path_id, 
						 short[] coordinates, long timeStamp ) {
		this.msg_id = IdCode.DATA_MESSAGE;
        this.message = new DataMessage( player_id, path_id, coordinates, timeStamp );
	}

	/**
	 *  To send if brush properties changes
	 */
	public MessagePacket(byte player_id, byte r, byte g, 
						 byte b, byte alpha, byte brush_size, 
						 boolean[] effects ) {
		this.msg_id = IdCode.BRUSH_MESSAGE;
		this.message = new BrushMessage( player_id, r, g, b, alpha, brush_size, effects );
	}

	/**
	 * Info message from client or server
	 */
	public MessagePacket( byte msg_id, byte player_id, byte members ) {
		this.msg_id = msg_id;
		this.message = new InfoMessage( player_id, members );
	}	

	
	/**
	 * Message to send if removal of specific path is required
	 */
	public MessagePacket( short path_id ) {
		this.msg_id = IdCode.UNDO_PATH;
		this.message = new UndoMessage( path_id );
	}
	
	
  	/**
	 * Message to send if close down connection is required
	 */
	public MessagePacket() {
		this.msg_id = IdCode.QUIT_MESSAGE;
	}
	
  /**
	 * Message to send if Redo of drawingpath is required
	 */
	public MessagePacket(short[] coords, short pID, long timeStamp,
			   byte alpha, byte r, byte g, byte b, byte brush_size, 
			   boolean[] effects )
	{
		this.msg_id = IdCode.REDO_PATH;
		this.message = new RedoMessage(coords, pID, timeStamp,
				   alpha, r, g, b, brush_size, 
				   effects );
	}
    
	
	/**
	 * Get ID code
	 */
	public byte getMsgId() {
		return msg_id;
	}
	
	/**
	 * Return message object
	 */
	public Object getMsg() {
		return message;
	}
}