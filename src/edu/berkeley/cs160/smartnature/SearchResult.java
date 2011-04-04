package edu.berkeley.cs160.smartnature;

public class SearchResult {

	private String name;
	private String altNames;
	private String picURL;
	private String linkURL;
	
	SearchResult(String n, String a, String p, String l){
		name = n;
		altNames = a;
		picURL = p;
		linkURL = l;
		
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
	
	public String getLinkURL(){
		return linkURL;
	}
}
