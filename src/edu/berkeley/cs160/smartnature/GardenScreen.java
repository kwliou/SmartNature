package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class GardenScreen extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.garden);
		Bundle extras = getIntent().getExtras();
		setTitle(extras.getString("name"));
	}

	@Override
	public Dialog onCreateDialog(int id) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
		DialogInterface.OnClickListener confirmed = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				Intent intent = new Intent(GardenScreen.this, EditRegion.class);
				Bundle bundle = new Bundle();
				String regionName = ((EditText) textEntryView.findViewById(R.id.dialog_text_entry)).getText().toString();
				bundle.putString("name", regionName);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		};
		return new AlertDialog.Builder(this)
			.setTitle(R.string.new_region_prompt)
			.setView(textEntryView)
			.setPositiveButton(R.string.alert_dialog_ok, confirmed)
			.setNegativeButton(R.string.alert_dialog_cancel, null)
			.create();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.garden_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.m_addregion:
				showDialog(0);
		}
		return super.onOptionsItemSelected(item);
	}

}