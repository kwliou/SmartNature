package edu.berkeley.cs160.smartnature;

import android.graphics.drawable.ShapeDrawable;

public class Plot {
	
	private String name;
	private ShapeDrawable shape;
	
	Plot(ShapeDrawable shapedrawable, String regionName) {
		shape = shapedrawable;
		name = regionName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ShapeDrawable getShape() {
		return shape;
	}

	public void setShape(ShapeDrawable shape) {
		this.shape = shape;
	}
}
