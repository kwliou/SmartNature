package edu.berkeley.cs160.smartnature;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
	Context context;
	
	public GardenLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		plots = ((GardenScreen) context).plots;
		canvasPaint = new Paint(Color.BLACK);
		canvasPaint.setStyle(Paint.Style.STROKE);
		textPaint = new Paint(Color.BLACK);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(16);
		//setOnTouchListener(this);
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
			canvas.drawRGB(255, 255, 255);
			
			for (Plot p: plots) {
				p.getShape().draw(canvas);
				Rect bounds = p.getShape().getBounds();
				canvas.drawText(p.getName(), bounds.left,  bounds.top - 10, textPaint);
			}
			canvas.drawRect(new Rect(70, 250, 120, 300), canvasPaint);
		}
	}

}