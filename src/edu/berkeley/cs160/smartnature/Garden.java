package edu.berkeley.cs160.smartnature;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.ArrayList;

public class Garden {
	
	private String name;
	private int previewId;
	private ArrayList<Plot> plots;
	private Rect bounds;
	private Rect padding = new Rect(30, 30, 30, 10);
	private Rect paddingLand = new Rect(20, 30, 20, 10);
	private Rect paddingPort = new Rect(30, 20, 20, 10);
	
	Garden(String gardenName) {
		name = gardenName;
		plots = new ArrayList<Plot>();
		bounds = new Rect();
	}
	
	Garden(int resId, String gardenName) {
		this(gardenName);
		previewId = resId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPreviewId() {
		return previewId;
	}

	public void setPreviewId(int previewId) {
		this.previewId = previewId;
	}
	
	public ArrayList<Plot> getPlots() {
		return plots;
	}

	public void addPlot(Plot plot) {
		Rect pBounds = plot.getShape().getBounds();
		if (plots.isEmpty()) {
			bounds = new Rect(pBounds);
		}
		else {
			bounds.left = Math.min(bounds.left, pBounds.left);
			bounds.top = Math.min(bounds.top, pBounds.top);
			bounds.right = Math.max(bounds.right, pBounds.right);
			bounds.bottom = Math.max(bounds.bottom, pBounds.bottom);
		}
		plots.add(plot);
	}
	
	/** add a polygonal plot */
	public void addPlot(String plotName, Rect plotBounds, float rotation, float[] points) {
		addPlot(new Plot(plotName, plotBounds, rotation, points)); 
	}
	
	/** add a rectangular or elliptical plot */
	public void addPlot(String plotName, Rect plotBounds, float rotation, int shapeType) {
		addPlot(new Plot(plotName, plotBounds, rotation, shapeType)); 
	}
	
	public Rect getRawBounds() {
		return bounds;
	}
	
	/** Used for full screen */
	public RectF getBounds() {
		RectF padded = new RectF(bounds);
		padded.left -= padding.left;
		padded.top -= padding.top;
		padded.right += padding.right;
		padded.bottom += padding.bottom;
		return padded;
	}
	
	/** Used for portrait/landscape mode */
	public RectF getBounds(boolean portraitMode) {
		return portraitMode ? getPortBounds() : getLandBounds();
	}
	
	/** Used for portrait mode */
	public RectF getPortBounds() {
		RectF padded = new RectF(bounds);
		padded.left -= paddingPort.left;
		padded.top -= paddingPort.top;
		padded.right += paddingPort.right;
		padded.bottom += paddingPort.bottom;
		return padded;
	}
	
	/** Used for landscape mode */
	public RectF getLandBounds() {
		RectF padded = new RectF(bounds);
		padded.left -= paddingLand.left;
		padded.top -= paddingLand.top;
		padded.right += paddingLand.right;
		padded.bottom += paddingLand.bottom;
		return padded;
	}
	
	/** finds a plot which contains (x, y) after being transformed by the matrix */ 
	public Plot plotAt(float x, float y, Matrix matrix) {
		Matrix inverse = new Matrix();
		matrix.invert(inverse);
		float[] point = { x, y };
		inverse.mapPoints(point);
		for (Plot p : plots) {
			if (p.contains(point[0], point[1]))
				return p;
		}
		return null;
	}
}
