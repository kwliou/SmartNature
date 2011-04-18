package edu.berkeley.cs160.smartnature;

import java.util.Date;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class PlantScreen extends ListActivity implements View.OnClickListener, View.OnTouchListener, AdapterView.OnItemClickListener {

	AlertDialog dialog;

	static EntryAdapter adapter;

	int gardenID, plotID, plantID;

	LinearLayout entries;
	TextView dateText;
	TextView text;

	String name;
	EditText entryText;
	ImageView addImage;
	TextView plantTextView;
	Button addEntryButton, backButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("name")) {
			name = extras.getString("name");
			gardenID = extras.getInt("gardenID");
			plotID = extras.getInt("plotID");
			plantID = extras.getInt("plantID");
			setTitle(name);
		} else {
			showDialog(0);
		}

		setContentView(R.layout.plant);
		initMockData();
		getListView().setOnItemClickListener(PlantScreen.this);

		entryText = (EditText) findViewById(R.id.entryText);
		// addImage = (ImageView) findViewById(R.id.addImage);
		addEntryButton = (Button) findViewById(R.id.addEntryButton);
		plantTextView = (TextView) findViewById(R.id.plantTextView);
		plantTextView.setText(name);

		/*
		 * addImage.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { //TODO // Call Deepti's
		 * Picture dialog } });
		 */

		// Button addPicButton = (Button) findViewById(R.id.addPicButton);
		Button addEntryButton = (Button) findViewById(R.id.addEntryButton);

		addEntryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText entry = (EditText) findViewById(R.id.entryText);
				Date currentDate = new Date();
				String dateStr = currentDate.toString();
				StartScreen.gardens.get(gardenID).getPlots().get(plotID).getPlants().get(plantID).addEntry(
					new Entry(entryText.getText().toString(), dateStr));
				adapter.notifyDataSetChanged(); // refresh ListView
				entry.setText("");

			}
		});

	}

	public void initMockData() {

		adapter = new EntryAdapter(this, R.layout.journal_list_item,
				StartScreen.gardens.get(gardenID).getPlots().get(plotID).getPlants().get(plantID).getEntries());
		setListAdapter(adapter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.m_home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		System.out.println("touched");
		return false;
	}

	@Override
	public void onClick(View view) {
		System.out.println("clicked");
	}

	class EntryAdapter extends ArrayAdapter<Entry> {

		private ArrayList<Entry> items;
		private LayoutInflater li;

		public EntryAdapter(Context context, int textViewResourceId, ArrayList<Entry> items) {
			super(context, textViewResourceId, items);
			li = ((ListActivity) context).getLayoutInflater();
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null)
				v = li.inflate(R.layout.journal_list_item, null);
			Entry e = items.get(position);
			((TextView) v.findViewById(R.id.entry_name)).setText(e.getName());
			((TextView) v.findViewById(R.id.entry_date)).setText(e.getDate());
			return v;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	}

}
