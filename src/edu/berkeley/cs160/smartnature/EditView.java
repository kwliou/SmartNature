package edu.berkeley.cs160.smartnature;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
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
	Plot editPlot;
	/** the entire transformation matrix applied to the canvas */
	Matrix m = new Matrix();
	/** translation matrix applied to the canvas */
	Matrix dragMatrix = new Matrix();
	/** translation matrix applied to the background */
	Matrix bgDragMatrix = new Matrix();
	Drawable bg;
	Path arrow, resizeArrow;
	Paint boundPaint, rotatePaint, resizePaint, textPaint;
	float prevX, prevY, x, y, zoomScale = 1;
	float textSize;
	boolean portraitMode;
	int plotColor, focPlotColor;
	
	private final static int IDLE = 0, DRAG_SCREEN = 1, DRAG_SHAPE = 2, ROTATE_SHAPE = 3, RESIZE_SHAPE = 4;
	private int mode;
	
	public EditView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = (EditScreen) context;
		editPlot = this.context.plot;
		bg = getResources().getDrawable(R.drawable.tile);	
		textSize = getResources().getDimension(R.dimen.labelsize_default);
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
		/*
		arrow = new Path();
		arrow.rLineTo(2, 7);
		arrow.rLineTo(-2, -3);
		arrow.rLineTo(-2, 3);
		arrow.close();
		*/
		
		focPlotColor = getResources().getColor(R.color.focused_plot);
		
		resizeArrow = new Path();
		resizeArrow.moveTo(5, 5);
		resizeArrow.lineTo(7, 10);
		resizeArrow.lineTo(10, 7);
		resizeArrow.close();
		resizeArrow.moveTo(25, 25);
		resizeArrow.lineTo(20, 23);
		resizeArrow.lineTo(23, 20);
		resizeArrow.close();
		
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FAKE_BOLD_TEXT_FLAG|Paint.DEV_KERN_TEXT_FLAG);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setTextScaleX(getResources().getDimension(R.dimen.labelxscale_default));
		textPaint.setTextSize(textSize);
		
		rotatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		rotatePaint.setColor(Color.DKGRAY);
		rotatePaint.setStrokeCap(Paint.Cap.ROUND);
		rotatePaint.setStrokeWidth(getResources().getDimension(R.dimen.strokesize_default));
		rotatePaint.setStyle(Paint.Style.STROKE);
		
		resizePaint = new Paint(rotatePaint);
		
		boundPaint = new Paint(resizePaint);
		boundPaint.setColor(Color.GRAY);
		boundPaint.setPathEffect(new DashPathEffect(new float[] {8, 5}, 1));
	}
	
	/** called when user clicks "zoom to fit" */
	public void reset() {
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
		bg.draw(canvas);
		canvas.restore();
		
		m.reset();
		RectF gardenBounds = context.showFullScreen ? garden.getBounds() : garden.getBounds(portraitMode);
		m.setRectToRect(gardenBounds, getBounds(), Matrix.ScaleToFit.CENTER);
		if (portraitMode)
			m.postRotate(90, width/2f, width/2f);
		m.postConcat(dragMatrix);
		m.postScale(zoomScale, zoomScale, width/2f, height/2f);
		
		canvas.save();
		canvas.concat(m);
		for (Plot p: garden.getPlots()) {
			if (p != editPlot && p != context.oldPlot) {
				canvas.save();
				Rect shapeBounds = p.getBounds();
				canvas.rotate(p.getAngle(), shapeBounds.centerX(), shapeBounds.centerY());
				p.getShape().draw(canvas);
				canvas.restore();
			}
		}
		
		// "shade" over everything
		canvas.restore();
		canvas.drawARGB(65, 0, 0, 0);
		
		// draw plot being edited
		canvas.save();
		canvas.concat(m);
		Paint paint = editPlot.getShape().getPaint();
		Rect shapeBounds = editPlot.getBounds();
		canvas.rotate(editPlot.getAngle(), shapeBounds.centerX(), shapeBounds.centerY());
		//draw rectangular bounds
		canvas.drawRect(editPlot.getBounds(), boundPaint);
		//draw shape
		int oldColor = paint.getColor();
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.FILL);
		editPlot.getShape().draw(canvas);
		paint.setColor(oldColor);
		paint.setStyle(Paint.Style.STROKE);
		editPlot.getShape().draw(canvas);
		//draw resize box in bottom right corner
		RectF resizeBox = editPlot.getResizeBox(portraitMode, getResources().getDimension(R.dimen.resizebox_min));
		canvas.drawRect(resizeBox, resizePaint);
		
		Path path = new Path();
		resizeArrow.offset(resizeBox.left, resizeBox.top, path);
		canvas.drawPath(path, resizePaint);
		resizeBox.inset(5, 5);
		canvas.drawLine(resizeBox.left, resizeBox.top, resizeBox.right, resizeBox.bottom, resizePaint);
		
		//draw rotation circle/line
		canvas.drawLine(shapeBounds.centerX(), shapeBounds.centerY(), shapeBounds.centerX(), shapeBounds.top - 35, rotatePaint);
		canvas.drawCircle(shapeBounds.centerX(), shapeBounds.top - 50, 15, rotatePaint);
		
		canvas.restore();
		
		if (context.showLabels)
			for (Plot p: garden.getPlots()) {
				if (p != context.oldPlot) {
					float[] labelLoc;
					RectF bounds = p.getRotateBounds();
					if (portraitMode)
						labelLoc = new float[] { bounds.left - 10, bounds.centerY() };
					else
						labelLoc =  new float[] { bounds.centerX(), bounds.top - 10 };
					m.mapPoints(labelLoc);
					canvas.drawText(p.getName().toUpperCase(), labelLoc[0], labelLoc[1], textPaint);
				}
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
		zoomScale *= (float) Math.pow(getResources().getDimension(R.dimen.zoom_scalar), context.zoomPressed);
		textPaint.setTextSize(Math.max(textSize * zoomScale, getResources().getDimension(R.dimen.labelsize_min)));
		invalidate();
		context.zoomPressed = 0; 
	}
	
	@Override
	public void onClick(View view) {
	}
	
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		onTouchEvent(event);
		context.handleZoom();
		x = event.getX();
		y = event.getY();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				handleDown();
				break;
			case MotionEvent.ACTION_MOVE:
				handleMove();
				break;
			case MotionEvent.ACTION_UP:
				handleUp();
				break;
		}	
		prevX = x;
		prevY = y;
		invalidate();
		return true;
	}
	
	public void handleDown() {
		Matrix inverse = new Matrix();
		m.invert(inverse);
		float[] xy = { x, y }, rxy = { x, y };
		inverse.mapPoints(xy); // transformed coordinates
		inverse.postRotate(-editPlot.getAngle(), editPlot.getBounds().centerX(), editPlot.getBounds().centerY());
		inverse.mapPoints(rxy);  // transformed coordinates with plot's rotation 
		plotColor = editPlot.getPaint().getColor();
		RectF resizeBox = editPlot.getResizeBox(portraitMode, getResources().getDimension(R.dimen.resizebox_min));
		float rdx = editPlot.getBounds().centerX() - rxy[0];
		float rdy = editPlot.getBounds().top - 50 - rxy[1];
		resizeBox.inset(-10, -10);
		if (resizeBox.contains(rxy[0], rxy[1])) {
			mode = RESIZE_SHAPE;
			// set active resize appearance
			resizePaint.setColor(focPlotColor);
		} else if (rdx * rdx + rdy * rdy < 225) {
			mode = ROTATE_SHAPE;
			// set active rotate appearance
			rotatePaint.setColor(focPlotColor);
			rotatePaint.setShadowLayer(4, 0, 0, focPlotColor);
		} else if (editPlot.contains(xy[0], xy[1])) {
			mode = DRAG_SHAPE;
			// set active resize appearance
			editPlot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_editactive));
			editPlot.getPaint().setColor(focPlotColor);
		} else
			mode = DRAG_SCREEN;	
	}
	
	public void handleMove() {
		Matrix inverse = new Matrix();
		float[] dxy = { prevX, prevY, x, y };
		if (mode == DRAG_SCREEN) {
			float dx = dxy[2] - dxy[0], dy = dxy[3] - dxy[1];
			dragMatrix.postTranslate(dx / zoomScale, dy / zoomScale);
			bgDragMatrix.postTranslate(dx, dy);
		} else
			m.invert(inverse);
		
		if (mode == DRAG_SHAPE) {
			inverse.mapPoints(dxy);
			editPlot.getBounds().offset((int) (dxy[2] - dxy[0]), (int) (dxy[3] - dxy[1]));
		}
		else if (mode == RESIZE_SHAPE) {
			Rect newBounds = new Rect(editPlot.getBounds());
			inverse.postRotate(-editPlot.getAngle(), newBounds.centerX(), newBounds.centerY());
			inverse.mapPoints(dxy);
			int dx = (int) (dxy[2] - dxy[0]);
			int dy = (int) (dxy[3] - dxy[1]);
			newBounds.inset(-dx, portraitMode ? dy : -dy);
			
			float minSize = getResources().getDimension(R.dimen.resizebox_min) + 2; 
			if (newBounds.width() > minSize && newBounds.height() > minSize)
				editPlot.getShape().setBounds(newBounds);
		}
		else if (mode == ROTATE_SHAPE) {
			inverse.mapPoints(dxy);
			float dx = dxy[2] - editPlot.getBounds().centerX();
			float dy = dxy[3] - editPlot.getBounds().centerY();
			float angle = -(float)Math.toDegrees(Math.atan(dx/dy));
			if (dy > 0)
				angle += 180;
			else if (dy < 0 && dx < 0)
				angle += 360;
			
			editPlot.setAngle(angle);
		}		
	}
	
	public void handleUp() {
		mode = IDLE;
		editPlot.getShape().getPaint().setColor(plotColor);
		editPlot.getShape().getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_edit));
		resizePaint.setColor(Color.DKGRAY);
		rotatePaint.setColor(Color.DKGRAY);
		rotatePaint.clearShadowLayer();
	}
	
}
