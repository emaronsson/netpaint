package com.kandidat.rityta.multi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import messagepacket.BrushMessage;
import messagepacket.DataMessage;
import messagepacket.IdCode;
import messagepacket.InfoMessage;
import messagepacket.MessagePacket;
import messagepacket.RedoMessage;
import messagepacket.UndoMessage;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.kandidat.R;
import com.kandidat.gui.Main;
import com.kandidat.network.BluetoothHandler;
import com.kandidat.network.PointContainerIn;
import com.kandidat.network.PointContainerOut;
import com.kandidat.network.TCPHandler;
import com.kandidat.network.client.BluetoothHandling;
import com.kandidat.network.client.NetworkHandling;
import com.kandidat.rityta.Brush;
import com.kandidat.rityta.DrawingActivity;
import com.kandidat.rityta.DrawingPath;

/**
 * Extends the DrawingActivity class for multiplayer mode.
 */
public class DrawingActivityMulti extends DrawingActivity {

	//Debugging
	private final boolean D  = false;
	private final String tag = "DrawingActivityMulti";
	
	/** Specifies the drawingsurface's height */
	public static final int SURFACE_HEIGHT = 800;
	
	/** Specifies the drawingsurface's width */
	public static final int SURFACE_WIDTH = 600;
	
	/** Identifier for fetching this activity's results */
	public static final int APPLICATION_ID = 1;
	
	/** Handler for incoming bluetooth messages */
	private static BluetoothHandling btHandler;
	
	/** Handler for incoming network messages */
	private static NetworkHandling networkHandler;
	
	/** Container for received points */
	private static PointContainerIn pContainerIn;
	
	/** Container for outgoing points */
	private static PointContainerOut pContainerOut;
	
	/** List of all current painters and their respective ID, except for me */
	public static List<Player> players;
	
	/** Unique identifier for ones own paths */
	private static short pID = 0;
	
	/** Identifier for ones own position in a online room */
	private static byte rID = 0;
	
	private Semaphore destroyed = new Semaphore(0);
	
