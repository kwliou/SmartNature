package edu.berkeley.cs160.smartnature;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GardenView extends View implements View.OnClickListener, View.OnTouchListener, View.OnLongClickListener{

	GardenScreen context;
	Garden garden;
	/** plot that is currently pressed */
	Plot focusedPlot;
	/** the entire transformation matrix applied to the canvas */
	Matrix m = new Matrix();
	/** translation matrix applied to the canvas */
	Matrix dragMatrix = new Matrix();
	/** translation matrix applied to the background */
	Matrix bgDragMatrix = new Matrix();
	Drawable bg;
	Paint textPaint;
	int zoomLevel;
	float prevX, prevY, downX, downY, x, y, zoomScale = 1;
	float textSize;
	boolean portraitMode, dragMode;
	int tempColor;

	public GardenView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = (GardenScreen) context;
		textSize = 15.5f * getResources().getDisplayMetrics().scaledDensity;
		initPaint();
		bg = getResources().getDrawable(R.drawable.tile);	
		initPaint();
		initMockData();	
		setOnClickListener(this);
		setOnTouchListener(this);
		setOnLongClickListener(this);
	}

	public void initMockData() {
		garden = this.context.mockGarden;
		for (Plot plot : garden.getPlots()) {
			Paint p = plot.getPaint();
			p.setStyle(Paint.Style.STROKE);
			p.setStrokeWidth(3);
			p.setStrokeCap(Paint.Cap.ROUND);
			p.setStrokeJoin(Paint.Join.ROUND);
		}		
	}

	public void initPaint() {
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FAKE_BOLD_TEXT_FLAG|Paint.DEV_KERN_TEXT_FLAG);
		textPaint.setTextSize(textSize);
		textPaint.setTextScaleX(1.2f);
		textPaint.setTextAlign(Paint.Align.CENTER);
	}

	/** called when user clicks "zoom to fit" */
	public void reset() {
		zoomLevel = 0;
		zoomScale = 1;
		textPaint.setTextSize(textSize);
		textPaint.setTextScaleX(1.2f);
		dragMatrix.reset();
		bgDragMatrix.reset();
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width = getWidth(), height = getHeight();
		portraitMode = width < height;
		
		canvas.save();
		canvas.concat(bgDragMatrix);
		bg.setBounds(canvas.getClipBounds());
		bg.draw(canvas); //canvas.drawRGB(255, 255, 255);
		canvas.restore();
		
		m.reset();
		RectF gardenBounds = context.showFullScreen ? garden.getBounds() : garden.getBounds(portraitMode);
		m.setRectToRect(gardenBounds, getBounds(), Matrix.ScaleToFit.CENTER);
		if (portraitMode) {
			m.postRotate(90);
			m.postTranslate(width, 0);
		}
		m.postConcat(dragMatrix);
		
		if (zoomLevel != 0) {
			float zoomShift = (1 - zoomScale) / 2;
			m.postScale(zoomScale, zoomScale);
			m.postTranslate(zoomShift * width, zoomShift * height);
		}
		
		canvas.save();
		canvas.concat(m);
		for (Plot p: garden.getPlots()) {
			canvas.save();
			Rect bounds = p.getBounds();
			canvas.rotate(p.getAngle(), bounds.centerX(), bounds.centerY());
			p.getShape().draw(canvas);
			canvas.restore();
		}
		canvas.restore();
		
		if (context.showLabels)
			for (Plot p: garden.getPlots()) {
				RectF rbounds = p.getRotateBounds(); 
				float[] labelLoc;
				if (portraitMode)
					labelLoc = new float[] { rbounds.left - 10, rbounds.centerY() };
				else
					labelLoc = new float[] { rbounds.centerX(), rbounds.top - 10 };
				m.mapPoints(labelLoc);
				canvas.drawText(p.getName().toUpperCase(), labelLoc[0], labelLoc[1], textPaint);
			}
	}

	public RectF getBounds() {
		if (portraitMode)
			return new RectF(getLeft(), getTop(), getBottom(), getRight());
		else
			return new RectF(getLeft(), getTop(), getRight(), getBottom());
	}

	@Override
	public void onAnimationEnd() {
		zoomLevel = context.zoomLevel;
		zoomScale = (float) Math.pow(1.5, zoomLevel);
		textPaint.setTextSize(Math.max(10, textSize * zoomScale));
		invalidate();
		context.zoomPressed = false;
	}

	@Override
	public void onClick(View view) {
		if (focusedPlot != null){
			Intent intent = new Intent(context, PlotScreen.class);
			Bundle bundle = new Bundle(3);
			bundle.putString("name", focusedPlot.getName());
			bundle.putInt("garden_id", context.gardenID);
			bundle.putInt("plot_id", garden.getPlots().indexOf(focusedPlot));
	
			intent.putExtras(bundle);      	
			context.startActivity(intent);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if (focusedPlot != null) {
			Intent intent = new Intent(context, EditScreen.class);
			Bundle bundle = new Bundle();
			bundle.putString("name", focusedPlot.getName());
			bundle.putInt("garden_id", StartScreen.gardens.indexOf(garden));
			bundle.putInt("plot_id", garden.getPlotId(focusedPlot));
			bundle.putInt("zoom_level", zoomLevel);
			float[] values = new float[9], bgvalues = new float[9];
			dragMatrix.getValues(values);
			bgDragMatrix.getValues(bgvalues);
			bundle.putFloatArray("drag_matrix", values);
			bundle.putFloatArray("bgdrag_matrix", bgvalues);
			intent.putExtras(bundle);
			focusedPlot.getPaint().setColor(tempColor);
			focusedPlot.getPaint().setStrokeWidth(7);
			//context.getWindow().setWindowAnimations(0);
			context.startActivityForResult(intent, 0);
			context.overridePendingTransition(0, 0);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		onTouchEvent(event);
		context.handleZoom();
		x = event.getX(); y = event.getY();
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			dragMode = false;
			downX = x; downY = y;
			focusedPlot = garden.plotAt(x, y, m);
			if (focusedPlot != null) {
				// set focused plot appearance
				tempColor = focusedPlot.getPaint().getColor();
				focusedPlot.getPaint().setColor(getResources().getColor(R.color.focused_plot));
				focusedPlot.getPaint().setStrokeWidth(5);
			}
		}
		else {
			float dx = x - prevX, dy = y - prevY;
			dragMatrix.postTranslate(dx / zoomScale, dy / zoomScale);
			bgDragMatrix.postTranslate(dx, dy);
			if (!dragMode)
				dragMode = Math.abs(downX - x) > 5 || Math.abs(downY - y) > 5; // show some leniency
				if (dragMode && focusedPlot != null) {
					// plot can no longer be clicked so reset appearance
					focusedPlot.getPaint().setColor(tempColor);
					focusedPlot.getPaint().setStrokeWidth(3);
					focusedPlot = null; 
				}
		}

		if (event.getAction() == MotionEvent.ACTION_UP && !dragMode && focusedPlot != null) {
			// reset clicked plot appearance
			focusedPlot.getPaint().setColor(tempColor);
			focusedPlot.getPaint().setStrokeWidth(3);
		}
		
		prevX = x;
		prevY = y;
		invalidate();

		return true;
	}
	
}