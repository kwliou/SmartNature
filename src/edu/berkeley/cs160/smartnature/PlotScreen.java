package edu.berkeley.cs160.smartnature;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class PlotScreen extends ListActivity implements DialogInterface.OnClickListener, View.OnClickListener, AdapterView.OnItemClickListener {
	
	ListView plantListView;
	EditText plantName;
	AlertDialog dialog;
	View textEntryView;
	
	Garden garden;
	Plot plot;
	PlantAdapter adapter;
	Plant plant;
	int gardenIndex, plotIndex; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (!intent.hasExtra("garden_index")) {
			finish();
			return;
		}
		GardenGnome.init(this);
		gardenIndex = intent.getIntExtra("garden_index", 0);
		garden = GardenGnome.getGarden(gardenIndex);
		plotIndex = intent.getIntExtra("plot_index", 0);
		plot = garden.getPlot(plotIndex);
		setTitle(plot.getName());
		
		setContentView(R.layout.plot);
		
		plantName = (EditText) findViewById(R.id.new_plant_name);
		adapter = new PlantAdapter(this, R.layout.plant_list_item, plot.getPlants());
		setListAdapter(adapter);
		getListView().setOnItemClickListener(this);
		((Button) findViewById(R.id.addPlantButton)).setOnClickListener(this);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		textEntryView = LayoutInflater.from(this).inflate(R.layout.text_entry_dialog, null);
		
		dialog = new AlertDialog.Builder(this).setView(textEntryView)
			.setTitle("Edit plot name")
			.setPositiveButton(R.string.alert_dialog_rename, this)
			.setNegativeButton(R.string.alert_dialog_cancel, null)
			.create();
		
		// automatically show soft keyboard
		EditText input = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
		input.setText(plot.getName());
		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus)
					dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			}
		});
		
		return dialog;
	}
	
	@Override
	public void onClick(DialogInterface dialog, int whichButton) {
		EditText textEntry = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
		String plotName = textEntry.getText().toString().trim();
		if (plotName.length() == 0)
			return;
		plot.setName(plotName);
		GardenGnome.updatePlot(plot);
		setTitle(plotName);
	}
	
	@Override
	public void onClick(View view) {
		String plantString = plantName.getText().toString();
		if (plantString.length() == 0)
			plantString = "Untitled plant";
		
		GardenGnome.addPlant(plot, new Plant(plantString));
		plantName.setText("");
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(PlotScreen.this, PlantScreen.class);
		intent.putExtra("name", plot.getPlant(position).getName());
		intent.putExtra("garden_index", gardenIndex);
		intent.putExtra("plot_index", plotIndex);
		intent.putExtra("plant_index", position);
		startActivityForResult(intent, 0);
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
				Intent intent = new Intent(this, StartScreen.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;
			case R.id.m_deleteplot:
				GardenGnome.removePlot(garden, plot);
				finish();
				break;
			case R.id.m_renameplot:
				removeDialog(0);
				showDialog(0);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	class PlantAdapter extends ArrayAdapter<Plant> {

		private ArrayList<Plant> plants;
		private LayoutInflater li;

		public PlantAdapter(Context context, int textViewResourceId, ArrayList<Plant> items) {
			super(context, textViewResourceId, items);
			li = ((ListActivity) context).getLayoutInflater();
			this.plants = items;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null)
				v = li.inflate(R.layout.plant_list_item, null);
			plant = plants.get(position);
			((TextView) v.findViewById(R.id.plant_name)).setText(plant.getName());

			return v;
		}

	}
	
}
