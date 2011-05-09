package edu.berkeley.cs160.smartnature;

import com.google.gson.annotations.Expose;

import android.content.ContentValues;
import android.net.Uri;

public class Photo {
	
	private int db_id;
	private int id;
	/** uri is a string for json convenience */
	private String uri;
	@Expose private String title;
	
	Photo(String uri) { this.uri = uri; }
	
	Photo(Uri uri) { this.uri = uri.toString(); }
	
	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		values.put("server_id", id);
		values.put("name", title);
		values.put("uri", uri);
		return values;
	}
	
	public int getId() { return db_id; }
	
	public int getServerId() { return id; }
	
	public String getTitle() { return title; }
	
	public Uri getUri() { return Uri.parse(uri); }
	
	public void setId(int id) { db_id = id; }
	
	public void setServerId(int serverId) { id = serverId; }
	
	public void setTitle(String title) { this.title = title; }
	
	public void setUri(Uri uri) { this.uri = uri.toString(); }
	
}