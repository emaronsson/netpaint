package com.kandidat.rityta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.kandidat.R;
import com.kandidat.archive.LoadDialog;
import com.kandidat.archive.SaveDialog;
import com.kandidat.rityta.BrushDialog.BrushListener;
import com.kandidat.rityta.zoom.DynamicZoomControl;
import com.kandidat.rityta.zoom.ZoomListener;
import com.kandidat.rityta.zoom.ZoomListener.Mode;

/**
 * Class for taking care of the game. Contains a drawing surface,
 * the buttons and menues, and keeps a list of all drawn paths.
 */
public class DrawingActivity extends Activity implements BrushListener {
	
	/** List containing all drawn paths */
	public static List<DrawingPath> path;
	
	/** List for user own recently deleted paths */
	public static List<DrawingPath> redo;
	
	/** Custom view with canvas to draw on */
	public static DrawingView view;
	
	/** Listener for zoom/pan/draw actions on a DrawingView */
	private ZoomListener zoomListener;
	
	/** Handler for zoom type and limiting pan and zoom actions */
	private DynamicZoomControl zoomControl;
	
	/** Holds the currentBrush while erasing */
	private Brush tempBrush;
	
	/** The size of the eraser*/
	public int eraserSize;
	
	/** States if the eraser has been used */
	private boolean hasErased;
		
	
	// ----------------------- Activity methods -------------------------- //
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		path = Collections.synchronizedList(new ArrayList<DrawingPath>());
        redo = Collections.synchronizedList(new ArrayList<DrawingPath>());
        
