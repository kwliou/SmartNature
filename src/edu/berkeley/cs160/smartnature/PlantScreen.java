package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
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
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ZoomControls;

public class PlantScreen extends Activity implements View.OnTouchListener, View.OnClickListener {
	
	final int ZOOM_DURATION = 3000;
	Plant mockPlant;
	AlertDialog dialog;
	ZoomControls zoom;
	GardenView gardenView;
	Handler mHandler = new Handler();
	boolean showLabels = true, showFullScreen;
	int zoomLevel;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//showFullScreen = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("garden_fullscreen", false); 
		//if (showFullScreen)
			//setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);
		super.onCreate(savedInstanceState);
		mockPlant = new Plant("");

		/*
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("name")) {
			setTitle(extras.getString("name"));
			//mockGarden.setName(extras.getString("name"));
		} else {
			showDialog(0);
		}
		*/
		//showDialog(0);
		setContentView(R.layout.plant);
		/*

		
		boolean hintsOn = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("show_hints", true);
		if (hintsOn) {
			((TextView)findViewById(R.id.garden_hint)).setText(R.string.hint_gardenscreen);
			((TextView)findViewById(R.id.garden_hint)).setVisibility(View.VISIBLE);
		}
		*/
		
		Button addPicButton = (Button) findViewById(R.id.addPicButton);
		Button addEntryButton = (Button) findViewById(R.id.addEntryButton);
		Button backButton = (Button) findViewById(R.id.back2PlotButton);
		
		/*
		addPicButton.setOnClickListener(new OnClickListener() {
			@Override
      public void onClick(View v) {
				// Call Deepti's Picture dialog
      }
    });
		addEntryButton.setOnClickListener(new OnClickListener() {
			@Override
      public void onClick(View v) {
				// Call Deepti's Entry dialog
      }
    });
		*/
		backButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
      	finish();
      }
		});
		

    
	}
	
	public void initMockData() {
		mockPlant.addEntry(new Entry("Entry 1", "4/1"));
		mockPlant.addEntry(new Entry("Entry 2", "4/15"));
	}

	@Override
	public Dialog onCreateDialog(int id) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.text_entry_dialog, null);
		DialogInterface.OnClickListener confirmed = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				EditText plantName = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
				setTitle(plantName.getText().toString());
				mockPlant.setName(plantName.getText().toString());
			}
		};
		DialogInterface.OnClickListener canceled = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				finish();
			}
		};
		dialog = new AlertDialog.Builder(this)
			.setTitle(R.string.new_plant_prompt)
			.setView(textEntryView)
			.setPositiveButton(R.string.alert_dialog_ok, confirmed)
			.setNegativeButton(R.string.alert_dialog_cancel, canceled)
			.create();
		
		// automatically show soft keyboard
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
			case R.id.m_home:
				finish();
				break;
			case R.id.m_resetzoom:
				zoomLevel = 0;
				gardenView.reset();
				break;
			case R.id.m_share:
				startActivity(new Intent(this, ShareGarden.class));
				break;
			case R.id.m_showlabels:
				showLabels = !showLabels;
				item.setTitle(showLabels ? "Hide labels" : "Show labels");
				gardenView.invalidate();				
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		System.out.println("touched");
		return false;
	}
	
	@Override
	public void onClick(View view) {
		System.out.println("clicked");
	}

}
