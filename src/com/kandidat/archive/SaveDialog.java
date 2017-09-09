package com.kandidat.archive;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.kandidat.rityta.DrawingView;
import com.kandidat.R;

/**
 * Dialog for saving images.
 */
public class SaveDialog extends Dialog {

	private Context context;
	private DrawingView view;
	private ArchiveManager fm;
	private EditText edit;
	
	public SaveDialog(Context c, DrawingView v) {
		super(c);
		
		context = c;
		view    = v;
		fm      = new ArchiveManager(context);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filehandler_save);
		setTitle(context.getResources().getText(R.string.save_dialog_title));
		
		edit = (EditText) findViewById(R.id.filename_editbox);
		
		Button saveFile = (Button) findViewById(R.id.file_save_button);
		saveFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	String s = edit.getText().toString();
            	hideKeyboard();
            	dismiss();
            	
            	// if file exists we display a replacement dialog,
            	// if not we write it to disk
            	if (fm.checkFile(s)) 
            		showReplaceFileDialog(s);
            	else
            		fm.saveImage(view.getBitmap(), s);
            }
        });
	}	
	
	/**
	 * Warning dialog - if one tries to save an image with
	 * file name that already exists.
	 **/
	private void showReplaceFileDialog(String s) {
		final String str = s;
		
		new AlertDialog.Builder(context)
		.setTitle(R.string.replace_dialog_title)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setMessage(R.string.replace_dialog_text)
		.setPositiveButton(R.string.replace_dialog_yes,
		new DialogInterface.OnClickListener() {
        	
        	@Override
            public void onClick(DialogInterface dialog, int which) {
        		fm.saveImage(view.getBitmap(), str);
            }
        })
        .setNegativeButton(R.string.replace_dialog_no, null)
        .show();
	}
	
	/**
	 * Hides the keyboard on the screen.
	 */
	private void hideKeyboard() {
		InputMethodManager inputManager = 
				(InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE); 
		
    	inputManager.hideSoftInputFromWindow(
    			getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
	}
}