	// -------------------------- View methods --------------------------- //
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.canvas_multi);
		setResult(Activity.RESULT_CANCELED);
		createButtons(this, this);
		
		if (D) Log.d(tag, "--- onCreate() ---");
		
        // Multiplayer
		players = Collections.synchronizedList(new ArrayList<Player>());
        
        // Drawing View
        view = (DrawingViewMulti) findViewById(R.id.drawingViewMulti);
        createZoomUtilities();
        createBackground();
        restoreState();
        
        // Point Containers
        pContainerOut = new PointContainerOut();
        pContainerIn  = new PointContainerIn(this);
        
        // Received message listener
        if (Main.controller == Main.MultiController.TCP) {
        	if (D) Log.d(tag, "-- TCP --");
        	
        	networkHandler = Main.getTCPHandler();
        	((TCPHandler) networkHandler.getHandler()).init(
        			this);
        }
        else if (Main.controller == Main.MultiController.BLUETOOTH) {
        	if (D) Log.d(tag, "-- BT --");
        	
        	btHandler = Main.getBTHandler();
        	((BluetoothHandler) btHandler.getHandler()).init(
        			this, btHandler.getId());
        }
	}
	
	/**
	 * When quiting, send quit message to server, reset threads
	 * and ID's.
	 */
	@Override
	protected void onDestroy() {
		if (D) Log.d(tag, "--- killing drawing activity multi...");
		
		//Send quit message
		MessagePacket msg = new MessagePacket();
		sendMessage(msg);
		
		killResources();
		
		super.onDestroy();
	}
	
	public void reload() {
		//TODO: everything!
//	    Intent intent = getIntent();
//	    overridePendingTransition(0, 0);
//	    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//		try {
//			killResources();
//			
//			if (D) Log.d(tag, "-- wainting for resources to be destoryed...");
//			
//			destroyed.acquire() ;
//		} catch (InterruptedException ignored) {
//		} finally {
//			if (D) Log.d(tag, "-- finishing activity...");
//			
			finish();
//		}
//	    
//	    overridePendingTransition(0, 0);
//	    startActivity(intent);
	}
	
	private void killResources() {
		if (D) Log.d(tag, "- killing resources...");
		
		// Reset Threads and ID's
		pID = 0;
		rID = 0;
		if (Main.controller == Main.MultiController.TCP) {
			if (networkHandler.isConnected())
				networkHandler.disconnect();
		} else if (Main.controller == Main.MultiController.BLUETOOTH)
			btHandler.stop();

		for (Player p : getPlayers())
			p.getPainter().kill();
		
		destroyed.release();
	}
	
	/**
	 * Set a white background.
	 */
	private void createBackground() {
		Bitmap tmp = Bitmap.createBitmap(SURFACE_WIDTH, SURFACE_HEIGHT, Bitmap.Config.ARGB_8888);
		tmp.eraseColor(Color.WHITE);
		view.setBitmap(tmp);
	}
	
	// ------------------------- Handler methods ------------------------- //
	
	/**
	 * Load a new room with other players.
	 */
	@Override
	protected void newGame() {
		super.newGame();
		
		//TODO: change room and start a new
		if (Main.controller == Main.MultiController.TCP) {
			if (D) Log.d(tag, "-- restarting game...");
//			setResult(Activity.RESULT_OK);
			reload();
		}
	}
	
	/**
	 * Creates an new unique pID according to:
	 *  0000 000000000000
	 * |----|------------|
	 *  rID     pathID
	 */
	public static short getNewPID() {
		return (short) ((rID << 12) | pID++);
	}

	/**
	 * Handle incoming MessagePacket.
	 */
	public void handleMessage(MessagePacket msg) {
		if (D) Log.d(tag+"-handler", "--- got a message!" +
				"\n- message typ:");
		
		switch(msg.getMsgId()) {
		
			//Coordinates to be drawn.
			case IdCode.DATA_MESSAGE:
				if (D) Log.d(tag, "- received a data message!");

				DataMessage data_msg = (DataMessage) msg.getMsg();
				dataMessage(data_msg);
				break;
			
			//Change the properties of a player's brush.
			case IdCode.BRUSH_MESSAGE:
				if (D) Log.d(tag, "- received a brush message!");

				BrushMessage brush_msg = (BrushMessage) msg.getMsg();
				changeBrush(brush_msg);
				break;	
			
			//Remove a certain path.
			case IdCode.UNDO_PATH:
				if (D) Log.d(tag, "- received a remove message!");

				UndoMessage remove_msg = (UndoMessage) msg.getMsg();
				//makeToast("A player deleted a path"
					//		+ "\nPath ID: " + remove_msg.getPathId());
				removePath(remove_msg);
				break;
				
			//Adds the path that another player called redo on to the list and draws it
			case IdCode.REDO_PATH:
				if (D) Log.d(tag, "- received a redo message!");
				
				RedoMessage redo_msg = (RedoMessage) msg.getMsg();
				redoPath(redo_msg);
				break;	
				
			//Inform the user that a player has joined the room.
			case IdCode.PLAYER_JOINED:
				if (D) Log.d(tag, "- received a player joined message!");

				InfoMessage player_joined_msg = (InfoMessage) msg.getMsg();
				makeToast(getString(R.string.new_player_added) +
						    "\n" + getString(R.string.numb_players) + ": " + player_joined_msg.getMembers());
				playerJoined(player_joined_msg);
				break;
				
			//Inform the user that a player has left the room.
			case IdCode.PLAYER_QUIT:
				if (D) Log.d(tag, "- received a player quit message!");

				InfoMessage player_quit_msg = (InfoMessage) msg.getMsg();
				makeToast(getString(R.string.player_left) + 
						  "\n" + getString(R.string.numb_players) + ": " + player_quit_msg.getMembers());
				playerQuit(player_quit_msg);
				break;
				
			//Inform the user that he/she has joined a room, what his/her
			//player id is, and how many members there are in the room.
			case IdCode.JOIN_SUCCESS:
				if (D) Log.d(tag, "- received a join success message!");

				InfoMessage join_success_msg = (InfoMessage) msg.getMsg();
				makeToast(getString(R.string.join_success) +
							//"\nYour ID: "+ join_success_msg.getPlayerId() +
							"\n" + getString(R.string.numb_players) + ": " + join_success_msg.getMembers());
				joinSuccess(join_success_msg);
				break;
				
			case IdCode.QUIT_MESSAGE:
				if (D) Log.d(tag, "- received a quit message!");

				//TODO: See if room is empty and prompt user if so
				break;	
				
			//Get the room history, and update the drawing surface.
			case IdCode.ROOM_HISTORY:
				// TODO: Get history of the room.
				break;
				
			//Something wrong with the message.
			default:
				//makeToast(this.getString(R.string.err_mess));
				break;
		}		
	}
	
	/**
	 * Send the incoming coordinates to the PointContainer.
	 */
	private void dataMessage(DataMessage msg) {
		if (D) Log.d(tag, "--- dataMessage" +
				"\n- pid: "+msg.getPathId());
		
		pContainerIn.processMessage(msg);
	}
	
	/**
	 * Add the new player that joined the room.
	 */
	public void playerJoined(InfoMessage msg) {
		if (D) Log.d(tag, "---playerJoined" +
				"\n- player id: "+ msg.getPlayerId() +
				"\n- members: "+msg.getMembers());
		
		players.add(new Player(
				(int) msg.getPlayerId(),
				new PointPainter((DrawingViewMulti) view),
				new DrawingPath()));
		
		updateBrush(view.currBrush);
		
		if (D) Log.d(tag, "- our player list size: "+players.size());
	}
	
	/**
	 * TODO:Remove player
	 */
	private void playerQuit(InfoMessage msg) {
//		final int player_id = msg.getPlayerId();
//		int i = 0;
//		for (;i < players.size() && player_id != players.get(i).getPlayerID(); i++) {}
//		players.remove(i);
	}
	
	/**
	 * Add all the other players to your list of players	
	 **/
	private void joinSuccess(InfoMessage msg) {
		if (D) Log.d(tag, "--- joinSuccess" +
				"\n- my ID: "+msg.getPlayerId() +
				"\n- room members: "+msg.getMembers());
		
		rID = msg.getPlayerId();
		for (int i = 0; i < msg.getMembers(); i++) {
			if (i == rID)
				continue;
			
			players.add(new Player(
					i,
					new PointPainter((DrawingViewMulti) view),
					new DrawingPath()));
		}
		
		if (D) Log.d(tag, "- our player list size: "+players.size());
	}
	
	/**
	 * Remove another player's path.
	 */
	private void removePath(UndoMessage msg) {
		if (D) {
			Log.d(tag, "--- removePath --------------------"
					+"\n- id: "+msg.getPathId()
					+"\n-- available paths:");
			
			synchronized (DrawingActivityMulti.players) {
				for (Player p : DrawingActivityMulti.players)
					if (!p.getPath().getPath().isEmpty() && D)
						Log.d(tag+"-loop:players", "- Path ID: "+p.getPath().getPID());
			}
			
			synchronized (DrawingActivity.path) {
					for (DrawingPath dp : DrawingActivity.path)
						if (!dp.getPath().isEmpty() && D)
							Log.d(tag+"-loop:paths", "- Path ID: "+dp.getPID());
			}
		}
				
		final short pID = msg.getPathId();
		boolean found = false;
		synchronized (DrawingActivityMulti.players) {
			for (Player p : getPlayers())
				if (pID == p.getPath().getPID()) {
					if (D) Log.d(tag, "-- reset a players' path!");
					
					p.getPath().setPID(Short.MIN_VALUE);
					p.getPath().setTimeStamp(0);
					p.getPath().getPath().reset();
					found = true;
		}}
		
		if (!found && !DrawingActivity.path.isEmpty()) {
			if (D) Log.d(tag+"-search", "--- Searching through all saved paths in reverse order");
		
			synchronized (DrawingActivity.path) {
				if (D) Log.d(tag+"-search", " -- paths to search: "+DrawingActivity.path.size());
				
				for (int i = DrawingActivity.path.size() - 1; i >= 0; i--) {
					if (D) Log.d(tag+"-search", 
							"-i="+i+", comparing path: "+DrawingActivity.path.get(i).getPID()
							+" to: "+pID);
						
					if (pID == DrawingActivity.path.get(i).getPID()) {
						if (D) Log.d(tag, "-- removed a path!");
						
						DrawingActivity.path.remove(i);
						break;
		}}}}
		
		view.redrawPaths();
	}
	
	/**
	 * Draws and adds the received drawing path to the list of paths. 
	 */
	private void redoPath(RedoMessage redo_msg) {
		if (D) Log.d(tag, "--- redoPath --------------------" +
				"\n- timeStamp: "+redo_msg.getTimeStamp() + 
				"\n- pID: "+redo_msg.getPath_id() + 
				"\n- coords: "+redo_msg.getCoordinates().length);
		
		DrawingPath path = new DrawingPath();
		
		short[] coords = redo_msg.getCoordinates();
		long timeStamp = redo_msg.getTimeStamp();
		short pID = redo_msg.getPath_id();
		
		if (D) Log.d(tag, "-- creating path...");
		
		path.createPath(coords, timeStamp, pID);
		
		Brush b = new Brush(redo_msg.getBrush_size(),
				Color.argb(redo_msg.getAlpha(),
					redo_msg.getR(), 
					redo_msg.getG(),
					redo_msg.getB()));
		b.setActiveEffects(redo_msg.getEffects());
		
		path.setBrush(b);

		if (D) Log.d(tag, "-- drawing path...");
		
		// Draw the finish path to the surface
		((DrawingViewMulti) view).drawPath(path, true);
	}
	
	/**
	 * Update your own brush, and send its argb values, brush size,
	 * and effects as a Message Packet to the other players- to inform
	 * them that your brush has been changed.
	 */
	@Override
	public void updateBrush(Brush brush) {
		super.updateBrush(brush);
	
		final int color = brush.getPaint().getColor();
		final short a = (short) ((color & 0xFF000000) >>> 24);
		final short r = (short) ((color & 0x00FF0000) >>> 16);
		final short g = (short) ((color & 0x0000FF00) >>> 8);
		final short b = (short)  (color & 0x000000FF);
		
		MessagePacket msg = new MessagePacket(
				rID,
				convertToByte(r), 
				convertToByte(g), 
				convertToByte(b), 
				convertToByte(a),
				(byte) brush.getPaint().getStrokeWidth(),
				brush.getActiveEffects());
		
		sendMessage(msg); 
	}
	
	/**
	 * If another player has changed brush, change it in the list
	 * of players.
	 */
	private void changeBrush(BrushMessage msg) {
		if (D) Log.d(tag, "--- changeBrush" +
				"\n- player_id: "+msg.getPlayerId());
		
		Brush b = new Brush(msg.getBrushSize(),
					Color.argb(msg.getAlphaValue(),
						msg.getColorR(), 
						msg.getColorG(),
						msg.getColorB()));
		b.setActiveEffects(msg.getEffects());
		
		final int player_id = msg.getPlayerId();
		byte i = 0;
		for (;(i < players.size()) && (player_id != players.get(i).getPlayerID()); i++) {}
//		players.get(i).getPath().setBrush(b.clone());
		getPlayerSafely(i).getPath().setBrush(b.clone());
    }
	
	public Player getPlayerSafely(byte ID) {
		if (D) Log.d(tag, "--- getPlayerSafely" +
				"\n- ID: "+ID);
		
		Player player = null;
		try {
			player = DrawingActivityMulti.getPlayer(ID);
		} catch (IndexOutOfBoundsException ignored) {
			if (D) Log.d(tag, "-- didn't find player... cheating");
			
			MessagePacket tmp_msg = new MessagePacket(
					IdCode.PLAYER_JOINED, ID, ID);
			
			updateBrush(view.currBrush);
			
			playerJoined((InfoMessage) tmp_msg.getMsg());
			player = DrawingActivityMulti.getPlayer(ID);
		}
		
		if (D) Log.d(tag, "-- player real now?"+(player!=null));
		
		return player;
	}
	
	// --------------------- Container out methods ----------------------- //

	public static void addPointsToContainer(Point[] points) {
    	pContainerOut.addPoints(points);
    }
    
    public static void addPointsToContainer(short[] array) {
    	pContainerOut.addPoints(array);
    }
    
    /**
     * Create Message Packet from your coordinates drawn, and 
     * send to the other players.
     */
    public static void sendPointContainer(short pID, long time) {
    	short[] coords = pContainerOut.getCoordsListInt();
    	pContainerOut.clearPoints();

    	MessagePacket msg = new MessagePacket(
    			rID, pID, coords, time);
    	
    	sendMessage(msg);
    }
    
    /**
     * Send a message to the server or to the other player - if
     * Bluetooth is used.
     */
    public static void sendMessage(MessagePacket msg) {
    	if (Main.controller == Main.MultiController.TCP)
    		networkHandler.writeToServer(msg);
    	else if (Main.controller == Main.MultiController.BLUETOOTH)
    		btHandler.write(msg);
    }
    
    /**
     * Show a toast with given string.
     */
    private void makeToast(String text) {
		Toast t = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
    	t.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
		t.show();
	}

    // ----------------------------- Menus  ---------------------------- //
    
    private static final int NEW_MENU_ID = Menu.FIRST;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(MENU_GROUP_ID, NEW_MENU_ID, 1, R.string.ico_new); 
		
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int text = 0;
	    switch (item.getItemId()) {
	        case NEW_MENU_ID:
	        	if (Main.controller == Main.MultiController.TCP)
	    			text = R.string.multiplayer_switch_friends;
	    		else if (Main.controller == Main.MultiController.BLUETOOTH)
	    			text = R.string.multiplayer_switch_blue;
	        	new AlertDialog.Builder(this)
	    		.setTitle(R.string.ico_new)
	            .setIcon(android.R.drawable.ic_dialog_alert)
	            .setMessage(text)
	            .setPositiveButton(R.string.quit_yes, new DialogInterface.OnClickListener() {
	            	
	            	@Override
	                public void onClick(DialogInterface dialog, int which) {
	            		newGame();
	                      
	            }})
	            .setNegativeButton(R.string.quit_no, null)
	            .show();
				return true;
	            
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	// ----------------------- Getters & Setters ------------------------- // 
	
	public static byte getMyRID() {
		return rID;
	}
	
	public static List<Player> getPlayers() {
		return players;
	}
	
	public static Player getPlayer(int ID) throws IndexOutOfBoundsException {
		return players.get(findPlayer(ID));
	}
	
	public static Player removePlayer(int ID) {
		return players.remove(findPlayer(ID));
	}
	
	private static int findPlayer(int ID) {
		int i = 0;
		for (;i < players.size() - 1 && ID != players.get(i).getPlayerID(); i++);
		return i;
	}
	
    //Convert rgba values from short to byte to save space
    public static byte convertToByte( short x ) {
        return (byte)(x-128);
    }
	
    public static PointContainerOut getContainerOut() {
    	return pContainerOut;
    }
    
    public static Handler getActiveHandler() {
    	if (Main.controller == Main.MultiController.TCP)
    		return networkHandler.getHandler();
    	return btHandler.getHandler();
    }
}
