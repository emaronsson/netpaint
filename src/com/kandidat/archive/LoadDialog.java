package com.kandidat.archive;

import java.io.File;
import java.nio.Buffer;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.kandidat.rityta.DrawingActivity;
import com.kandidat.rityta.DrawingView;
import com.kandidat.R;

/**
 * Dialog for loading images.
 */
public class LoadDialog extends Dialog {

	private Context context;
	private DrawingView view;
	private ArchiveManager fm;
	private File path;
	
	public LoadDialog(Context c, DrawingView v) {
		super(c);
		this.setTitle(R.string.load_dialog_title);
		
		context = c;
		view    = v;
		fm      = new ArchiveManager(context);
		path    = fm.getPath();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filehandler_load);
		
		// array to hold the filenames
		final String[] items = path.list();
		
		ListView lv = (ListView) findViewById(R.id.top_list);
		lv.setAdapter(new ArrayAdapter<String>(context, R.layout.load_list_item, items));
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Buffer b = fm.loadImage(items[arg2], view);
				
				// Clear buffers to avoid pre-images reloading bugs
				DrawingActivity.path.clear();
				DrawingActivity.redo.clear();
				view.setBackground(b);
				
				dismiss();
			}
		});
	}
}
