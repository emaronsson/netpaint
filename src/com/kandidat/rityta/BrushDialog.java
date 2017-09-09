package com.kandidat.rityta;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.kandidat.R;

/**
 * Dialog for selecting options for a brush.
 */
public class BrushDialog extends Dialog {
	
	private Context context;
	private BrushListener sListener;
	private TextView tv;
	private ColorPickerView cpv;

	private static Brush brush;
	private Brush        oldBrush;
	private SeekBar 	 brushSeek;
	
	public interface BrushListener{
		void updateBrush(Brush b);
	}

	/**
	 * Sets the brush and listener to those sent in to the constructor.
	 */
	public BrushDialog(Context context, BrushListener s, Brush mBrush) {
		super(context);
		this.context = context;
		sListener    = s;
		brush        = mBrush.clone();
		oldBrush     = mBrush.clone();
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	/**
	 * Initialize and set layout.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.brush2);
		
		getWindow().setBackgroundDrawableResource(R.color.dark_grey);
		
		// Effects title
		TextView fxTitle = (TextView) super.findViewById(R.id.brush_fx_title);
		fxTitle.setText(R.string.brush_fx_title);

		RadioGroup radioGroup        = (RadioGroup)  super.findViewById(R.id.rGroup);
		
		final RadioButton resetBrush = (RadioButton) super.findViewById(R.id.btn_fx0);
		final RadioButton effect1    = (RadioButton) super.findViewById(R.id.btn_fx1);
		final RadioButton effect2    = (RadioButton) super.findViewById(R.id.btn_fx2);
		final RadioButton effect3    = (RadioButton) super.findViewById(R.id.btn_fx3);
		
		//When starting - check which RadioButton was chosen last time, and mark it.
		switch (brush.getEffect()) {
			case 0: resetBrush.setChecked(true); 	break;
			case 1: effect1.setChecked(true); 		break;
			case 2: effect2.setChecked(true); 		break;
			case 3: effect3.setChecked(true); 		break;
		
		}
		
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			/**
			 * When a user presses a new RadioButton, the effect
			 * connected to that one will be applied to the brush.
			 */
			@Override
			public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
				
				brush.resetBrush();
				
				if (resetBrush.isChecked()) {
					brush.setActiveEffect(0);
				}
				else if(effect1.isChecked()) {
					brush.enableEmboss();
					brush.setActiveEffect(1);
				}
				else if(effect2.isChecked()) {
					brush.enableBlur();
					brush.setActiveEffect(2);
				}
				else if(effect3.isChecked()) {
					brush.enableShadow();
					brush.setActiveEffect(3);
				}
			}
		});

		int size = (int) brush.getSize();
		tv = (TextView) super.findViewById(R.id.sizetext);
		tv.setText(""+(size-1));
		
		brushSeek = (SeekBar) findViewById(R.id.brush_seekbar);

		//Adjust seek bar to show current brush size
		brushSeek.setProgress(size);
		//Add listener to seek bar
		brushSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			/** 
			 * Change text as long as we keep holding handle 
			 **/
			public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
				tv.setText("" + (progress+1));
			}

			public void onStartTrackingTouch(SeekBar seekbar) {	}

			/**  
			 * when we let go of handle, set brush size 
			 **/
			public void onStopTrackingTouch(SeekBar seekbar) {
				brush.setSize(brushSeek.getProgress() + 2);
			}
		});

		TextView sizeTitle = (TextView) super.findViewById(R.id.brush_size_title);
		sizeTitle.setText(R.string.brush_size_title);

		//Add a color picker.
		final LinearLayout colPick = (LinearLayout) super.findViewById(R.id.colorView);
		cpv = new ColorPickerView(context, oldBrush.getPaint().getColor());
		colPick.addView(cpv);

		//The white color.
		LinearLayout whitebox = (LinearLayout) super.findViewById(R.id.whiteBox);
		whitebox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				brush.setPaint(Color.WHITE);
				cpv.changeCenterColor(Color.WHITE);
			}
		});
		
		//The black color.
		LinearLayout blackbox = (LinearLayout) super.findViewById(R.id.blackBox);
		blackbox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				brush.setPaint(Color.BLACK);
				cpv.changeCenterColor(Color.BLACK);
			}
		});
		
		//Updates the brush to have the selected settings, and closes the dialog.
		Button okButton = (Button) super.findViewById(R.id.save);
		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				sListener.updateBrush(brush);
				dismiss();
			}
		});
	}

	/**
	 * ColorPickerView inspired by the view from Android's
	 * open source "FingerPaint.java".
	 */
	private static class ColorPickerView extends View {

		private Paint mPaint;
		private Paint mCenterPaint;
		private final int[] mColors;
		
		private static int CENTER_X;
		private static int CENTER_Y;
		private static int CENTER_RADIUS;;
		
		// Test numbers
		private int viewWidth;
		private int viewHeight;

		public ColorPickerView(Context c, int color) {
			super(c);

			mColors = new int[] {
					0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
					0xFFFFFF00, 0xFFFF0000
			};
			Shader s = new SweepGradient(0, 0, mColors, null);

			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setShader(s);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeWidth(25);

			mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mCenterPaint.setColor(color);
			mCenterPaint.setStrokeWidth(10);	
		}

		private boolean mTrackingCenter;
		private boolean mHighlightCenter;

		/**
		 * Paint the circle with the colors.
		 */
		@Override
		protected void onDraw(Canvas canvas) {
			float r = CENTER_X - mPaint.getStrokeWidth()*0.5f;

			canvas.translate(CENTER_X, CENTER_X);

			canvas.drawOval(new RectF(-r, -r, r, r), mPaint);
			canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);

			if (mTrackingCenter) {
				int c = mCenterPaint.getColor();
				mCenterPaint.setStyle(Paint.Style.STROKE);

				if (mHighlightCenter) {
					mCenterPaint.setAlpha(0xFF);
				} else {
					mCenterPaint.setAlpha(0x80);
				}
				canvas.drawCircle(0, 0,
						CENTER_RADIUS + mCenterPaint.getStrokeWidth(),
						mCenterPaint);

				mCenterPaint.setStyle(Paint.Style.FILL);
				mCenterPaint.setColor(c);
			}
		}

		@Override
		public void onWindowFocusChanged(boolean hasWindowFocus) {
			super.onWindowFocusChanged(hasWindowFocus);
			viewHeight = this.getMeasuredHeight();
			viewWidth  = this.getMeasuredWidth();
			CENTER_X   = (int) (viewWidth*0.5);
			CENTER_Y   = (int) (viewHeight*0.5);
			CENTER_RADIUS = (int) (viewWidth * 0.2);
		}

		private int ave(int s, int d, float p) {
			return s + java.lang.Math.round(p * (d - s));
		}

		/**
		 * Interpret the colors as argb values.
		 */
		private int interpColor(int colors[], float unit) {
			if (unit <= 0) {
				return colors[0];
			}
			if (unit >= 1) {
				return colors[colors.length - 1];
			}

			float p = unit * (colors.length - 1);
			int i = (int)p;
			p -= i;

			// now p is just the fractional part [0...1) and i is the index
			int c0 = colors[i];
			int c1 = colors[i+1];
			int a = ave(Color.alpha(c0), Color.alpha(c1), p);
			int r = ave(Color.red(c0), Color.red(c1), p);
			int g = ave(Color.green(c0), Color.green(c1), p);
			int b = ave(Color.blue(c0), Color.blue(c1), p);

			return Color.argb(a, r, g, b);
		}

		private static final float PI = 3.1415926f;

		/**
		 * Change the circle in the middle to get the color which
		 * the user selects. When letting go of the screen, set
		 * the brush color to the selected color.
		 */
		@Override
		public boolean onTouchEvent(MotionEvent event) {

			float x = event.getX() - CENTER_X;
			float y = event.getY() - (CENTER_Y/2);
			
			boolean inCenter = java.lang.Math.sqrt(x*x + y*y) <= CENTER_RADIUS;

			switch (event.getAction()) {
			
			case MotionEvent.ACTION_MOVE:
				if (mTrackingCenter) {
					if (mHighlightCenter != inCenter) {
						mHighlightCenter = inCenter;
						invalidate();
					}
				} else {
					float angle = (float)java.lang.Math.atan2(y, x);
					// need to turn angle [-PI ... PI] into unit [0....1]
					float unit = angle/(2*PI);
					if (unit < 0) {
						unit += 1;
					}
					mCenterPaint.setColor(interpColor(mColors, unit));
					
					invalidate();
				}
				break;
			case MotionEvent.ACTION_UP:
				brush.setPaint(mCenterPaint.getColor());
				invalidate();
				break;
			}
			return true;
		}
		
		/**
		 * Set the color of the circle in the middle.
		 */
		public void changeCenterColor(int c) {
			mCenterPaint.setColor(c);
			invalidate();
		}
	}
}
