package edu.berkeley.cs160.smartnature;

import java.util.ArrayList;

import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.*;

public class Plot {
	
	static final int RECT = 0, OVAL = 1, POLY = 2;
	private String name;
	private ShapeDrawable shape;
	private int type;
	private float[] polyPoints;
	private float rotation; // angle of direction where 0 is "north"
	private int color;
	
	private int id;
	private ArrayList<Plant> plants = new ArrayList<Plant>();
	
	/** create a polygonal plot */
	Plot(String plotName, Rect bounds, float angle, float[] points) {
		name = plotName;
		type = POLY;
		polyPoints = points;
		rotation = angle;
		color = 0;
		Path p = new Path();
		p.moveTo(points[0], points[1]);
		for (int i = 2; i < points.length; i += 2)
			p.lineTo(points[i], points[i + 1]);
		p.close();
		PathShape pshape = new PathShape(p, bounds.width(), bounds.height());
		shape = new ShapeDrawable(pshape);
		shape.setBounds(bounds);
	}
	
	/** create a rectangular or elliptical plot */
	Plot(String plotName, Rect bounds, float angle, int shapeType) {
		name = plotName;
		type = shapeType;
		rotation = angle;
		shape = new ShapeDrawable(type == OVAL ? new OvalShape() : new RectShape());
		shape.setBounds(bounds);
	}
		
	
	public ArrayList<Plant> getPlants() {
		return plants;
	}

	public void addPlant(Plant p) {
		p.setID(plants.size());
		plants.add(p);
	}
	
	
	public int getID(){
		return id;
	}
	
	public int setID(int i){
		return id = i;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public float getAngle() {
		return rotation;
	}
	
	public void setAngle(float angle) {
		this.rotation = angle;
	}
	
	public int getColor() {
		return color;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	
	public float[] getPoint () {
		return polyPoints;
	}
	
	public ShapeDrawable getShape() {
		return shape;
	}
	
	public void setShape(ShapeDrawable shape) {
		this.shape = shape;
	}
	
	public boolean contains(float x, float y) {
		if (type == OVAL)
			return ovalContains(x, y);
		else if (type == POLY)
			return pathContains(x, y);
		else
			return rectContains(x, y);
	}
	
	public boolean rectContains(float x, float y) {
		Rect bounds = shape.getBounds();
		return bounds.left <= x && x <= bounds.right && bounds.top <= y && y <= bounds.bottom;  
	}
	
	/** @see java.awt.geom.Ellipse2D#contains(double, double) */
	public boolean ovalContains(float x, float y) {
		Rect bounds = shape.getBounds();
		float ellw = bounds.width(), ellh = bounds.height();
		float normx = (x - bounds.left) / ellw - 0.5f;
		float normy = (y - bounds.top) / ellh - 0.5f;
		return (normx * normx + normy * normy) <= 0.25f;
	}
	
	/** @see <a href="http://alienryderflex.com/polygon">http://alienryderflex.com/polygon</a> */
	public boolean pathContains(float x, float y) {
		x -= shape.getBounds().left;
		y -= shape.getBounds().top;
		int polySides = polyPoints.length / 2;
		float[] polyX = new float[polySides], polyY = new float[polySides];
		for (int i = 0; i < polyPoints.length; i += 2) {
			polyX[i/2] = polyPoints[i];
			polyY[i/2] = polyPoints[i + 1]; 
		}
		
		boolean oddTransitions = false;
		for (int i = 0, j = polySides - 1; i < polySides; j = i++) {
			if ((polyY[i] < y && polyY[j] >= y) || (polyY[j] < y && polyY[i] >= y))
				if (polyX[i] + (y - polyY[i]) / (polyY[j] - polyY[i]) * (polyX[j] - polyX[i]) < x)
					oddTransitions = !oddTransitions;
		}
		return oddTransitions;
	}
	
	public int getType() {
		return type;
	}
	
}
