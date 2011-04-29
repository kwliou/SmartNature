package edu.berkeley.cs160.smartnature;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
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
	TextView plantTextView, plantHint;
	Button addEntryButton, deleteEntryButton, searchPlantButton, deletePlantButton;
	int po_pk = -1, pa_pk = -1;
	Garden garden;
	Plot plot;
	Plant plant;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("name")) {
			name = extras.getString("name");
			gardenID = extras.getInt("garden_id");
			plotID = extras.getInt("plot_id");
			plantID = extras.getInt("plant_id");
			setTitle(name);
		} else {
			showDialog(0);
		}

		setContentView(R.layout.plant);
		
		garden = GardenGnome.gardens.get(gardenID);
		plot = garden.getPlot(plotID);
		plant = plot.getPlants().get(plantID);
		
		List<Integer> temp1 = StartScreen.dh.select_map_gp_po(gardenID + 1);
		for(int i = 0; i < temp1.size(); i++) {
			if(po_pk != -1) 
				break;
			if(plot.getName().equalsIgnoreCase(StartScreen.dh.select_plot_name(temp1.get(i).intValue())))
				po_pk = temp1.get(i);
		}
		List<Integer> temp2 = StartScreen.dh.select_map_pp_pa(po_pk);
		for(int i = 0; i < temp2.size(); i++) {
			if(pa_pk != -1) 
				break;
			if(plant.getName().equalsIgnoreCase(StartScreen.dh.select_plant_name(temp2.get(i).intValue())))
				pa_pk = temp2.get(i);
		}
		
		initMockData();
		getListView().setOnItemClickListener(PlantScreen.this);

		entryText = (EditText) findViewById(R.id.entryText);
		// addImage = (ImageView) findViewById(R.id.addImage);
		addEntryButton = (Button) findViewById(R.id.addEntryButton);


		plantHint = (TextView)findViewById(R.id.plant_hint);
		plantHint.setText(R.string.hint_plantscreen);

		searchPlantButton = (Button) findViewById(R.id.lookup_plant);
		//searchPlantButton.setText("Search for " + name);
		searchPlantButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(PlantScreen.this, Encyclopedia.class);
				Bundle bundle = new Bundle(1);
				bundle.putString("name", name);


				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		deletePlantButton = (Button) findViewById(R.id.delete_plant);
		//deletePlantButton.setText("Delete " + name + " from plot");
		deletePlantButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//remove(plant);
				GardenGnome.gardens.get(gardenID).getPlot(plotID).getPlants().remove(plantID);
				/*
				int e_pk = -1;
				List<Integer> temp = StartScreen.dh.select_map_pe_e(pa_pk);
				for(int i = 0; i < temp.size(); i++) {
					if(e_pk != -1) 
						break;
					if(plant.getEntries().get(i).getName().equalsIgnoreCase(StartScreen.dh.select_entry_name(temp.get(i).intValue())))
						e_pk = temp.get(i);
				}
				*/
				StartScreen.dh.delete_plant(pa_pk);
				PlotScreen.adapter.notifyDataSetChanged(); 
				finish();
			}
		});


		/*
		if (StartScreen.showHints){
			plantHint.setVisibility(View.VISIBLE);
		}else{
			plantHint.setVisibility(View.GONE);
		}
		 */

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

				GardenGnome.gardens.get(gardenID).getPlots().get(plotID).getPlants().get(plantID).addEntry(
						new Entry(entryText.getText().toString(), dateStr));
				StartScreen.dh.insert_entry(entryText.getText().toString(), dateStr);
				StartScreen.dh.insert_map_pe(pa_pk, StartScreen.dh.count_entry());
				adapter.notifyDataSetChanged(); // refresh ListView
				entry.setText("");

			}
		});
	}

	public void initMockData() {
		List<Integer> temp = StartScreen.dh.select_map_pe_e(pa_pk);
		if(plant.getEntries().size() != temp.size()) {
			for(int i = 0; i < temp.size(); i++)
				plant.addEntry(StartScreen.dh.select_entry(temp.get(i)));
		}
		adapter = new EntryAdapter(this, R.layout.journal_list_item, plant.getEntries());
		setListAdapter(adapter);
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
			final Entry e = items.get(position);
			((TextView) v.findViewById(R.id.entry_name)).setText(e.getName());
			((TextView) v.findViewById(R.id.entry_date)).setText(e.getDate());

			((Button) v.findViewById(R.id.delete_journal)).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					remove(e);
				}
			});
			return v;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.plot_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.m_home:
			Intent intent = new Intent(PlantScreen.this, StartScreen.class);
			startActivity(intent);
			break;
		case R.id.m_showhints:
			/*StartScreen.showHints = !StartScreen.showHints;
				if (StartScreen.showHints){
					plantHint.setVisibility(View.VISIBLE);
				}else{
					plantHint.setVisibility(View.GONE);
				}
				item.setTitle(StartScreen.showHints ? "Hide Hints" : "Show Hints");		
			 */
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
