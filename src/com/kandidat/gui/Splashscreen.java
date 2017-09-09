package com.kandidat.gui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import com.kandidat.R;

/**
 * Class for the splash screen that will show in the beginning.
 */
public class Splashscreen extends Activity {
	  
	protected boolean _active = true;
	
	protected int _splashTime = 1000; // time to display the splash screen in ms	
	
	/** Special font used for the interactive menus */
	private Typeface tf;
	
	/** 
	 * Called when the activity is first created. 
	 **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.splashscreen);
        tf = Typeface.createFromAsset(getAssets(),
                "fonts/PAINP___.TTF");
        TextView tv = (TextView) findViewById(R.id.title);
        tv.setTypeface(tf);
        TextView tv2 = (TextView) findViewById(R.id.title2);
        tv2.setTypeface(tf);
        
        // thread for displaying the SplashScreen
        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while(_active && (waited < _splashTime)) {
                        sleep(100);
                        if(_active) {
                            waited += 100;
                        }
                    }
                } catch(InterruptedException e) {} 
                finally {
                    finish();
                    Intent intent = new Intent(Splashscreen.this, Main.class);
                    startActivity(intent);
                }
            }
        };
        splashTread.start();
    }
      
    /**
     * Inactivate splash screen if pressing it. 
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            _active = false;
        }
        return true;
    }
    
}
