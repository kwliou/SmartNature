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
import android.widget.FrameLayout;

import java.util.ArrayList;

public class GardenLayout extends FrameLayout implements View.OnTouchListener, View.OnClickListener {
	Paint canvasPaint;
	Paint textPaint;
	ArrayList<Plot> plots;
	Drawable bg;
	GardenScreen context;
	
	public GardenLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = (GardenScreen) context;
		bg = getResources().getDrawable(R.drawable.tile);	
		initPaint();	
		plots = this.context.plots;
		for (Plot r: plots) {
			r.getShape().getPaint().setColor(Color.BLACK);
			r.getShape().getPaint().setStyle(Paint.Style.STROKE);
			r.getShape().getPaint().setStrokeWidth(3);
		}
		addView(new GardenView(context, null), new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setOnClickListener(this);
	}
	
	public void initPaint() {
		canvasPaint = new Paint();
		canvasPaint.setStyle(Paint.Style.STROKE);
		textPaint = new Paint(Paint.FAKE_BOLD_TEXT_FLAG|Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextSize(15.5f);
		textPaint.setTextScaleX(1.2f);
		textPaint.setTextAlign(Paint.Align.CENTER);
	}
	
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		//invalidate();
		return true;
	}
	
	@Override
	public void onClick(View view) {
		
	}
	
	class GardenView extends View {
		
		public GardenView(Context context, AttributeSet attrs) { super(context, attrs); }

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			int width = getWidth(), height = getHeight();
			boolean portraitMode = width < height;
			bg.setBounds(canvas.getClipBounds());
			bg.draw(canvas); //canvas.drawRGB(255, 255, 255);
			
			Matrix m = new Matrix();
			if (context.zoomLevel != 0) {
				float zoomScale = (float) Math.pow(1.5, context.zoomLevel);
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
			
			float[] labelLoc = { 0, 0 };
			if (context.showLabels)
				for (Plot p: plots) {
					Rect bounds = p.getShape().getBounds();
					if (portraitMode)
						m.mapPoints(labelLoc, new float[] { bounds.left - 10, bounds.centerY() });
					else
						m.mapPoints(labelLoc, new float[] { bounds.centerX(), bounds.top - 10 });
					textPaint.setTextSize(15 * (float) Math.pow(1.5, context.zoomLevel));
					canvas.drawText(p.getName().toUpperCase(), labelLoc[0], labelLoc[1], textPaint);
				}
		}
	}
}
