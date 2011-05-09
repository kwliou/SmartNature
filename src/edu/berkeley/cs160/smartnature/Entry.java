package edu.berkeley.cs160.smartnature;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Entry {
	
	@Expose private String body;
	@Expose @SerializedName("created_at") private long date;
	private int id;
	
	private int entry_num;
	
	Entry(String body, long date) {
		this.body = body;
		this.date = date;
	}
	
	public int getServerId() {
		return id;
	}

	public void setServerId(int serverId) {
		this.id = serverId;
	}
	
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public int getEntryNum() { return this.entry_num; }

	public void setEntryNum(int entry_num) { this.entry_num = entry_num; }
	
}
