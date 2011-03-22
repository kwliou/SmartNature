package edu.berkeley.cs160.smartnature;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;

import java.util.ArrayList;

public class GardenLayout extends FrameLayout implements OnTouchListener {
	Paint canvasPaint;
	Paint textPaint;
	ArrayList<Plot> plots;
	Drawable bg;
	
	Context context;
	
	public GardenLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		bg = getResources().getDrawable(R.drawable.tile);
		canvasPaint = new Paint(Color.BLACK);
		canvasPaint.setStyle(Paint.Style.STROKE);
		textPaint = new Paint(Color.BLACK);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(15);
		textPaint.setTextScaleX(1.1f);
		textPaint.setTextAlign(Paint.Align.CENTER);
		plots = ((GardenScreen) context).plots;
		for (Plot r: plots) {
			r.getShape().getPaint().setColor(Color.BLACK);
			r.getShape().getPaint().setStyle(Paint.Style.STROKE);
			r.getShape().getPaint().setStrokeWidth(3);
		}
		addView(new GardenView(context, null), new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//invalidate();
		return true;
	}
	
	class GardenView extends View {
		
		public GardenView(Context context, AttributeSet attrs) { super(context, attrs); }

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			int width = getWidth(), height = getHeight();
			boolean portrait = width < height;
			//canvas.drawRGB(255, 255, 255);
			bg.setBounds(canvas.getClipBounds());
			bg.draw(canvas);
			
			for (Plot p: plots) {
				Rect bounds = p.getShape().getBounds();
				float[] labelCenter = { bounds.centerX(), bounds.top - 10 };
				canvas.save();
				if (portrait) {
					canvas.translate(width, 0);
					canvas.rotate(90);
					canvas.getMatrix().mapPoints(labelCenter, new float[] { bounds.left - 10, bounds.centerY() });
					labelCenter[1] -= GardenScreen.realHeight - height; // why should I need this?
				}
				p.getShape().draw(canvas);
				/*
				Path path = new Path();
				path.lineTo(0, -bounds.height());
				path.close();
				canvas.drawTextOnPath(p.getName().toUpperCase(), path, bounds.left - 10, bounds.bottom, textPaint);
				*/
				canvas.restore();
				canvas.drawText(p.getName().toUpperCase(), labelCenter[0], labelCenter[1], textPaint);
			}
		}
	}

}
