package edu.berkeley.cs160.smartnature;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Entry {
	
	@Expose @SerializedName("body") private String name;
	@Expose @SerializedName("created_at") private long date;
	private int id;
	
	Entry(String name, long date) {
		this.name = name;
		this.date = date;
		this.id = 0;
	}
	
	public int getServerId() {
		return id;
	}

	public void setServerId(int serverId) {
		this.id = serverId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	
}
