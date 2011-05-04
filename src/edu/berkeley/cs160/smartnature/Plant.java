package edu.berkeley.cs160.smartnature;
import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class Plant {
	
	@Expose private String name;
	@Expose private String online_entry;
	/** database id on server */
	private int id;
	private ArrayList<Entry> entries = new ArrayList<Entry>();
	
	Plant(String name) {
		this.name = name;
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

	public ArrayList<Entry> getEntries() {
		return entries;
	}
	
	public void setEntries(ArrayList<Entry> entries) {
		this.entries = entries;
	}
	
	public void addEntry(Entry entry) {
		entries.add(entry);
	}
	
	public Entry getEntry(int index) { 
		return entries.get(index); 
	}
	
}
