package edu.berkeley.cs160.smartnature;
import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class Plant {
	
	@Expose private String name;
	private int id;
	private ArrayList<Entry> entries;

	Plant(String name) {
		this.name = name;
		entries = new ArrayList<Entry>();
	}
	
	public int getID(){
		return id;
	}
	
	public int setID(int i){
		return id = i;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Entry> getEntries() {
		return entries;
	}

	public void addEntry(Entry entry) {
		entries.add(entry);
	}
	
}
