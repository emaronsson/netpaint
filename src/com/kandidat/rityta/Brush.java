package com.kandidat.rityta;

import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/**
 * Class for representing a brush and 
 * all its options.
 */
public class Brush implements Cloneable {

	private Paint paint;
	
	private boolean[] activeEffects;
	
	/**
	 * Sets the specified values for the brush.
	 */
	public Brush(float width, int color) {
		paint = new Paint();
		paint.setColor(color);
		paint.setStrokeWidth(width);
		paint.setXfermode(null);
		paint.setAlpha(0xFF);
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStyle(Style.STROKE);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setPathEffect(new CornerPathEffect(10));
		activeEffects = new boolean[4];
	}
	
	public Brush(float width, int color,boolean[] effects) {
		paint = new Paint();
		paint.setColor(color);
		paint.setStrokeWidth(width);
		paint.setXfermode(null);
		paint.setAlpha(0xFF);
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStyle(Style.STROKE);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setPathEffect(new CornerPathEffect(10));
		activeEffects = new boolean[4];
		setActiveEffects(effects);
	}
	
	/**
	 * Set the color of the brush.
	 */
	public void setPaint(Paint paint) {
		this.paint = paint;
	}
	
	/**
	 * Set the color of the brush, keeping the alpha value.
	 */
	public void setPaint(int color) {
		final short a = (short) ((color & 0xFF000000) >>> 24);
		int alpha = this.paint.getAlpha() & a;
		this.paint.setColor(color);
		paint.setAlpha(alpha);
	}
	
	/**
	 * Set the size of the brush.
	 */
	public void setSize(int size)
	{
		paint.setStrokeWidth(size);
	}
	
	/**
	 * Set the effect of the brush to embossing.
	 */
	public void enableEmboss()
	{
		paint.setMaskFilter(
				new EmbossMaskFilter(
						new float[] { 1, 1, 1 },
						0.4f,
						10,
						8.2f));
	}
	
	/**
	 * Set the effect of the brush to blur.
	 */
	public void enableBlur()
	{
		paint.setMaskFilter(
				new BlurMaskFilter(15, Blur.SOLID));
	}
	
	/**
	 * Set the effect of the brush to shadow.
	 */
	public void enableShadow()
	{
		paint.setAlpha(50);
	}

	/**
	 * Return the color of the brush.
	 */
	public Paint getPaint() {
		return paint;
	}
	
	/**
	 * Return the size of the brush.
	 */
	public float getSize() {
		return paint.getStrokeWidth();
	}
	
	/**
	 * Returns a clone of the brush.
	 */
	public Brush clone() {
		try{
			Brush result = (Brush) super.clone();
			result.paint = new Paint(paint);
			result.activeEffects = new boolean[4];
			for(int i = 0; i<activeEffects.length;i++)
				result.activeEffects[i] = activeEffects[i];
			return result;
		} catch(CloneNotSupportedException e) {
			return null;
		}
	}
	
	/**
	 * Make the brush into an eraser.
	 */
	public void setEraser()
	{
		paint.setColor(Color.WHITE);
		paint.setMaskFilter(null);
		paint.setAlpha(255);
		setActiveEffect(0);
	}
	
	/**
	 * Remove effects and alpha.
	 */
	public void resetBrush() {
		paint.setMaskFilter(null);
		paint.setAlpha(255);
		setActiveEffect(0);
	}
	
	/**
	 * Set the effect that is marked with true in the array.
	 */
	public void setActiveEffects(boolean[] effects){
		resetEffects();
		paint.setAlpha(0xFF);
		if(effects[1])
			enableEmboss();
		if(effects[2])
			enableBlur();
		if(effects[3])
			enableShadow();
	}
	
	/**
	 * Set the effect with the given index.
	 */
	public void setActiveEffect(int index) {
		resetEffects();
		activeEffects[index] = true;
	}
	
	/**
	 * Return the effects.
	 */
	public boolean[] getActiveEffects() {
		return activeEffects;
	}
	
	/**
	 * Return the index of the current effect.
	 */
	public int getEffect() {
		for (int i = 0; i < activeEffects.length; i++) {
			if (activeEffects[i] == true)
				return i;
		}
		return 0;
	}
	
	/**
	 * Turn off all effects.
	 */
	private void resetEffects(){
		for (int i = 0; i < activeEffects.length; i++)
			activeEffects[i] = false;
	}
}
