package edu.berkeley.cs160.smartnature;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.ArrayList;

public class Garden {
	
	private String name;
	private int previewId;
	private ArrayList<Plot> plots;
	private RectF bounds;
	private static Rect padding = new Rect(30, 30, 30, 30);
	private static Rect paddingLand = new Rect(20, 30, 20, 10);
	private static Rect paddingPort = new Rect(30, 20, 10, 20);
	
	Garden(int resId, String gardenName) {
		name = gardenName;
		plots = new ArrayList<Plot>();
		bounds = new RectF();
		previewId = resId;
	}
	
	Garden(String gardenName) {
		this(R.drawable.preview, gardenName);
	}
	
	Garden() {
		this(R.drawable.preview, "");
	}
	
	public void addPlot(Plot plot) {
		RectF pBounds = plot.getRotateBounds();
		if (plots.isEmpty()) {
			bounds = new RectF(pBounds);
		}
		else {
			bounds.left = Math.min(bounds.left, pBounds.left);
			bounds.top = Math.min(bounds.top, pBounds.top);
			bounds.right = Math.max(bounds.right, pBounds.right);
			bounds.bottom = Math.max(bounds.bottom, pBounds.bottom);
		}
		
		plot.setID(plots.size());
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
	
	/** Used for full screen mode */
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
		Rect offset = portraitMode ? paddingPort : paddingLand;
		RectF padded = new RectF(bounds);
		padded.left -= offset.left;
		padded.top -= offset.top;
		padded.right += offset.right;
		padded.bottom += offset.bottom;
		return padded;
	}
	
	public void refreshBounds() {
		if (plots.isEmpty())
			bounds = new RectF();
		else {
			bounds = plots.get(0).getRotateBounds();
			for (Plot p : plots) {
				RectF pBounds = p.getRotateBounds();
				bounds.left = Math.min(bounds.left, pBounds.left);
				bounds.top = Math.min(bounds.top, pBounds.top);
				bounds.right = Math.max(bounds.right, pBounds.right);
				bounds.bottom = Math.max(bounds.bottom, pBounds.bottom);
			}
		}
	}
	
	/** finds a plot which contains (x, y) after being transformed by the matrix */ 
	public Plot plotAt(float x, float y, Matrix matrix) {
		Matrix inverse = new Matrix();
		matrix.invert(inverse);
		for (Plot p : plots) {
			float[] point = { x, y };
			Matrix tmp = new Matrix(inverse);
			tmp.postRotate(-p.getAngle(), p.getShape().getBounds().centerX(), p.getShape().getBounds().centerY());
			tmp.mapPoints(point);
			if (p.contains(point[0], point[1]))
				return p;
		}
		return null;
	}
	
	public String getName() { return name; }

	public void setName(String name) { this.name = name; }

	public int getPreviewId() { return previewId; }

	public void setPreviewId(int previewId) { this.previewId = previewId; }
	
	public ArrayList<Plot> getPlots() { return plots; }
	
	public int getPlotId(Plot plot) { return plots.indexOf(plot); }
	
	public Plot getPlot(int id) { return plots.get(id); }
	
	public void setPlot(int id, Plot plot) { plots.add(id, plot); }

	public RectF getRawBounds() { return bounds; }
	
	public int size() { return plots.size(); }
	
}
