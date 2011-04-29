package edu.berkeley.cs160.smartnature;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class Garden {

	@Expose private String name;
	@Expose private String city;
	@Expose private String state;
	/** database id on server, equal to -1 during uploading */
	private int serverId;
	private boolean is_public;
	private String password;  // should we turn this into a SHA-256 hash?
	private ArrayList<Plot> plots = new ArrayList<Plot>();
	private RectF bounds = new RectF(0, 0, 800, 480);
	private ArrayList<String> images = new ArrayList<String>();
	
	private static Rect padding = new Rect(30, 30, 30, 30);
	private static Rect paddingLand = new Rect(20, 30, 20, 10);
	private static Rect paddingPort = new Rect(30, 20, 10, 20);
	
	Garden() { this(R.drawable.preview, ""); }
		
	Garden(String gardenName) { this(R.drawable.preview, gardenName); }
	
	Garden(int resId, String gardenName) {
		bounds = new RectF(0, 0, 800, 480);
		name = gardenName;
	}
	
	public void addPlot(Plot plot) {
		plot.setID(plots.size());
		plots.add(plot);
		// NOTE: do not immediately refresh bounds to preserve viewport
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
		refreshBounds(plots.size());
	}
	
	public void refreshBounds(int count) {
		if (plots.isEmpty())
			bounds = new RectF();
		else {
			bounds = plots.get(0).getRotateBounds();
			for (int i = 0; i < count; i++) {
				Plot p = plots.get(i);
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
			tmp.postRotate(-p.getAngle(), p.getBounds().centerX(), p.getBounds().centerY());
			tmp.mapPoints(point);
			if (p.contains(point[0], point[1]))
				return p;
		}
		return null;
	}
	
	public String getCity() { return city; }
		
	public String getName() { return name; }
	
	public String getPassword() { return password; }
	
	public Uri getPreview() {return getImage(images.size() - 1); }
	
	public RectF getRawBounds() { return bounds; }
	
	public int getServerId() { return serverId; }
	
	public String getState() { return state; }
	
	public boolean isPublic() { return is_public; }
	
	public void setCity(String city) { this.city = city; }
	
	public void setName(String name) { this.name = name; }
	
	public void setPassword(String password) { this.password = password; }
	
	public void setPublic(boolean is_public) { this.is_public = is_public; }
	
	public void setRawBounds(RectF bounds) { this.bounds = bounds; }
	
	public void setServerId(int serverId) { this.serverId = serverId; }
	
	public void setState(String state) { this.state = state; }
	
	/** Helpful ArrayList-related methods */
	
	public ArrayList<String> getImages() {return images; }
	
	public void setImages(ArrayList<String> images) {this.images = images; }
	
	public ArrayList<Plot> getPlots() { return plots; }
	
	public Plot getPlot(int index) { return plots.get(index); }
	
	public int indexOf(Plot plot) { return plots.indexOf(plot); }
	
	public boolean isEmpty() { return plots.isEmpty(); }
	
	public void remove(Plot plot) { plots.remove(plot); }
	
	public int size() { return plots.size(); }
	
	public Uri getImage(int index) {return Uri.parse(images.get(index)); }
	
	public void addImage(Uri uri) { images.add(uri.toString()); }
	
	public int numImages() { return images.size(); }
	
}
