package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

public class EditPlot extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plot_edit);
		Bundle extras = getIntent().getExtras();
		setTitle(extras.getString("name") + " (Edit mode)");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.plot_edit_menu, menu);
		return true;
	}

}