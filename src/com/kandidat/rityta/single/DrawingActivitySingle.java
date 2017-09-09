package com.kandidat.rityta.single;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;

import com.kandidat.R;
import com.kandidat.rityta.DrawingActivity;
import com.kandidat.rityta.DrawingView;

/**
 * Extends DrawingActivity for singleplayer mode.
 */
public class DrawingActivitySingle extends DrawingActivity {
	
	// ----------------------- Activity methods -------------------------- //
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.canvas_single);
		createButtons(this, this);
		
		// Drawing View
        view = (DrawingView) findViewById(R.id.drawingViewSingle);
        createZoomUtilities();
        createBackground();
        restoreState();
	}
	
	private void createBackground() {
		Display display = getWindowManager().getDefaultDisplay();
		Bitmap tmp = Bitmap.createBitmap(display.getWidth(), (int) (display.getHeight() * 0.85), Bitmap.Config.ARGB_8888);
		tmp.eraseColor(Color.WHITE);
		view.setBitmap(tmp);
	}
	
	// ----------------------- Singleplayer methods ---------------------- //

	private static final int NEW_MENU_ID = Menu.FIRST;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(MENU_GROUP_ID, NEW_MENU_ID, 1, "Nytt spel"); 
		
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case NEW_MENU_ID:
	        	new AlertDialog.Builder(this)
	    		.setTitle(R.string.ico_new)
	            .setIcon(android.R.drawable.ic_dialog_alert)
	            .setMessage(R.string.singleplayer_new)
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
}
