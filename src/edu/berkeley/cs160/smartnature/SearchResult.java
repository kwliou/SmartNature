package edu.berkeley.cs160.smartnature;

import android.graphics.Bitmap;

public class SearchResult {

	private String name;
	private String altNames;
	private String picURL;
	private String linkURL;
	private Bitmap bmp;
	
	SearchResult(String name, String aliases, String picUrl, String url){
		this.name = name;
		altNames = aliases;
		picURL = picUrl;
		linkURL = url;
	}
	
	public Bitmap getBitmap() {
		return bmp;
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
	
	public String getLinkURL(){
		return linkURL;
	}
	
	public void setBitmap(Bitmap bmp) {
		this.bmp = bmp;
	}
	
}
