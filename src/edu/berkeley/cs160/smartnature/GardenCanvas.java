package edu.berkeley.cs160.smartnature;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ZoomButtonsController;

import java.util.ArrayList;

public class GardenCanvas extends RelativeLayout implements OnTouchListener {
	//Bitmap bmp;
	Canvas offscreen;
	Paint canvasPaint;
	Paint textPaint;
	ArrayList<Plot> plots;
	ShapeDrawable d;
	Context c;
	
	public GardenCanvas(Context context, ArrayList<Plot> gardenRegions, int width, int height) {
		super(context);
		c = context;
		canvasPaint = new Paint(Color.BLACK);
		canvasPaint.setStyle(Paint.Style.STROKE);
		textPaint = new Paint(Color.BLACK);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(16);
		
		plots = gardenRegions;
		for (Plot r: plots) {
			r.getShape().getPaint().setColor(Color.BLACK);
			r.getShape().getPaint().setStyle(Paint.Style.STROKE);
			r.getShape().getPaint().setStrokeWidth(3);
		}
		MyCanvas my = new MyCanvas(c);
		addView(my, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		Button b = new Button(c);
		b.setText("AaSSSAG");
		Button b2 = new Button(c);
		b2.setText("AaSSSAGrhrethteh");
		//addView(b);
		//b.setLayoutParams(
		ZoomButtonsController z = new ZoomButtonsController(my);
		addView(z.getContainer());
		z.setVisible(true);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		z.getContainer().setLayoutParams(params);
		//b.setGravity(Gravity.BOTTOM);
		//addView(b2);
	}

	class MyCanvas extends View {
	public MyCanvas(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawRGB(255, 255, 255);
		
		for (Plot p: plots) {
			p.getShape().draw(canvas);
			p.getShape().getBounds();
			canvas.drawText(p.getName(), p.getShape().getBounds().left,  p.getShape().getBounds().top - 10, textPaint);
		}
		canvas.drawRect(new Rect(70, 250, 120, 300), canvasPaint);
		//canvas.drawBitmap(bmp, 0, 0, canvasPaint);
	}}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//p.set(event.getX(), event.getY());
		//paint.setColor(eraseMode ? Color.WHITE : Color.argb(brushA, brushR, brushG, brushB));
		//offscreen.drawCircle(p.x, p.y, (float)brushSize, paint);
		//touched = true;
		//invalidate();
		return true;
	}
}