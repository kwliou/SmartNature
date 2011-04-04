package edu.berkeley.cs160.smartnature;
import java.util.ArrayList;

public class Plant {
	
	private String name;
	private int id;
	private ArrayList<String> entries;

	Plant(String name) {
		this.name = name;
		entries = new ArrayList<String>();
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

	public ArrayList<String> getEntries() {
		return entries;
	}

	public void addEntry(String entry) {
		entries.add(entry);
	}
	
}
