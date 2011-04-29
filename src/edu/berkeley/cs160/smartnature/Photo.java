package edu.berkeley.cs160.smartnature;

import com.google.gson.annotations.Expose;

import android.net.Uri;

public class Photo {
	
	private int serverId;
	/** uri is a string for json convenience */
	private String uri;
	@Expose private String title;
	
	Photo(Uri uri) {
		this.uri = uri.toString();
	}
	
	public int getServerId() { return serverId; }
	
	public String getTitle() { return title; }
	
	public Uri getUri() { return Uri.parse(uri); }
	
	public void setServerId(int serverId) { this.serverId = serverId; }
	
	public void setTitle(String title) { this.title = title; }
	
	public void setUri(Uri uri) { this.uri = uri.toString(); }
	
}