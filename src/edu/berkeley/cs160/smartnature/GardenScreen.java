package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ZoomControls;

import java.util.ArrayList;

public class GardenScreen extends Activity implements OnTouchListener, OnClickListener {
	
	final int ZOOM_DURATION = 3000;
	ArrayList<Plot> plots;
	AlertDialog dialog;
	ZoomControls zoom;
	Handler mHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new Handler();
		Bundle extras = getIntent().getExtras();
		setTitle(extras.getString("name"));
		
		initMockData();
		setContentView(R.layout.garden);
		findViewById(R.id.garden_layout).setOnTouchListener(this);
		zoom = (ZoomControls) findViewById(R.id.zoom_controls);
		zoom.setVisibility(View.GONE);
		zoom.setOnZoomInClickListener(this);
		zoom.setOnZoomOutClickListener(this);
	}
	
	public void initMockData() {
		plots = new ArrayList<Plot>();
		ShapeDrawable s1 = new ShapeDrawable(new RectShape());
		s1.setBounds(20, 60, 80, 200);
		ShapeDrawable s2 = new ShapeDrawable(new OvalShape());
		s2.setBounds(140, 120, 190, 190);
		plots.add(new Plot(s1, "Jerry's Plot"));
		plots.add(new Plot(s2, "Amy's Plot"));
	}

	@Override
	public Dialog onCreateDialog(int id) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
		DialogInterface.OnClickListener confirmed = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				Intent intent = new Intent(GardenScreen.this, EditRegion.class);
				Bundle bundle = new Bundle();
				String regionName = ((EditText) textEntryView.findViewById(R.id.dialog_text_entry)).getText().toString();
				bundle.putString("name", regionName);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		};
		dialog = new AlertDialog.Builder(this)
			.setTitle(R.string.new_region_prompt)
			.setView(textEntryView)
			.setPositiveButton(R.string.alert_dialog_ok, confirmed)
			.setNegativeButton(R.string.alert_dialog_cancel, null)
			.create();

		EditText input = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
		        if (hasFocus)
		            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		    }
		});

		return dialog;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.garden_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.m_addregion:
				showDialog(0);
				break;
			case R.id.m_home:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		handleZoom();
		return false;
	}
	
	@Override
	public void onClick(View view) {
		handleZoom();
		
	}
	
	public void handleZoom() {
		mHandler.removeCallbacks(autoHide);
		if (!zoom.isShown())
			zoom.show(); //zoom.setVisibility(View.VISIBLE);
		mHandler.postDelayed(autoHide, ZOOM_DURATION);
	}
	Runnable autoHide = new Runnable() {	
		@Override
		public void run() {
			if (zoom.isShown()) {
				mHandler.removeCallbacks(autoHide);			
				zoom.hide(); //zoom.setVisibility(View.GONE);
			}
		}
	};

}