package edu.berkeley.cs160.smartnature;

public class Entry {
	
	private String name;
	private String date;

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
