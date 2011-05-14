package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import android.widget.ZoomControls;

public class GardenScreen extends Activity implements View.OnClickListener, View.OnFocusChangeListener, View.OnTouchListener {
	
	/** request codes used in intents */
	final static int EDIT_GARDEN = 1, SHARE_GARDEN = 2, USE_CAMERA = 3, ADD_PLOT = 4, EDIT_PLOT = 5, VIEW_PLOT = 6, VIEW_PHOTOS=7;
	
	Garden garden;
	int gardenIndex;
	GardenView gardenView;
	View textEntryView;
	AlertDialog dialog;
	ZoomControls zoomControls;
	
	/** User-related options */
	boolean hintsOn, showLabels = true, showFullScreen, zoomAutoHidden;
	/** describes what zoom button was pressed: 1 for +, -1 for -, and 0 by default */
	int zoomPressed;
	/** URI of photo from camera app */
	Uri imageUri;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		loadPreferences();
		if (showFullScreen)
			setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);
		super.onCreate(savedInstanceState);
		if (!getIntent().hasExtra("garden_index")) {
			finish();
			return;
		}
		GardenGnome.init(this);
		gardenIndex = getIntent().getIntExtra("garden_index", 0);
		garden = GardenGnome.getGarden(gardenIndex);
		setTitle(garden.getName());
		if (savedInstanceState == null) // first init
			garden.refreshBounds();
		setContentView(R.layout.garden);
		gardenView = (GardenView) findViewById(R.id.garden_view);
		
		Drawable footer = findViewById(R.id.garden_footer).getBackground().mutate();
		footer.setAlpha(getResources().getInteger(R.integer.bar_trans));
		initButton(R.id.addplot_btn);
		initButton(R.id.zoomfit_btn);
		if (hintsOn) {
			((TextView)findViewById(R.id.garden_hint)).setText(R.string.hint_gardenscreen);
			((TextView)findViewById(R.id.garden_hint)).setVisibility(View.VISIBLE);
		}
		zoomControls = (ZoomControls) findViewById(R.id.zoom_controls);
		if (zoomAutoHidden)
			zoomControls.setVisibility(View.GONE);
		zoomControls.setOnZoomInClickListener(zoomIn);
		zoomControls.setOnZoomOutClickListener(zoomOut);
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
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable("key", imageUri);
		savedInstanceState.putFloat("zoom_scale", gardenView.zoomScale);
		savedInstanceState.putBoolean("portrait_mode", gardenView.portraitMode);
		float[] values = new float[9], bgvalues = new float[9];
		gardenView.dragMatrix.getValues(values);
		gardenView.bgDragMatrix.getValues(bgvalues);
		savedInstanceState.putFloatArray("drag_matrix", values);
		savedInstanceState.putFloatArray("bgdrag_matrix", bgvalues);
		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		imageUri = (Uri) savedInstanceState.getParcelable("key");
		gardenView.zoomScale = savedInstanceState.getFloat("zoom_scale");
		boolean prevPortraitMode = savedInstanceState.getBoolean("portrait_mode");
		boolean portraitMode = getWindowManager().getDefaultDisplay().getWidth() < getWindowManager().getDefaultDisplay().getHeight();
		
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
		
		gardenView.dragMatrix.setValues(values);
		gardenView.bgDragMatrix.setValues(bgvalues);
		gardenView.onAnimationEnd();	
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.addplot_btn:
				startActivityForResult(new Intent(this, AddPlot.class), ADD_PLOT);
				break;
			case R.id.zoomfit_btn:
				gardenView.zoomScale = 1;
				garden.refreshBounds();
				gardenView.reset();
				break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case EDIT_GARDEN: // returning from GardenAttr activity
				setTitle(garden.getName());
				break;
			case USE_CAMERA: // returning from Camera activity
				if (resultCode == RESULT_OK) {
					System.out.print("imageUri=" + imageUri.toString() + " => " );
					System.out.println(Helper.resolveUri(this, imageUri));
					
					if (data != null && data.getData() != null)
						imageUri = data.getData();
					System.out.print("imageUri=" + imageUri.toString() + " => " );
					System.out.println(Helper.resolveUri(this, imageUri));
					
					GardenGnome.addPhoto(garden, new Photo(imageUri));	
				}
				break;
			case ADD_PLOT: // returning from AddPlot activity
				if (data != null) {
					data.putExtra("garden_index", gardenIndex);
					data.putExtra("zoom_scale", gardenView.zoomScale);
					float[] values = new float[9], bgvalues = new float[9];
					gardenView.dragMatrix.getValues(values);
					gardenView.bgDragMatrix.getValues(bgvalues);
					data.putExtra("drag_matrix", values);
					data.putExtra("bgdrag_matrix", bgvalues);
					startActivityForResult(data, EDIT_PLOT);
					overridePendingTransition(0, 0);
				}
				break;
			case EDIT_PLOT: // returning from EditScreen activity
				gardenView.zoomScale = data.getFloatExtra("zoom_scale", 1);
				gardenView.dragMatrix.setValues(data.getFloatArrayExtra("drag_matrix"));
				gardenView.bgDragMatrix.setValues(data.getFloatArrayExtra("bgdrag_matrix"));
				gardenView.onAnimationEnd();
				if (zoomAutoHidden)
					zoomControls.setVisibility(View.GONE); // need to manually hide
				break;
			case VIEW_PLOT: // returning from PlotScreen activity
				if (zoomAutoHidden)
					zoomControls.hide(); // need to manually hide
				break;
		}
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		DialogInterface.OnClickListener confirm = new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int whichButton) {
				GardenGnome.removeGarden(gardenIndex);
				finish();
			}
		};
		
		AlertDialog dialog = new AlertDialog.Builder(this)
			.setTitle("Confirm deletion")
			.setPositiveButton(R.string.alert_dialog_delete, confirm)
			.setNegativeButton(R.string.alert_dialog_cancel, null) // this means cancel was pressed
			.create();
		
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
		Intent intent;
		
		switch (item.getItemId()) {
			case R.id.m_gardenoptions:
				intent = new Intent(this, GardenAttr.class).putExtra("garden_index", gardenIndex);
				startActivityForResult(intent, EDIT_GARDEN);
				break;
			case R.id.m_sharegarden:
				intent = new Intent(this, ShareGarden.class).putExtra("garden_index", gardenIndex);
				startActivityForResult(intent, SHARE_GARDEN);
				break;
			case R.id.m_gallery:
				intent = new Intent(this, GardenGallery.class).putExtra("garden_index", gardenIndex);
				startActivityForResult(intent, VIEW_PHOTOS);
				break;
			case R.id.m_takephoto:
				intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				String fileName = "GardenGnome_" + garden.getName() + garden.numPhotos() + ".jpg";
				
				// HTC camera app ignores EXTRA_OUTPUT
				ContentValues values = new ContentValues();
				values.put(Images.Media.TITLE, fileName);
				//values.put(Images.Media.DESCRIPTION, "Image capture by camera");
				imageUri = getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // some devices take low-quality pics without this
				startActivityForResult(intent, USE_CAMERA);
				break;
			case R.id.m_deletegarden:
				showDialog(0);
				break;
			case R.id.m_home:
				if (getCallingActivity().getShortClassName().equals(".StartScreen"))
					finish();
				else {
					intent = new Intent(this, StartScreen.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	View.OnClickListener zoomIn = new View.OnClickListener() {
		@Override
		public void onClick(final View view) {
			handleZoom();
			if (zoomPressed == 0) {
				zoomPressed = 1;
				float zoomScalar = getResources().getDimension(R.dimen.zoom_scalar);
				ScaleAnimation anim = new ScaleAnimation(1, zoomScalar, 1, zoomScalar, gardenView.getWidth()/2f, gardenView.getHeight()/2f);
				anim.setDuration(getResources().getInteger(R.integer.zoom_duration));
				gardenView.startAnimation(anim);
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
				ScaleAnimation anim = new ScaleAnimation(1, zoomScalar, 1, zoomScalar, gardenView.getWidth()/2f, gardenView.getHeight()/2f); 
				anim.setDuration(getResources().getInteger(R.integer.zoom_duration));
				gardenView.startAnimation(anim);
			}
		}
	};
	
	/** handles auto-hidden zoom controls */
	public void handleZoom() {
		if (zoomAutoHidden) {
			zoomControls.removeCallbacks(autoHide);
			if (!zoomControls.isShown())
				zoomControls.show();
			zoomControls.postDelayed(autoHide, getResources().getInteger(R.integer.hidezoom_delay));
		}
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
		else
			view.getBackground().setAlpha(getResources().getInteger(R.integer.btn_trans));
		return false;
	}
	
}