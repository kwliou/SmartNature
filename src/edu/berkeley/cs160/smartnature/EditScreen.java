package edu.berkeley.cs160.smartnature;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ZoomControls;

public class EditScreen extends Activity implements View.OnClickListener, View.OnFocusChangeListener, View.OnTouchListener, ColorPickerDialog.OnColorChangedListener {

	Garden garden;
	EditView editView;
	Plot plot, oldPlot;
	Bundle extras;
	ZoomControls zoomControls;

	boolean footerShown;
	/** false if activity has been previously started */
	boolean firstInit = true;
	/** User-related options */
	boolean createPoly;
	boolean hintsOn, showLabels = true, showFullScreen, zoomAutoHidden;
	/** describes what zoom button was pressed: 1 for +, -1 for -, and 0 by default */
	int zoomPressed;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		firstInit = savedInstanceState == null;
		loadPreferences();
		if (showFullScreen)
			setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);
		super.onCreate(savedInstanceState);
		
		extras = getIntent().getExtras();
		garden = GardenGnome.getGarden(extras.getInt("garden_id"));
		setTitle(extras.getString("name") + " (Edit mode)"); 
		
		if (extras.getInt("type") == Plot.POLY)
			createPoly = true;
		
		if (firstInit && extras.containsKey("type"))
			createPlot();
		else
			loadPlot();
		
		if (firstInit) { 
			oldPlot = new Plot(plot);
			garden.addPlot(oldPlot);
		} else
			oldPlot = garden.getPlot(garden.size() - 1);
		
		plot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_edit));
		setContentView(R.layout.edit_plot);
		editView = (EditView) findViewById(R.id.edit_view);
		
		if (createPoly)
			((Button)findViewById(R.id.save_btn)).setText("Save shape");
		
		if (firstInit) {
			editView.zoomScale = extras.getFloat("zoom_scale");
			editView.dragMatrix.setValues(extras.getFloatArray("drag_matrix"));
			editView.bgDragMatrix.setValues(extras.getFloatArray("bgdrag_matrix"));
			editView.onAnimationEnd();
		}
		
		if (hintsOn) {
			TextView hint = (TextView) findViewById(R.id.edit_hint);
			hint.setText(createPoly ? R.string.hint_editpoly : R.string.hint_editscreen);
			hint.setVisibility(View.VISIBLE);
		}
		
		findViewById(R.id.edit_footer).getBackground().setAlpha(getResources().getInteger(R.integer.bar_trans));
		initButton(R.id.save_btn);
		initButton(R.id.edit_zoomfit_btn);
		
		zoomControls = (ZoomControls) findViewById(R.id.edit_zoom_controls);
		zoomControls.setOnZoomInClickListener(zoomIn);
		zoomControls.setOnZoomOutClickListener(zoomOut);
		if (zoomAutoHidden)
			zoomControls.setVisibility(View.GONE);
		
		editView.invalidate();
	}
	
	public void loadPreferences() {
		SharedPreferences prefs = getSharedPreferences("global", Context.MODE_PRIVATE);
		hintsOn = prefs.getBoolean("show_hints", true);
		showFullScreen = prefs.getBoolean("garden_fullscreen", false);
		zoomAutoHidden = prefs.getBoolean("zoom_autohide", false);
	}
	
	public void initButton(int id) {
		View view = findViewById(id);
		view.setOnClickListener(this);
		view.setOnFocusChangeListener(this);
		view.setOnTouchListener(this);
		view.getBackground().setAlpha(getResources().getInteger(R.integer.btn_trans));
	}
	
	public void createPlot() {
		RectF gBounds = garden.getRawBounds();
		if (garden.isEmpty()) {
			int width = getWindowManager().getDefaultDisplay().getWidth();
			int height = getWindowManager().getDefaultDisplay().getHeight();
			gBounds = new RectF(0, 0, width, height);
		}
		int type = extras.getInt("type");
		String name = extras.getString("name");
		Rect bounds = new Rect((int)gBounds.left, (int)gBounds.top, (int)gBounds.right, (int)gBounds.bottom);
		bounds.inset((int)gBounds.width()/3, (int)gBounds.height()/3);
		if (type == Plot.POLY)
			plot = new Plot(name, bounds, new float[] { 0, 0 });
		else
			plot = new Plot(name, bounds, type);
		
		String shape_s = "" + bounds.left + "," + bounds.top + "," + bounds.right + "," + bounds.bottom + "," + Color.BLACK;
		garden.addPlot(plot);
		//StartScreen.dh.insert_map_gp(extras.getInt("garden_id") + 1, StartScreen.dh.count_plot());
		garden.refreshBounds();
		//String bounds_s = "" + garden.getBounds().left + "," + garden.getBounds().top + "," + garden.getBounds().right + "," + garden.getBounds().bottom;
		//StartScreen.dh.update_garden(extras.getInt("garden_id") + 1, bounds_s);
	}
	
	public void loadPlot() {
		if (extras.containsKey("type"))
			plot = garden.getPlot(garden.size() - 2);
		else
			plot = garden.getPlot(extras.getInt("plot_id"));
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putFloat("zoom_scale", editView.zoomScale);
		savedInstanceState.putBoolean("portrait_mode", editView.portraitMode);
		savedInstanceState.putBoolean("create_poly", createPoly);
		if (createPoly)
			savedInstanceState.putFloatArray("poly_points", toFloatArray(editView.polyPts));
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
		editView.zoomScale = savedInstanceState.getFloat("zoom_scale");
		boolean prevPortraitMode = savedInstanceState.getBoolean("portrait_mode");
		boolean portraitMode = getWindowManager().getDefaultDisplay().getWidth() < getWindowManager().getDefaultDisplay().getHeight();
		createPoly = savedInstanceState.getBoolean("create_poly");
		if (createPoly) {
			float[] pts = savedInstanceState.getFloatArray("poly_points");
			for (float pt : pts)
				editView.polyPts.add(pt);
		}
		float[] values = savedInstanceState.getFloatArray("drag_matrix");
		float[] bgvalues = savedInstanceState.getFloatArray("bgdrag_matrix");
		
		if (portraitMode && !prevPortraitMode) { // changed from landscape to portrait
			float tmp = values[Matrix.MTRANS_X];
			values[Matrix.MTRANS_X] = -values[Matrix.MTRANS_Y];
			values[Matrix.MTRANS_Y] = tmp;
			tmp = bgvalues[Matrix.MTRANS_X];
			bgvalues[Matrix.MTRANS_X] = -bgvalues[Matrix.MTRANS_Y];
			bgvalues[Matrix.MTRANS_Y] = tmp;
		}
		else if (!portraitMode && prevPortraitMode) { // changed from portrait to landscape
			float tmp = values[Matrix.MTRANS_X];
			values[Matrix.MTRANS_X] = values[Matrix.MTRANS_Y];
			values[Matrix.MTRANS_Y] = -tmp;
			tmp = bgvalues[Matrix.MTRANS_X];
			bgvalues[Matrix.MTRANS_X] = bgvalues[Matrix.MTRANS_Y];
			bgvalues[Matrix.MTRANS_Y] = -tmp;
		}
		
		editView.dragMatrix.setValues(values);
		editView.bgDragMatrix.setValues(bgvalues);
		editView.onAnimationEnd();
	}
	
	@Override
	public void onBackPressed() {
		if (createPoly) {
			createPolyPlot();
			return;
		}
		
		garden.remove(oldPlot);
		plot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_default));
		Intent intent = new Intent().putExtra("zoom_scale", editView.zoomScale);
		float[] values = new float[9], bgvalues = new float[9];
		editView.dragMatrix.getValues(values);
		editView.bgDragMatrix.getValues(bgvalues);
		intent.putExtra("drag_matrix", values);
		intent.putExtra("bgdrag_matrix", bgvalues);
		setResult(RESULT_OK, intent);
		// REPLACE THIS WITH GardenGnome.updatePlot( whatever )
		/*String shape_s = plot.getBounds().left + "," + plot.getBounds().top + "," + plot.getBounds().right + "," + plot.getBounds().bottom + "," + plot.getPaint().getColor();
		float[] polyPoints_f = plot.getPoints();
		String polyPoints_s = "";
		if(plot.getPoints().length >= 2) {
			for(int i = 0; i < polyPoints_f.length; i++)
				polyPoints_s = polyPoints_s + polyPoints_f[i] + ",";
			polyPoints_s.substring(0, polyPoints_s.length() - 2);
		}
		
		List<Integer> temp = StartScreen.dh.select_map_gp_po(extras.getInt("garden_id") + 1);
		int po_pk = -1;
		for(int i = 0; i < temp.size(); i++) {
			if(po_pk != -1) 
				break;
			if(plot.getName().equalsIgnoreCase(StartScreen.dh.select_plot_name(temp.get(i).intValue())))
				po_pk = temp.get(i);
		}				
		
		StartScreen.dh.update_plot(po_pk, shape_s, plot.getColor(), polyPoints_s, plot.getAngle());*/
		
		finish();
		overridePendingTransition(0, 0);
	}
	
	public void createPolyPlot() {
		float[] pts = toFloatArray(editView.polyPts);
		plot.set(new Plot(plot.getName(), pts));
		plot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_edit));
		oldPlot.set(plot);
		((Button)findViewById(R.id.save_btn)).setText(R.string.btn_save_edit);
		if (hintsOn) {
			TextView hint = (TextView) findViewById(R.id.edit_hint);
			hint.setText(R.string.hint_editscreen);
		}
		String polyPoints_s = "";
		for(int i = 0; i < pts.length; i++)
			polyPoints_s += pts[i];

		createPoly = false;
		editView.invalidate();
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
		case R.id.m_changecolor:
			int color = getPreferences(MODE_PRIVATE).getInt("color", Color.WHITE);
			new ColorPickerDialog(this, this, color).show();
			break;
		case R.id.m_revert:
			plot.set(oldPlot);
			plot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_edit));
			editView.invalidate();
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.save_btn:
			onBackPressed();
			break;
		case R.id.edit_zoomfit_btn:
			editView.zoomScale = 1;
			garden.refreshBounds(garden.size() - (createPoly ? 2 : 1));
			String bounds_s = "" + garden.getBounds().left + "," + garden.getBounds().top + "," + garden.getBounds().right + "," + garden.getBounds().bottom;
			//GardenGnome.updateGarden(extras.getInt("garden_id") + 1, bounds_s);
			editView.reset();
			break;
		}
	}
	
	View.OnClickListener zoomIn = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			handleZoom();
			if (zoomPressed == 0) {
				zoomPressed = 1;
				float zoomScalar = getResources().getDimension(R.dimen.zoom_scalar);
				ScaleAnimation anim = new ScaleAnimation(1, zoomScalar, 1, zoomScalar, editView.getWidth()/2f, editView.getHeight()/2f);
				anim.setDuration(getResources().getInteger(R.integer.zoom_duration));
				editView.startAnimation(anim);
			}
		}
	};
	
	View.OnClickListener zoomOut = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			handleZoom();
			if (zoomPressed == 0) {
				zoomPressed = -1;
				float zoomScalar = 1/getResources().getDimension(R.dimen.zoom_scalar);
				ScaleAnimation anim = new ScaleAnimation(1, zoomScalar, 1, zoomScalar, editView.getWidth()/2f, editView.getHeight()/2f); 
				anim.setDuration(getResources().getInteger(R.integer.zoom_duration));
				editView.startAnimation(anim);
			}
		}
	};
	
	public void handleZoom() {
		zoomControls.removeCallbacks(autoHide);
		if (!zoomControls.isShown())
			zoomControls.show();
		zoomControls.postDelayed(autoHide, getResources().getInteger(R.integer.hidezoom_delay));
	}
	
	Runnable autoHide = new Runnable() {
		@Override
		public void run() {
			if (zoomControls.isShown()) {
				zoomControls.removeCallbacks(this);
				zoomControls.hide();
			}
		}
	};

	@Override
	public void colorChanged(int color) {
		getPreferences(MODE_PRIVATE).edit().putInt("color", color).commit();
		plot.getPaint().setColor(color);
		editView.invalidate();
	}
	
	/** handles button transparency */
	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		if (hasFocus)
			view.getBackground().setAlpha(0xff);
		else
			view.getBackground().setAlpha(getResources().getInteger(R.integer.btn_trans));
	}
	
	/** handles button transparency */
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			view.getBackground().setAlpha(0xff);
		else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (!view.isPressed()) {
				view.getBackground().setAlpha(getResources().getInteger(R.integer.btn_trans));
				view.invalidate();
			}
		}
		else {
			view.getBackground().setAlpha(getResources().getInteger(R.integer.btn_trans));
			view.invalidate();
		}
		return false;
	}
	
	public static float[] toFloatArray(ArrayList<Float> list) {
		float[] pts = new float[list.size()];
		for (int i = 0; i < pts.length; i++)
			pts[i] = list.get(i);
		return pts;
	}
}
