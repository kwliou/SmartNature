package edu.berkeley.cs160.smartnature;

import android.content.ContentValues;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Entry {
	
	private int db_id;
	/** database id on server */
	private int id;
	@Expose private String body;
	@Expose @SerializedName("created_at") private long date;
	
	Entry(String body, long date) {
		this.body = body;
		this.date = date;
	}
	
	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		values.put("server_id", id);
		values.put("name", body);
		values.put("date", Long.toString(date));
		return values;
	}
	
	public int getServerId() { return id; }
	
	public void setServerId(int serverId) { this.id = serverId; }
	
	public String getBody() { return body; }
	
	public void setBody(String body) { this.body = body; }
	
	public long getDate() { return date; }
	
	public void setDate(long date) { this.date = date; }
	
	public int getId() { return db_id; }
	
	public void setId(int id) { db_id = id; }
	
}
