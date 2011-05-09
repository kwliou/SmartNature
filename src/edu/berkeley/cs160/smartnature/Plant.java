package edu.berkeley.cs160.smartnature;
import java.util.ArrayList;

import android.content.ContentValues;

import com.google.gson.annotations.Expose;

public class Plant {
		
	private int db_id;
	/** database id on server */
	private int id;
	@Expose private String name;
	//@Expose private String online_entry;
	private ArrayList<Entry> entries = new ArrayList<Entry>();
	
	Plant(String name) { this.name = name; }
	
	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		values.put("server_id", id);
		values.put("name", name);
		return values;
	}
	
	public ArrayList<Entry> getEntries() { return entries; }
	
	public int getId() { return db_id; }
	
	public String getName() { return name; }
	
	public int getServerId() { return id; }
	
	public void setEntries(ArrayList<Entry> entries) { this.entries = entries; }
	
	public void setId(int id) { db_id = id; }
	
	public void setName(String name) { this.name = name; }
	
	public void setServerId(int serverId) { this.id = serverId; }
	
	/** ArrayList-related methods */
	
	public void addEntry(Entry entry) { entries.add(entry); }
	
	public Entry getEntry(int index) { return entries.get(index); }
	
}
