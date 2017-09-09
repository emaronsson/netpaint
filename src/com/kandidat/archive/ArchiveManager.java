package com.kandidat.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Environment;
import android.widget.Toast;

import com.kandidat.rityta.DrawingView;
import com.kandidat.R;

/**
 * Class for handling loading and saving of images.
 */
public class ArchiveManager {

	private static final int TOAST_SHOWTIME = 2;
	
	private boolean available = false;
	private boolean writable  = false;
	
	private File path;
	private File file;
	private Context context;
	private Canvas canvas;
	private String state;
	
	public ArchiveManager(Context c) {
		this.context = c;
		
		// External storage permission check
		state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			
			 //Uses programs local storage
			 //CAUTION! Files removed when app uninstalled
			 
			String packageName  = context.getPackageName();
			File   externalPath = Environment.getExternalStorageDirectory();
			path = new File(externalPath.getAbsolutePath() +
					"/Android/data/" + packageName + "/files");
			
			// create dirs if first run
			if(!path.exists())
				path.mkdirs();

			available = true;
			writable  = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			available = true;
			writable  = false;
		} else {
			available = false;
			writable  = false;
		}
	}
	
	/**
	 * Save the given bitmap as a file with given file name.
	 **/
	public void saveImage(Bitmap bitmapToSave, String s) {
		OutputStream out = null;
		Bitmap saveThis;

		if (available == true && writable == true) {
			// Set up bitmap to be saved
			saveThis = Bitmap.createBitmap(
							bitmapToSave.getWidth(),
							bitmapToSave.getHeight(),
							bitmapToSave.getConfig());
			
			// Create canvas upon which to draw the bitmap
			canvas = new Canvas(saveThis);
			
			// Create output file
			file = new File(path, s+".png");
			try {
				out = new FileOutputStream(file);

				// Build the file and write it to disk
				canvas.drawColor(0xFFFFFFFF);
				canvas.drawBitmap(bitmapToSave, 0, 0, null);
				saveThis.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.flush();
				out.close();

				// Show success notification
				makeToast(R.string.pic_saved);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else
			makeToast(R.string.external_notavailable);
	}
	
	/**
	 * Return the pixels of the image with given file name.
	 **/
	public Buffer loadImage(String filename, DrawingView v) {
		if (writable != true)
			return null;
		
		InputStream in = null;
		File f = new File(path, filename);
		
		// Try to connect, abandon if failed
		try   { in = new FileInputStream(f); } 
		catch (FileNotFoundException ignored) { }
		
		// Load result to buffer and return
		Bitmap loaded = BitmapFactory.decodeStream(in);
		ByteBuffer pixels = ByteBuffer.allocate(loaded.getRowBytes() * loaded.getHeight() * 10);
		
		loaded.copyPixelsToBuffer(pixels);
		
		return pixels;
	}
	
	/**
	 * Prints a R.string id through a Toast message.
	 **/
	public void makeToast(int id) {
		Toast.makeText(context, context.getResources().getString(id), TOAST_SHOWTIME)
			 .show();
	}
	
	/**
	 * Return the path of the file.
	 **/
	public File getPath() {
		return path;
	}
	
	/** 
	 * Check if a file with given name exists.
	 **/
	public boolean checkFile(String s) {
		String test = s + ".png";
		File f = new File(path, test);
		return (f.exists());
	}
}
