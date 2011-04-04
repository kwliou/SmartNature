package edu.berkeley.cs160.smartnature;

public class SearchResult {

	private String name;
	private String altNames;
	private String picURL;
	SearchResult(String n, String a, String p){
		name = n;
		altNames = a;
		picURL = p;
		
	}
	
	public String getName(){
		return name;
	}
	public String getAltNames(){
		return altNames;
	}
	public String getPicURL(){
		return picURL;
	}
}
