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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

public class StartScreen extends Activity implements OnClickListener, OnItemClickListener, OnItemSelectedListener {
	
	Gallery gardenPreviews;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		gardenPreviews = (Gallery) findViewById(R.id.gallery);
		String[] mockGardens = { "BYA", "Karl Linn", "Peralta" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.gallery_item, mockGardens);
		gardenPreviews.setAdapter(adapter);
		gardenPreviews.setOnItemClickListener(this);
		gardenPreviews.setOnItemSelectedListener(this);

		((Button) findViewById(R.id.new_garden)).setOnClickListener(this);
		((Button) findViewById(R.id.imageButton1)).setOnClickListener(this);
		((Button) findViewById(R.id.imageButton2)).setOnClickListener(this);	
	}

	@Override
	public Dialog onCreateDialog(int id) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
		DialogInterface.OnClickListener confirmed = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				Intent intent = new Intent(StartScreen.this, Garden.class);
				Bundle bundle = new Bundle();
				String gardenName = ((EditText) textEntryView.findViewById(R.id.dialog_text_entry)).getText().toString();
				bundle.putString("name", gardenName);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		};
		return new AlertDialog.Builder(this)
			.setTitle(R.string.new_garden_prompt)
			.setView(textEntryView)
			.setPositiveButton(R.string.alert_dialog_ok, confirmed)
			.setNegativeButton(R.string.alert_dialog_cancel, null)
			.create();
	}

	@Override
	public void onClick(View view) {
		int position = gardenPreviews.getSelectedItemPosition();
		switch (view.getId()) {
			case R.id.new_garden:
				showDialog(0);
				break;
			case R.id.imageButton1:
				if (position > 0)
					gardenPreviews.setSelection(position - 1);
				break;
			case R.id.imageButton2:
				if (position < gardenPreviews.getCount() - 1)
					gardenPreviews.setSelection(position + 1);
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, Garden.class);
		Bundle bundle = new Bundle();
		bundle.putCharSequence("name", ((TextView) view).getText());
		intent.putExtras(bundle);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.m_contact:
		case R.id.m_options:
		case R.id.m_help:
		}
		return super.onOptionsItemSelected(item);
	}
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        int previewIds[] = {R.drawable.preview, R.drawable.preview2, R.drawable.preview3}; 
		((ImageView)findViewById(R.id.image_preview)).setImageResource(previewIds[position]);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) { }

}