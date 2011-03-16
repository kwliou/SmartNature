package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

public class EditRegion extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.region_edit);
		Bundle extras = getIntent().getExtras();
		setTitle(extras.getString("name") + " (Edit mode)");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.region_edit_menu, menu);
		return true;
	}

}