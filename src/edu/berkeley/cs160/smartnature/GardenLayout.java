package edu.berkeley.cs160.smartnature;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class GardenLayout extends View implements View.OnClickListener {
	GardenScreen context;
	Paint canvasPaint, textPaint;
	ArrayList<Plot> plots;
	Drawable bg;
	Matrix dragMatrix = new Matrix(), bgDragMatrix = new Matrix();
	int zoomLevel;
	float zoomScale = 1;
	
	public GardenLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
		this.context = (GardenScreen) context;
		bg = getResources().getDrawable(R.drawable.tile);	
		initPaint();	
		plots = this.context.plots;
		for (Plot r: plots) {
			r.getShape().getPaint().setColor(Color.BLACK);
			r.getShape().getPaint().setStyle(Paint.Style.STROKE);
			r.getShape().getPaint().setStrokeWidth(3);
		}
		setOnClickListener(this);
	}
	
	@Override
	public void onAnimationEnd() {
		zoomLevel = context.zoomLevel;
		zoomScale = (float) Math.pow(1.5, zoomLevel);
		textPaint.setTextSize(Math.max(10, 15 * zoomScale));
		if (zoomLevel >= 0)
			textPaint.setTextScaleX(1.2f + 0.05f * zoomLevel); // optional text appearance
		invalidate();
	}
	
	public void initPaint() {
		canvasPaint = new Paint();
		canvasPaint.setStyle(Paint.Style.STROKE);
		textPaint = new Paint(Paint.FAKE_BOLD_TEXT_FLAG|Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextSize(15.5f);
		textPaint.setTextScaleX(1.2f);
		textPaint.setTextAlign(Paint.Align.CENTER);
	}
	
	public void reset() {
		zoomLevel = 0;
		zoomScale = 1;
		textPaint.setTextSize(15.5f);
		textPaint.setTextScaleX(1.2f);
		dragMatrix.reset();
		bgDragMatrix.reset();
		invalidate();
	}
	
	float prevX, prevY;
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		context.handleZoom();
		float x = event.getX(), y = event.getY();
		if (event.getAction() != MotionEvent.ACTION_DOWN) {
			float dx = x - prevX, dy = y - prevY;
			dragMatrix.postTranslate(dx / zoomScale, dy / zoomScale);
			bgDragMatrix.postTranslate(dx, dy);
		}
		prevX = x;
		prevY = y;
		invalidate();
		return true;
	}
	
	@Override
	public void onClick(View view) {
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width = getWidth(), height = getHeight();
		boolean portraitMode = width < height;
		Matrix m = new Matrix();
		
		canvas.save();
		canvas.concat(bgDragMatrix);
		bg.setBounds(canvas.getClipBounds());
		bg.draw(canvas); //canvas.drawRGB(255, 255, 255);
		canvas.restore();
		
		m.postConcat(dragMatrix);
		if (zoomLevel != 0) {
			float zoomShift = (1 - zoomScale) / 2;
			m.postScale(zoomScale, zoomScale);
			m.postTranslate(zoomShift * width, zoomShift * height);
		}
		if (portraitMode) {
			m.preTranslate(width, 0);
			m.preRotate(90);
		}
		
		canvas.save();
		canvas.concat(m);
		for (Plot p: plots)
			p.getShape().draw(canvas);
		canvas.restore();
		
		if (context.showLabels)
			for (Plot p: plots) {
				Rect bounds = p.getShape().getBounds();
				float[] labelLoc = portraitMode ? new float[] {bounds.left - 10, bounds.centerY()} : new float[] {bounds.centerX(), bounds.top - 10};
				m.mapPoints(labelLoc);
				canvas.drawText(p.getName().toUpperCase(), labelLoc[0], labelLoc[1], textPaint);
			}
		}
	}
