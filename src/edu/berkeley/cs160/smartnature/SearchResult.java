package edu.berkeley.cs160.smartnature;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.net.URL;

public class SearchResult {

	private String name;
	private String altNames;
	private String picURL;
	private String linkURL;
	private Bitmap bmp;
	
	SearchResult(String n, String a, String p, String l){
		name = n;
		altNames = a;
		picURL = p;
		linkURL = l;
		try {
			bmp = BitmapFactory.decodeStream(new URL(picURL).openConnection().getInputStream());
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public String getName(){
		return name;
	}
	public String getAltNames(){
		return altNames;
	}
	public String getPicURL(){
		return picURL;
	}
	
	public Bitmap getBitmap() {
		return bmp;
	}
	public String getLinkURL(){
		return linkURL;
	}
}
