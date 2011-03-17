package edu.berkeley.cs160.smartnature;

public class Garden {
	
	private String name;
	private int previewId;
	
	Garden(int resId, String gardenName) {
		previewId = resId;
		name = gardenName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPreviewId() {
		return previewId;
	}

	public void setPreviewId(int previewId) {
		this.previewId = previewId;
	}
}
