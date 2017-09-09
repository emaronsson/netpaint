package com.kandidat.rityta;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.kandidat.R;

/**
 * Class for dialog to set the size of the eraser.
 */
public class EraserDialog extends Dialog{

	private TextView tv;
	private SeekBar erasorSeek;
	private DrawingActivity da;
	
	public EraserDialog(Context context, final Brush eraser) {
		super(context);
		
		this.da = (DrawingActivity) context;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.eraser);
		
		TextView sizeText = (TextView) super.findViewById(R.id.size_text);
		sizeText.setText(R.string.erasorSize);
		
		tv = (TextView) findViewById(R.id.erasor_text);
		tv.setText(""+(int) eraser.getSize());
		
		//Seekbar for setting the eraser size.
		erasorSeek = (SeekBar) findViewById(R.id.seekBar);
		erasorSeek.setProgress((int) eraser.getSize());
		erasorSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			/**
			 * Change text as long as we keep holding handle 
			 **/
			public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
				tv.setText("" + progress);
			}

			public void onStartTrackingTouch(SeekBar seekbar) {	}

			/** 
			 * When we let go of handle, set eraser size 
			 **/
			public void onStopTrackingTouch(SeekBar seekbar) {
				eraser.setSize(erasorSeek.getProgress());
			}
		});
		
		/**
		 * When pressing ok, close the dialog.
		 */
		Button okButton = (Button) super.findViewById(R.id.ok);
		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				da.updateBrush(DrawingActivity.view.currBrush);
				dismiss();
			}
		});
	}
}