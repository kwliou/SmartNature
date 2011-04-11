package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ZoomControls;

public class EditScreen extends Activity implements View.OnTouchListener, View.OnClickListener, ColorPickerDialog.OnColorChangedListener {
	final int ZOOM_DURATION = 3000;
	Garden mockGarden;
	ZoomControls zoom;
	EditView editView;
	Handler mHandler = new Handler();
	boolean showLabels = true, showFullScreen, rotateMode, zoomAutoHidden;
	boolean zoomPressed;
	int zoomLevel;
	Plot plot, oldPlot;
	Button rotateButton, saveButton;
	TextView mode_rotate;
	boolean firstInit = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		firstInit = savedInstanceState == null;
		showFullScreen = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("garden_fullscreen", false); 
		if (showFullScreen)
			setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		
		mockGarden = StartScreen.gardens.get(extras.getInt("garden_id"));
		setTitle(extras.getString("name") + " (Edit mode)"); 
		
		if(extras.containsKey("type")){
			int type = extras.getInt("type"); 
			String name = extras.getString("name");
			if(type == Plot.POLY) {
				Rect bounds = new Rect(270, 120, 270 + 90, 120 + 100);
				float[] pts = { 0, 0, 50, 10, 90, 100 };
				plot = new Plot(name, bounds, 0, pts);
			}	
			else {
				Rect bounds = new Rect(140, 120, 210, 190);
				plot = new Plot(name, bounds, 0, type);
			}
			mockGarden.addPlot(plot);
		}
		else {
			plot = mockGarden.getPlots().get(extras.getInt("plot_id"));
			oldPlot = new Plot(plot);
		}
		plot.getPaint().setStrokeWidth(7);
		
		setContentView(R.layout.edit_plot);
		editView = (EditView) findViewById(R.id.edit_view);
		
		if (firstInit) {
			zoomLevel = extras.getInt("zoom_level");
			editView.dragMatrix.setValues(extras.getFloatArray("drag_matrix"));
			editView.bgDragMatrix.setValues(extras.getFloatArray("bgdrag_matrix"));
			editView.onAnimationEnd();
		}
		
		zoom = (ZoomControls) findViewById(R.id.edit_zoom_controls);
		zoomAutoHidden = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("zoom_autohide", false);
		if (zoomAutoHidden)
			zoom.setVisibility(View.GONE);
		zoom.setOnZoomInClickListener(zoomIn);
		zoom.setOnZoomOutClickListener(zoomOut);

		boolean hintsOn = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("show_hints", true);
		if (hintsOn) {
			((TextView)findViewById(R.id.edit_hint)).setText(R.string.hint_editscreen);
			((TextView)findViewById(R.id.edit_hint)).setVisibility(View.VISIBLE);
		}

		rotateButton = (Button) findViewById(R.id.rotateButton);
		rotateButton.setOnClickListener(rotate);
		saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(save);
		mode_rotate = (TextView) findViewById(R.id.mode_rotate);

		editView.invalidate();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (firstInit) {
			TranslateAnimation anim = new TranslateAnimation(0, 0, findViewById(R.id.footer).getHeight(), 0);
			anim.setDuration(250);
			findViewById(R.id.footer).startAnimation(anim);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("zoom_level", zoomLevel);
		float[] values = new float[9], bgvalues = new float[9];
		editView.dragMatrix.getValues(values);
		editView.bgDragMatrix.getValues(bgvalues);
		savedInstanceState.putFloatArray("drag_matrix", values);
		savedInstanceState.putFloatArray("bgdrag_matrix", bgvalues);
		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		zoomLevel = savedInstanceState.getInt("zoom_level");
		editView.dragMatrix.setValues(savedInstanceState.getFloatArray("drag_matrix"));
		editView.bgDragMatrix.setValues(savedInstanceState.getFloatArray("bgdrag_matrix"));
		editView.onAnimationEnd();	
	}
	
	public void onBackPressed() {
		plot.getPaint().setStrokeWidth(3);
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putInt("zoom_level", zoomLevel);
		float[] values = new float[9], bgvalues = new float[9];
		editView.dragMatrix.getValues(values);
		editView.bgDragMatrix.getValues(bgvalues);
		bundle.putFloatArray("drag_matrix", values);
		bundle.putFloatArray("bgdrag_matrix", bgvalues);
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		finish();
		overridePendingTransition(0, 0);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			onBackPressed();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.plot_edit_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.m_change_color:
			int color = PreferenceManager.getDefaultSharedPreferences(EditScreen.this).getInt("color",Color.WHITE);
			new ColorPickerDialog(EditScreen.this, EditScreen.this, color).show();
			break;
		case R.id.m_resetzoom:
			zoomLevel = 0;
			mockGarden.refreshBounds();
			editView.reset();
			break;
		case R.id.m_revert:
			plot.set(oldPlot);
			plot.getPaint().setStrokeWidth(7);
			editView.invalidate();
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		System.err.println("touched");
		return false;
	}

	@Override
	public void onClick(View view) {
		System.err.println("clicked");
	}
	
	View.OnClickListener rotate = new View.OnClickListener() {
		@Override
		public void onClick(View view) {			
			rotateMode = !rotateMode;
			mode_rotate.setText("Rotate Mode is " + (rotateMode ? "ON" : "OFF"));
			editView.invalidate();
		}
	};
	
	View.OnClickListener save = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			onBackPressed();
		}
	};
	
	View.OnClickListener zoomIn = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			handleZoom();
			if (!zoomPressed) {
				zoomPressed = true;
				ScaleAnimation anim = new ScaleAnimation(1, 1.5f, 1, 1.5f, editView.getWidth() / 2.0f, editView.getHeight() / 2.0f);
				anim.setDuration(400);
				anim.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation anim) { }
					@Override
					public void onAnimationRepeat(Animation anim) { }
					@Override
					public void onAnimationEnd(Animation anim) { zoomLevel++; }
				});
				editView.startAnimation(anim);
			}
		}
	};
	
	View.OnClickListener zoomOut = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			handleZoom();
			if (!zoomPressed) {
				zoomPressed = true;
				ScaleAnimation anim = new ScaleAnimation(1, 1/1.5f, 1, 1/1.5f, editView.getWidth() / 2.0f, editView.getHeight() / 2.0f); 
				anim.setDuration(400);
				anim.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation anim) { }				
					@Override
					public void onAnimationRepeat(Animation anim) { }
					@Override
					public void onAnimationEnd(Animation anim) { zoomLevel--; }
				});
				editView.startAnimation(anim);
			}
		}
	};

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

	@Override
	public void colorChanged(int color) {
		PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("color", color).commit();
		plot.getPaint().setColor(color);
		editView.invalidate();
	}

}