package edu.berkeley.cs160.smartnature;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
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
			Paint p = plot.getShape().getPaint();
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
			Rect shapeBounds = p.getShape().getBounds();
			canvas.rotate(p.getAngle(), shapeBounds.centerX(), shapeBounds.centerY());
			p.getShape().draw(canvas);
			canvas.restore();
		}
		canvas.restore();

		if (context.showLabels)
			for (Plot p: garden.getPlots()) {
				Rect bounds = p.getShape().getBounds();
				float[] labelLoc = portraitMode ? new float[] {bounds.left - 10, bounds.centerY()} : new float[] {bounds.centerX(), bounds.top - 10};
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
	}

	@Override
	public void onClick(View view) {
		if (focusedPlot != null){
			Intent intent = new Intent(context, PlotScreen.class);
			Bundle bundle = new Bundle(3);
			bundle.putString("name", focusedPlot.getName());
			bundle.putInt("gardenID", context.gardenID);
			bundle.putInt("plotID", focusedPlot.getID());
	
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
			bundle.putInt("id", StartScreen.gardens.indexOf(garden));
			bundle.putInt("plot_name", garden.getPlotId(focusedPlot));
			intent.putExtras(bundle);
			focusedPlot.getShape().getPaint().setColor(tempColor);
			focusedPlot.getShape().getPaint().setStrokeWidth(7);
			context.startActivity(intent);
			return true;
		}
		return false;
	}
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		super.onTouchEvent(event);
		context.handleZoom();
		x = event.getX(); y = event.getY();
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			dragMode = false;
			downX = x; downY = y;
			focusedPlot = garden.plotAt(x, y, m);
			if (focusedPlot != null) {
				// set focused plot appearance
				tempColor = focusedPlot.getShape().getPaint().getColor();
				focusedPlot.getShape().getPaint().setColor(0xFF7BB518);
				focusedPlot.getShape().getPaint().setStrokeWidth(5);
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
					focusedPlot.getShape().getPaint().setColor(tempColor);
					focusedPlot.getShape().getPaint().setStrokeWidth(3);
					focusedPlot = null; 
				}
		}

		// onClick for some reason doesn't execute on its own so manually do it
		if (event.getAction() == MotionEvent.ACTION_UP && !dragMode) {
			if (focusedPlot != null) {
				// reset clicked plot appearance
				focusedPlot.getShape().getPaint().setColor(tempColor);
				focusedPlot.getShape().getPaint().setStrokeWidth(3);
			}
			//performClick();

			if (event.getAction() == MotionEvent.ACTION_UP && !dragMode && focusedPlot != null) {
				// reset clicked plot appearance
				focusedPlot.getShape().getPaint().setColor(Color.BLACK);
				focusedPlot.getShape().getPaint().setStrokeWidth(3);

			}
		}
		prevX = x;
		prevY = y;
		invalidate();

		return true;
	}
}