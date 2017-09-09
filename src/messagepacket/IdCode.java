package messagepacket;

/**
 * Codes for the PacketMessages.
 */
public class IdCode {
  public static final byte DATA_MESSAGE    = 0;
  public static final byte BRUSH_MESSAGE   = 1;
  public static final byte UNDO_PATH       = 2;
  public static final byte REDO_PATH       = 3;
  public static final byte PLAYER_JOINED   = 4;
  public static final byte PLAYER_QUIT     = 5;
  public static final byte JOIN_SUCCESS    = 6;
  public static final byte QUIT_MESSAGE    = 7;
  public static final byte ROOM_HISTORY    = 8;
}
