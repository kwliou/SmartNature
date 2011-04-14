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
	Path arrow;
	Paint boundPaint, rayPaint, resizePaint, textPaint;
	int zoomLevel;
	float prevX, prevY, x, y, zoomScale = 1;
	float textSize;
	boolean portraitMode, focused;
	int plotColor;
	
	private int status;
	private final static int DRAG_NONE = 0, DRAG_SHAPE = 1, DRAG_SCREEN = 2, RESIZE_SHAPE = 3;
	
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
		arrow = new Path();
		arrow.rLineTo(2, 7);
		arrow.rLineTo(-2, -3);
		arrow.rLineTo(-2, 3);
		arrow.close();
		
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FAKE_BOLD_TEXT_FLAG|Paint.DEV_KERN_TEXT_FLAG);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setTextSize(textSize);
		textPaint.setTextScaleX(getResources().getDimension(R.dimen.labelxscale_default));
		
		rayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		rayPaint.setStyle(Paint.Style.STROKE);
		rayPaint.setStrokeCap(Paint.Cap.ROUND);
		rayPaint.setStrokeMiter(getResources().getDimension(R.dimen.mitersize_default));
		rayPaint.setStrokeWidth(getResources().getDimension(R.dimen.strokesize_default));
		
		resizePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		resizePaint.setStyle(Paint.Style.STROKE);
		resizePaint.setStrokeWidth(getResources().getDimension(R.dimen.strokesize_default));

		boundPaint = new Paint(resizePaint);
		boundPaint.setColor(Color.GRAY);
		boundPaint.setPathEffect(new DashPathEffect(new float[] {8, 5}, 1));
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
		bg.draw(canvas);
		canvas.restore();
		
		m.reset();
		RectF gardenBounds = context.showFullScreen ? garden.getBounds() : garden.getBounds(portraitMode);
		m.setRectToRect(gardenBounds, getBounds(), Matrix.ScaleToFit.CENTER);
		
		if (portraitMode)
			m.postRotate(90, width/2f, width/2f);
		
		m.postConcat(dragMatrix);
		
		if (zoomLevel != 0)
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
		canvas.drawRect(editPlot.getBounds(), boundPaint);
		int oldColor = paint.getColor();
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.FILL);
		editPlot.getShape().draw(canvas);
		paint.setColor(oldColor);
		paint.setStyle(Paint.Style.STROKE);
		editPlot.getShape().draw(canvas);
		canvas.drawRect(editPlot.getResizeBox(portraitMode, getResources().getDimension(R.dimen.resizebox_min)), resizePaint);
		if (context.rotateMode) {
			canvas.drawLine(shapeBounds.centerX(), shapeBounds.centerY(), shapeBounds.centerX(), shapeBounds.top - 50, rayPaint);
			Path path = new Path(arrow);
			path.offset(shapeBounds.centerX(), shapeBounds.top - 50);
			canvas.drawPath(path, rayPaint);
		}
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
		zoomLevel = context.zoomLevel;
		zoomScale = (float) Math.pow(getResources().getDimension(R.dimen.zoom_scalar), zoomLevel);
		textPaint.setTextSize(Math.max(textSize * zoomScale, getResources().getDimension(R.dimen.labelsize_min)));
		invalidate();
		context.zoomPressed = false; 
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
		Matrix inverse = new Matrix();
		
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			m.invert(inverse);
			float[] xy = { x, y }, rxy = { x, y };
			inverse.mapPoints(xy);
			inverse.postRotate(-editPlot.getAngle(), editPlot.getBounds().centerX(), editPlot.getBounds().centerY());
			inverse.mapPoints(rxy);
			plotColor = editPlot.getPaint().getColor();
			RectF resizeBox = editPlot.getResizeBox(portraitMode, getResources().getDimension(R.dimen.resizebox_min) + 2); 
			if (resizeBox.contains(rxy[0], rxy[1])) {
				resizePaint.setColor(getResources().getColor(R.color.focused_plot));
				status = RESIZE_SHAPE;
			}
			else if (editPlot.contains(xy[0], xy[1])) {
				// set focused plot appearance
				editPlot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_editactive));
				editPlot.getPaint().setColor(getResources().getColor(R.color.focused_plot));
				status = DRAG_SHAPE;
			} else
				status = DRAG_SCREEN;
			
			break;
		
		case MotionEvent.ACTION_MOVE:
			if (status == DRAG_SHAPE) {
				float[] dxy = { prevX, prevY, x, y };
				m.invert(inverse);
				inverse.mapPoints(dxy);
				editPlot.getBounds().offset((int) (dxy[2] - dxy[0]), (int) (dxy[3] - dxy[1]));
			}
			else if (status == RESIZE_SHAPE) {
				Rect newBounds = new Rect(editPlot.getBounds());
				float[] dxy = { prevX, prevY, x, y };
				m.invert(inverse);
				inverse.postRotate(-editPlot.getAngle(), newBounds.centerX(), newBounds.centerY());
				inverse.mapPoints(dxy);
				int dx = (int) (dxy[2] - dxy[0]);
				int dy = (int) (dxy[3] - dxy[1]);
				newBounds.inset(-dx, portraitMode ? dy : -dy);
				
				float minSize = getResources().getDimension(R.dimen.resizebox_min) + 2; 
				if (newBounds.width() > minSize && newBounds.height() > minSize)
					editPlot.getShape().setBounds(newBounds);
			}
			else {
				float dx = x - prevX, dy = y - prevY;
				dragMatrix.postTranslate(dx / zoomScale, dy / zoomScale);
				bgDragMatrix.postTranslate(dx, dy);
			}
			break;
		
		case MotionEvent.ACTION_UP:
			status = DRAG_NONE;
			editPlot.getShape().getPaint().setColor(plotColor);
			editPlot.getShape().getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_edit));
			resizePaint.setColor(Color.BLACK);
			focused = false;
			break;
		}
	}
	
	public void handleRotation(MotionEvent event) {
		Matrix inverse = new Matrix();
		m.invert(inverse);
		float[] xy = { x, y };
		inverse.mapPoints(xy);
		float dx = xy[0] - editPlot.getShape().getBounds().centerX();
		float dy = xy[1] - editPlot.getShape().getBounds().centerY();
		float angle = -(float)Math.toDegrees(Math.atan(dx/dy));
		if (dy > 0)
			angle += 180;
		else if (dy < 0 && dx < 0)
			angle += 360;
		
		editPlot.setAngle(angle);		
	}

}
