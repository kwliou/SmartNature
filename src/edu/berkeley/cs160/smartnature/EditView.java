package edu.berkeley.cs160.smartnature;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class EditView extends View implements View.OnClickListener, View.OnTouchListener {

	EditScreen context;
	Garden garden;
	/** plot that is currently pressed */
	Plot focusedPlot;
	/** the entire transformation matrix applied to the canvas */
	static Matrix m = new Matrix();
	/** translation matrix applied to the canvas */
	Matrix dragMatrix = new Matrix();
	/** translation matrix applied to the background */
	Matrix bgDragMatrix = new Matrix();
	Drawable bg;
	Path arrow;
	Paint textPaint, rayPaint;
	int zoomLevel;
	float prevX, prevY, x, y, zoomScale = 1;
	float textSize;
	boolean portraitMode;
	int plotColor;
	
	private int status;
	private final static int DRAG_NONE = 0;
	private final static int DRAG_SHAPE = 1;
	private final static int DRAG_SCREEN = 2;
	
	public EditView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = (EditScreen) context;
		bg = getResources().getDrawable(R.drawable.tile);	
		textSize = 15.5f * getResources().getDisplayMetrics().scaledDensity;
		initPaint();
		initMockData();	
		setOnClickListener(this);
		setOnTouchListener(this);
	}
	
	public void initMockData() {
		garden = this.context.mockGarden;
		for (Plot plot : garden.getPlots()) {
			Paint p = plot.getShape().getPaint();
			p.setStyle(Paint.Style.STROKE);
			p.setStrokeCap(Paint.Cap.ROUND);
			p.setStrokeJoin(Paint.Join.ROUND);
		}		
	}
	
	public void initPaint() {
		arrow = new Path();
		arrow.rLineTo(2, 7);
		arrow.rLineTo(-2, -3);
		arrow.rLineTo(-2, 3);
		arrow.close();
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FAKE_BOLD_TEXT_FLAG|Paint.DEV_KERN_TEXT_FLAG);
		textPaint.setTextSize(textSize);
		textPaint.setTextScaleX(1.2f);
		textPaint.setTextAlign(Paint.Align.CENTER);
		rayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		rayPaint.setStyle(Paint.Style.STROKE);
		rayPaint.setStrokeWidth(5);
		rayPaint.setStrokeMiter(30);
		//rayPaint.setStrokeCap(Paint.Cap.SQUARE);
		//rayPaint.setStrokeJoin(Paint.Join.ROUND);
	}
	
	/** called when user clicks "zoom to fit" */
	public void reset() {
		zoomLevel = 0;
		zoomScale = 1;
		textPaint.setTextSize(textSize);
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
			if (p != context.newPlot) {
				canvas.save();
				Rect shapeBounds = p.getShape().getBounds();
				canvas.rotate(p.getAngle(), shapeBounds.centerX(), shapeBounds.centerY());
				p.getShape().draw(canvas);
				canvas.restore();
			}
		}

		// "shade" over everything
		canvas.restore();
		canvas.drawARGB(80, 0, 0, 0);

		// draw plot being edited
		canvas.save();
		canvas.concat(m);
		Rect shapeBounds = context.newPlot.getShape().getBounds();
		canvas.rotate(context.newPlot.getAngle(), shapeBounds.centerX(), shapeBounds.centerY());
		Paint paint = context.newPlot.getShape().getPaint();
		
		int oldColor = paint.getColor();
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.FILL);
		context.newPlot.getShape().draw(canvas);
		paint.setColor(oldColor);
		paint.setStyle(Paint.Style.STROKE);
		context.newPlot.getShape().draw(canvas);
		if (context.rotateMode) {
			canvas.drawLine(shapeBounds.centerX(), shapeBounds.centerY(), shapeBounds.centerX(), shapeBounds.top - 50, rayPaint);
			Path path = new Path(arrow);
			path.offset(shapeBounds.centerX(), shapeBounds.top - 50);
			canvas.drawPath(path, rayPaint);
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
	}
	
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		onTouchEvent(event);
		context.handleZoom();
		x = event.getX(); y = event.getY();
		if (context.rotateMode)
			handleRotation(event);
		else
			handleDragging(event);
		
		prevX = x;
		prevY = y;
		invalidate();
		return true;
	}
	
	public void handleDragging(MotionEvent event) {
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Matrix inv = new Matrix();
			m.invert(inv);
			float[] xy = { x, y };
			inv.mapPoints(xy);
			if (context.newPlot.contains(xy[0], xy[1])) {
				focusedPlot = context.newPlot;
				// set focused plot appearance
				plotColor = focusedPlot.getShape().getPaint().getColor();
				focusedPlot.getShape().getPaint().setStrokeWidth(9);
				focusedPlot.getShape().getPaint().setColor(getResources().getColor(R.color.focused_plot));
				status = DRAG_SHAPE;
			} else
				status = DRAG_SCREEN;
			break;
		
		case MotionEvent.ACTION_MOVE:
			if(status == DRAG_SHAPE) {
				float[] dxy = {x, y, prevX, prevY};
				Matrix inverse = new Matrix();
				m.invert(inverse);
				inverse.mapPoints(dxy);
				focusedPlot.getShape().getBounds().offset((int) (- dxy[2] + dxy[0]), (int) (- dxy[3] + dxy[1]));
			}
			else {
				float dx = x - prevX, dy = y - prevY;
				dragMatrix.postTranslate(dx / zoomScale, dy / zoomScale);
				bgDragMatrix.postTranslate(dx, dy);
			}	
			break;
		
		case MotionEvent.ACTION_UP:
			status = DRAG_NONE;
			if(focusedPlot != null) {
				focusedPlot.getShape().getPaint().setColor(plotColor);
				focusedPlot.getShape().getPaint().setStrokeWidth(7);
			}
			break;
		}
	}
	
	public void handleRotation(MotionEvent event) {
		Matrix inverse = new Matrix();
		m.invert(inverse);
		float[] xy = { x, y };
		inverse.mapPoints(xy);
		float dx = xy[0] - context.newPlot.getShape().getBounds().centerX();
		float dy = xy[1] - context.newPlot.getShape().getBounds().centerY();
		float angle = -(float)Math.toDegrees(Math.atan(dx/dy));
		if (dy > 0) angle += 180;
		context.newPlot.setAngle(angle);		
	}

}
