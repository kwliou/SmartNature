package edu.berkeley.cs160.smartnature;

import com.google.gson.annotations.Expose;

public class Entry {
	
	@Expose private String name;
	@Expose private String date;

	Entry(String name, String date) {
		this.name = name;
		this.date = date;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	
}
