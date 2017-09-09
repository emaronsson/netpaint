package com.kandidat.rityta;

import java.nio.Buffer;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.kandidat.rityta.zoom.ZoomRatio;
import com.kandidat.rityta.zoom.ZoomState;


/**
 * Class for representing the drawing surface.
 */
public class DrawingView extends View implements Observer  {

	// Debugging
	private final boolean D  = false;
	private final String tag = "DrawingView";
	
	/** Move threshold for recognizing a move and click */
	public static final float TOUCH_TOLERANCE = 4f;

	/** Source rectangle to select scaled image to draw from */
	private final Rect rectSrc = new Rect();
	
	/** Destination rectangle to draw scaled images too, usually the display */
	private final Rect rectDst = new Rect();
	
	/** Container for information of the current image scaling setting */
	private final ZoomRatio zoomRatio = new ZoomRatio();
	
	/** Container for information of the current image pan and zoom setting */
	private ZoomState zoomState;
	
	/** Boolean to check if zoom is enabled, i.e. zoomState == null */
	protected boolean zoomEnabled;
	
	/** Bitmap which we draw all paths on*/
	public Bitmap bitmap;
	
	/** Canvas used to draw from, linked to our bitmap */
	public Canvas canvas;
	
	/** Paint used to draw the bitmap */
	public Paint  bitPaint;
	
	/** Buffer containing a background image when one is loaded */
	public Buffer background;

	/** Path for recording the users drawing motions */
	protected DrawingPath currPath;
	
	/** Path for translating the users drawing motions to scaled plane */
	protected DrawingPath scaledPath;
	
	/** Brush associated with the users current DrawingPath */
	public Brush currBrush;
	
	/** Used to scale the brush size to match what is drawn */
	private float scaledBrushRatio;
	
	/** Coordinates for last recorded drawn point */
	private float mX;
	private float mY;
	
	/** Coordinates for last recorded scaled drawn point */
	private float sX;
	private float sY;
	
	// ------------------------- Constructors ---------------------------- //
	
	public DrawingView(Context context) {
		super(context);
		init(context);
	}
	
