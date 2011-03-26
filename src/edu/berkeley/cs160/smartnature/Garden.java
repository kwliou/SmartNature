package edu.berkeley.cs160.smartnature;

import java.util.ArrayList;

import android.graphics.Rect;

public class Garden {
	
	private String name;
	private int previewId;
	private ArrayList<Plot> plots;
	private Rect bounds;
	private Rect paddingLand = new Rect(20, 30, 20, 10);
	private Rect paddingPort = new Rect(30, 20, 20, 10);
	
	Garden(int resId, String gardenName) {
		previewId = resId;
		name = gardenName;
		plots = new ArrayList<Plot>();
		bounds = new Rect();
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
	public Rect getRawBounds() {
		return bounds;
	}
	
	public Rect getLandBounds() {
		Rect padded = new Rect(bounds);
		padded.left -= paddingLand.left;
		padded.top -= paddingLand.top;
		padded.right += paddingLand.right;
		padded.bottom += paddingLand.bottom;
		return padded;
	}
	
	public Rect getPortBounds() {
		Rect padded = new Rect(bounds);
		padded.left -= paddingPort.left;
		padded.top -= paddingPort.top;
		padded.right += paddingPort.right;
		padded.bottom += paddingPort.bottom;
		return padded;
	}
	
}