		eraserSize = 20;
		hasErased  = false;
    }
    
    /**
     * When orientation changes, we call state-class to
     * collect objects to retain.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        return (new DrawingState(view));
    }
  
    /**
     * When drawingActivity closed set the bitmap to null, clear listeners
     * and delete all observers.
     */
    @Override
	protected void onDestroy() {
    	super.onDestroy();
    	
    	view.bitmap.recycle();
    	view.setOnTouchListener(null);
    	if (zoomControl != null)
    		zoomControl.getZoomState().deleteObservers();
    }
    
    // -------------------- View modification methods -------------------- //
    
    /**
     * Restore data structures lost due to a screen flip
     */
    protected void restoreState() {
        final DrawingState st = (DrawingState) getLastNonConfigurationInstance();
        if (st != null) {
        	path = st.getPathListFromSavedState();
    		redo = st.getRedoListFromSavedState();
    		view.setCurrentBrush(st.getBrushFromSavedState());
        } 
    }
    
    /**
     * Restores the zoom state to its original settings
     */
    protected void restoreZoomState() {
    	zoomControl.getZoomState().setPanX(0.5f);
    	zoomControl.getZoomState().setPanY(0.5f);
    	zoomControl.getZoomState().setZoom(1f);
    	zoomControl.getZoomState().notifyObservers();
    }
    
    /**
     * Creates a new game by clearing the image, in multiplayer this
     * should be overwritten to connect to a new session instead.
     */
    protected void newGame() {
    	path.clear();
    	redo.clear();
    	view.background = null;
    	view.bitmap.eraseColor(Color.WHITE);
    	view.invalidate();
    }

    /**
   	 * Update the settings of the brush.
   	 */
    public void updateBrush(Brush brush) {
       	view.setCurrentBrush(brush);
    }

    //--------------------- View creation methods ----------------------- //

    /**
     * Initialized the zoom utilities needed to perform zooming and
     * panning actions, as well as the touch listener used.
     */
    protected void createZoomUtilities() {
    	zoomControl  = new DynamicZoomControl();
    	zoomListener = new ZoomListener(this);
    	zoomListener.setZoomControl(zoomControl);

    	view.setZoomState(zoomControl.getZoomState());
    	view.setOnTouchListener(zoomListener);

    	zoomControl.setZoomRatio(view.getZoomRatio());

    	restoreZoomState();
    }
    
    /**
     * Create the buttons for the lower menu, and their listeners.
     */
    protected void createButtons(final Context c, final BrushListener bl) {

    	// Initialize buttons here so we can use them in all listeners
    	final ToggleButton brushButton  = (ToggleButton) findViewById(R.id.brushButton);
    	final ToggleButton eraserButton = (ToggleButton) findViewById(R.id.eraserButton);
    	final ToggleButton zoomButton   = (ToggleButton) findViewById(R.id.zoomButton);

    	//-------------------------Brush button----------------------------//

    	brushButton.setChecked(true);
    	brushButton.setOnClickListener(new OnClickListener() {
    		private boolean firstClick = true;

    		@Override
    		public void onClick(View v) {
    			if (firstClick) {
    				firstClick = false;
    				makeCustomBrushToast();
    			}

    			if (hasErased){
    				eraserSize = (int) view.currBrush.getSize();
    				view.currBrush = tempBrush.clone();
    				hasErased = false;
    				updateBrush(view.currBrush);
    			}

    			if (!brushButton.isChecked())
    				brushButton.setChecked(true);

    			zoomListener.setControlType(Mode.DRAWING);
    			eraserButton.setChecked(false);
    			zoomButton.setChecked(false);
    		}});

    	brushButton.setOnLongClickListener(new OnLongClickListener() {

    		@Override
    		public boolean onLongClick(View v) {
    			zoomListener.setControlType(Mode.DRAWING);

    			if (hasErased){
    				eraserSize = (int) view.currBrush.getSize();
    				view.currBrush = tempBrush.clone();
    				hasErased = false;
    				updateBrush(view.currBrush);
    			}

    			brushButton.setChecked(true);
    			eraserButton.setChecked(false);
    			zoomButton.setChecked(false);

    			new BrushDialog(c, bl, view.getCurrentBrush()).show();
    			return true;
    		}
    	});

    	//-------------------------Eraser button----------------------------//

    	eraserButton.setOnClickListener(new OnClickListener() {
    		private boolean firstClick = true;

    		@Override
    		public void onClick(View v) {
    			if (firstClick){
    				firstClick = false;
    				makeCustomEraserToast();
    			}

    			brushButton.setChecked(false);
    			zoomButton.setChecked(false);
    			eraserButton.setChecked(true);
    			zoomListener.setControlType(Mode.DRAWING);

    			if (!hasErased)
    			{
    				tempBrush = view.currBrush.clone();
    				view.currBrush.setEraser();
    				view.currBrush.setSize(eraserSize);
    				hasErased = true;
    				updateBrush(view.currBrush);
    			}
    		}});

    	eraserButton.setOnLongClickListener(new OnLongClickListener() {

    		@Override
    		public boolean onLongClick(View v) {

    			brushButton.setChecked(false);
    			eraserButton.setChecked(true);
    			zoomButton.setChecked(false);

    			zoomListener.setControlType(Mode.DRAWING);

    			if(!hasErased){
    				tempBrush = view.currBrush.clone();
    				view.currBrush.setEraser();
    				view.currBrush.setSize(eraserSize);
    				hasErased = true;
    			}

    			EraserDialog ed = new EraserDialog(c,view.currBrush);
    			ed.show();
    			
    			return true;
    		}
    	});

    	//-------------------------Zoom button----------------------------//

    	zoomButton.setOnClickListener(new OnClickListener() {
    		private boolean firstClick = true;

    		@Override
    		public void onClick(View v) {
    			if (firstClick) {
    				firstClick = false;
    				makeCustomZoomToast();
    			}

    			if (!zoomButton.isChecked())
    				zoomButton.setChecked(true);

    			zoomListener.setControlType(Mode.UNDEFINED);
    			brushButton.setChecked(false);
    			eraserButton.setChecked(false);
    		}});

    	zoomButton.setOnLongClickListener(new OnLongClickListener() {

    		@Override
    		public boolean onLongClick(View v) {
    			zoomListener.setControlType(Mode.UNDEFINED);
    			brushButton.setChecked(false);
    			eraserButton.setChecked(false);
    			zoomButton.setChecked(true);

    			restoreZoomState();
    			return false;
    		}
    	});

    	//------------------------Undo and Redo button----------------------------//

    	final Button redoButton = (Button) findViewById(R.id.redoButton);
    	redoButton.setOnClickListener(new OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			view.redo();
    		}});

    	final Button undoButton = (Button) findViewById(R.id.undoButton);
    	undoButton.setOnClickListener(new OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			view.undo();
    		}});
    }
    
      //----------------------------------------------------------------------------//
    
    /**
     * Display a help toast for the brush.
     */
    protected void makeCustomBrushToast() {
    	
    	LayoutInflater inflater = getLayoutInflater();
    	View layout = inflater.inflate(R.layout.toast_brush,
    			(ViewGroup) findViewById(R.id.toast_brush_root));

    	ImageView image = (ImageView) layout.findViewById(R.id.toast_brush_image);
    	image.setImageResource(R.drawable.pen_selected);

    	TextView text = (TextView) layout.findViewById(R.id.toast_brush_text);
    	text.setText(R.string.brush_help);

    	showToast(layout);
    }

    /**
     * Display a help toast for the eraser.
     */
    protected void makeCustomEraserToast() {
    	LayoutInflater inflater = getLayoutInflater();
    	View layout = inflater.inflate(R.layout.toast_erase,
    			(ViewGroup) findViewById(R.id.toast_erase_root));

    	ImageView image = (ImageView) layout.findViewById(R.id.toast_erase_image);
    	image.setImageResource(R.drawable.eraser_selected);

    	TextView text = (TextView) layout.findViewById(R.id.toast_erase_text);
    	text.setText(R.string.erase_help);

    	showToast(layout);
    }
    
    /**
     * Display a help toast for the zoom.
     */
    protected void makeCustomZoomToast() {
    	LayoutInflater inflater = getLayoutInflater();
    	View layout = inflater.inflate(R.layout.toast_zoom,
    			(ViewGroup) findViewById(R.id.toast_zoom_root));

    	ImageView image = (ImageView) layout.findViewById(R.id.toast_zoom_image);
    	image.setImageResource(R.drawable.zoom_selected);

    	TextView text = (TextView) layout.findViewById(R.id.toast_zoom_text);
    	text.setText(R.string.zoom_help);

    	showToast(layout);
    	showToast(layout);
    }
    
    /**
     * Display another help toast for the zoom.
     */
    public void makeCustomZoomToast2() {
    	LayoutInflater inflater = getLayoutInflater();
    	View layout = inflater.inflate(R.layout.toast_zoom,
    			(ViewGroup) findViewById(R.id.toast_zoom_root));

    	ImageView image = (ImageView) layout.findViewById(R.id.toast_zoom_image);
    	image.setImageResource(R.drawable.zoom_selected);

    	TextView text = (TextView) layout.findViewById(R.id.toast_zoom_text);
    	text.setText(R.string.zoom_help2);

    	showToast(layout);
    }
    
    /**
     * Show a toast for the given view.
     */
    private void showToast(View view) {
    	Toast toast = new Toast(getApplicationContext());
    	toast.setGravity(Gravity.TOP, 0, 0);
    	toast.setDuration(Toast.LENGTH_LONG);
    	toast.setView(view);
    	toast.show();
    }
   
    // ------------------------------ Menus ------------------------------ //

    public static final int MENU_GROUP_ID = 1;
    
    public static final int LOAD_MENU_ID   = Menu.FIRST + 1;
	public static final int SAVE_MENU_ID   = Menu.FIRST + 2;
    
	/**
	 * The load and save items in the options menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 menu.add(MENU_GROUP_ID, LOAD_MENU_ID, 2, R.string.ico_load); 
		 menu.add(MENU_GROUP_ID, SAVE_MENU_ID, 3, R.string.ico_save);  
		
	    return super.onCreateOptionsMenu(menu);
	}
		
	/**
	 * Determines what will happen when selecting an item
	 * in the options menu.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
	    switch (item.getItemId()) {
	        case LOAD_MENU_ID:
	            new LoadDialog(this, view).show();
	            return true;
	            
	        case SAVE_MENU_ID:
	            new SaveDialog(this, view).show();
	            return true;
	            
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	// ----------------------- Key press handler ------------------------- //
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	
    	// Handle back button
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		new AlertDialog.Builder(this)
    		.setTitle(R.string.quitDraw_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(R.string.quitDraw_text)
            .setPositiveButton(R.string.quit_yes, new DialogInterface.OnClickListener() {
            	
            	@Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();    
            }})
            .setNegativeButton(R.string.quit_no, null)
            .show();
    		
    		return true;
        }
        else
            return super.onKeyDown(keyCode, event);
	}
}