	public DrawingView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    init(context);
	}
	
	protected void init(Context context) {
		bitPaint   = new Paint(Paint.DITHER_FLAG);
		scaledPath = new DrawingPath();
		currPath   = new DrawingPath();
		currBrush  = new Brush(10f, Color.BLACK);
	}
	
	// -------------------------- View methods --------------------------- //
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (bitmap != null) {
			// Set the background of the canvas
			canvas.drawColor(0x000000FF);
			
			if (zoomEnabled) {
				// Calculate what's to be drawn
				calculateZoomRectangles();
				
				// Draw previous paths already saved to the bitmap
				canvas.drawBitmap(bitmap, rectSrc, rectDst, bitPaint);
				
				// Calculate scaling of width on shown brush to match that of drawn paths size
				final Paint p = new Paint(currPath.getBrush().getPaint());
				p.setStrokeWidth(p.getStrokeWidth() * scaledBrushRatio);
				
				// Draw the current path if user is drawing
				canvas.drawPath(currPath.getPath(), p);
			} else {
				// Draw previous paths already saved to the bitmap
				canvas.drawBitmap(bitmap, 0, 0, bitPaint);
				
				// Draw the current path if user is drawing
				canvas.drawPath(currPath.getPath(), currPath.getBrush().getPaint());
			}
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (zoomState != null && bitmap != null) {
			zoomRatio.updateZoomRatio(right - left, bottom - top, bitmap.getWidth(), bitmap.getHeight());
	        zoomRatio.notifyObservers();
	        zoomEnabled = true;
	        
	        calculateZoomRectangles();
		} else 
			zoomEnabled = false;
		
        if (changed)
        	redrawPaths();
	}
	
	// ------------------------- Touch methods --------------------------- //

	/** 
	 * Creates a new path and starts to track it 
	 */
	public void touch_down(float x, float y) {
		final long timeStamp =  System.currentTimeMillis();
		
		if (D) Log.d(tag, "-- touch_down()\n- timeStamp: " + timeStamp);
		
		currPath.setBrush(currBrush);
		currPath.getPath().reset();
		currPath.getPath().moveTo(x, y);
		currPath.setTimeStamp(timeStamp);
		mX = x;   
		mY = y;
		
		if (zoomEnabled) {
			PointF p = scaleCoordinates(x, y);
			
			scaledPath.setBrush(currBrush);
			scaledPath.getPath().reset();
			scaledPath.getPath().moveTo(p.x, p.y);
			scaledPath.setTimeStamp(timeStamp);
			
			saveCoordinates(p.x, p.y);
		} else {
			saveCoordinates(x, y);
		}
	}

	/** 
	 * Builds on the path if at least one of the new coordinates
	 * exceed the touch tolerance 
	 */
	public boolean touch_move(float x, float y) {		
		final float dx = Math.abs(x - mX);
		final float dy = Math.abs(y - mY);
		
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			currPath.getPath().quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
			mX = x;  
			mY = y;
			
			if (zoomEnabled) {
				PointF p = scaleCoordinates(x, y);
				scaledPath.getPath().quadTo(sX, sY, (p.x + sX)/2, (p.y + sY)/2);
				
				saveCoordinates(p.x, p.y);
			} else {
				saveCoordinates(x, y);
			}
			
			return true;
		}
		
		return false;
	}

	/** 
	 * Stores, resets and draws the path to the canvas 
	 */
	public void touch_up() {
		currPath.getPath().lineTo(mX, mY);
		if (zoomEnabled) {
			scaledPath.getPath().lineTo(sX, sY);
			drawPath(scaledPath);
			scaledPath.getPath().reset();
			scaledPath.clearCoords();
		} else
			drawPath(currPath);
		currPath.getPath().reset();
		currPath.clearCoords();
		
		if (currRedoLenght() > 0)
			synchronized (DrawingActivity.redo) {
				DrawingActivity.redo.clear();
			}
	}
	
	/**
	 * Middle man function to draw and save a path, so we can
	 * override this in multiplayer and draw paths order instead.
	 */
	protected void drawPath(DrawingPath path) {
		canvas.drawPath(path.getPath(), currBrush.getPaint());
		
		synchronized (DrawingActivity.path) {
          DrawingActivity.path.add(path.clone());
		}
	}
	
	/**
	 * Middle man function to save coordinates, so we can
	 * override this and listen to the paths being drawn
	 */
	protected void saveCoordinates(float x, float y) {
		sX = x;
		sY = y;
	}
	
	// ----------------------- Image aux. methods ------------------------ //
	
	/**
	 * Undo last drawn path.
	 * The path is then put in the re-do buffer.
	 */
	public void undo() {
		final int lenght = currUndoLenght();
		System.out.println("Removed path. Length of list: " + lenght);
		if (lenght > 0)
			undo(lenght - 1);
	}
	
	/**
	 * Undo the path with the given index.
	 */
	protected void undo(int index) {
		canvas.drawColor(Color.WHITE);
		
		DrawingPath path;
		synchronized (DrawingActivity.path) {
            path = DrawingActivity.path.remove(index);
		}
		
		synchronized (DrawingActivity.redo) {
			DrawingActivity.redo.add(path.clone());
		}
		
		redrawPaths();
	}

	/**
	 * Redo last path that has been undone and add the path to the undo buffer.
	 * Since we only need to draw one path, its more efficient to do that here.
	 */
	public void redo() {
		final int lenght = currRedoLenght();
		if (lenght > 0)
			redo(lenght - 1);
	}
	
	/**
	 * Redo the path with the given index, and returns it.
	 */
	protected DrawingPath redo(int index) {
		DrawingPath path;
		synchronized (DrawingActivity.redo) {
			path = DrawingActivity.redo.remove(index);
		}
		
		synchronized (DrawingActivity.path) {
            DrawingActivity.path.add(path.clone());
		}
		
		canvas.drawPath(path.getPath(), path.getBrush().getPaint());
		invalidate();
		
		return path;
	}
	
	/**
	 * Redraws all drawn paths, and background, to the canvas and invalidates
	 */
	public void redrawPaths() {
		if(background != null)
			bitmap.copyPixelsFromBuffer(background);
		
		synchronized (DrawingActivity.path) {
			for (DrawingPath path : DrawingActivity.path)
				canvas.drawPath(path.getPath(), path.getBrush().getPaint());
		}
		
		invalidate();
	}
	
	// -------------------------- Zoom methods---------------------------- //

	/** 
	 * Sets this views ZoomState and adds itself as an observer to it.
	 */
	public void setZoomState(ZoomState zoomState) {
		if (this.zoomState != null)
			this.zoomState.deleteObservers();

		this.zoomState = zoomState;
		this.zoomState.addObserver(this);
		
		invalidate();
	}
	
	/**
	 * Transforms the x and y coordinates from the rectDst
	 * plane to the rectSrc plane.
	 */
	public PointF scaleCoordinates(float x, float y) {
		return new PointF(
				(x - rectDst.left) / (rectDst.right - rectDst.left) * (rectSrc.right - rectSrc.left) + rectSrc.left,
				(y - rectDst.top) / (rectDst.bottom - rectDst.top) * (rectSrc.bottom - rectSrc.top) + rectSrc.top);
	}
	
	/**
	 * Calculates the scaled imaged that should be displayed
	 */
	public void calculateZoomRectangles() {
		final float ratio = zoomRatio.get();
		
		final int viewWidth = getWidth();
        final int viewHeight = getHeight();
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();
        
        final float panX = zoomState.getPanX();
        final float panY = zoomState.getPanY();
        final float zoomX = zoomState.getZoomX(ratio) * viewWidth / bitmapWidth;
        final float zoomY = zoomState.getZoomY(ratio) * viewHeight / bitmapHeight;

        // Setup source and destination rectangles
        rectSrc.left   = (int) (panX * bitmapWidth - viewWidth / (zoomX * 2));
        rectSrc.top    = (int) (panY * bitmapHeight - viewHeight / (zoomY * 2));
        rectSrc.right  = (int) (rectSrc.left + viewWidth / zoomX);
        rectSrc.bottom = (int) (rectSrc.top + viewHeight / zoomY);
        
        rectDst.left   = getLeft();
        rectDst.top    = getTop();
        rectDst.right  = getRight();
        rectDst.bottom = getBottom();

        // Adjust source rectangle so that it fits within the source image.
        if (rectSrc.left < 0) {
            rectDst.left += -rectSrc.left * zoomX;
            rectSrc.left = 0;
        }
        if (rectSrc.right > bitmapWidth) {
            rectDst.right -= (rectSrc.right - bitmapWidth) * zoomX;
            rectSrc.right = bitmapWidth;
        }
        if (rectSrc.top < 0) {
            rectDst.top += -rectSrc.top * zoomY;
            rectSrc.top = 0;
        }
        if (rectSrc.bottom > bitmapHeight) {
            rectDst.bottom -= (rectSrc.bottom - bitmapHeight) * zoomY;
            rectSrc.bottom = bitmapHeight;
        }
        
        // Calculate scaled brush width in new plane
        scaledBrushRatio = ((float) (rectDst.right - rectDst.left)) 
        			     / ((float) (rectSrc.right - rectSrc.left));
	}
	
	// -------------------------- Observer methods ----------------------- //
	
	@Override
	public void update(Observable observable, Object data) {
		invalidate();
	}
	
	// ------------------------- Getters & Setters ----------------------- //
	
	private int currUndoLenght() {
		return DrawingActivity.path.size();
	}

	private int currRedoLenght() {
		return DrawingActivity.redo.size();
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		this.canvas = new Canvas(this.bitmap);
		
		zoomRatio.updateZoomRatio(getWidth(), getHeight(), bitmap.getWidth(), bitmap.getHeight());
        zoomRatio.notifyObservers();
		invalidate();
	}
	
	public Brush getCurrentBrush() {
		return currBrush;
	}
	
	public void setCurrentBrush(Brush brush) {
		currBrush = brush;
	}
	
	public void setBackground(Buffer buffer) {
		background = buffer;
		bitmap.copyPixelsFromBuffer(background);
		
		invalidate();
	}

	public PointF getLastPoint() {
		if (zoomEnabled)
			return new PointF(sX, sY);
		return new PointF(mX, mY);
	}

	public ZoomRatio getZoomRatio() {
		return zoomRatio;
	}

	public Rect getSrcRect() {
		return rectSrc;
	}
	
	public Rect getDstRect() {
		return rectDst;
	}
	
	public float getZoomX() {
		return zoomState.getZoomX(zoomRatio.get());
	}
	
	public float getZoomY() {
		return zoomState.getZoomY(zoomRatio.get());
	}
}
