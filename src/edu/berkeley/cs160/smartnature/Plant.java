package edu.berkeley.cs160.smartnature;
import java.util.ArrayList;

public class Plant {
	
	private String name;
	private ArrayList<Entry> entries;

	Plant(String name) {
		this.name = name;
		entries = new ArrayList<Entry>();
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
