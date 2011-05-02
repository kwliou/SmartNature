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

public class PlotScreen extends ListActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

	ListView plantListView;
	EditText plantName;
	AlertDialog dialog;

	Garden garden;
	Plot plot;
	static PlantAdapter adapter;
	Plant plant;
	int gardenID, plotID; 
	int po_pk;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		gardenID = extras.getInt("garden_id");
		garden = GardenGnome.getGarden(gardenID);
		plotID = extras.getInt("plot_id");
		plot = garden.getPlot(plotID);
		setTitle(plot.getName());

		setContentView(R.layout.plot);
		/*
		List<Integer> temp = StartScreen.dh.select_map_gp_po(gardenID + 1);
		po_pk = -1;
		for(int i = 0; i < temp.size(); i++) {
			if(po_pk != -1) 
				break;
			if(plot.getName().equalsIgnoreCase(StartScreen.dh.select_plot_name(temp.get(i).intValue())))
				po_pk = temp.get(i);
		}
		*/
		initMockData();
		getListView().setOnItemClickListener(this);

		plantName = (EditText) findViewById(R.id.new_plant_name);
		((Button) findViewById(R.id.addPlantButton)).setOnClickListener(this);

		/*
		backButton = (Button) findViewById(R.id.backButton);
		backButton.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
        	finish();
        }
    });
		 */
	}

	public void initMockData() {
		//plot.getPlants().clear();
		/*List<Integer> temp = StartScreen.dh.select_map_pp_pa(po_pk);
		System.err.println("size = " + temp.size());
		if(plot.getPlants().size() != temp.size()) {
			for(int i = 0; i < temp.size(); i++)
				plot.addPlant(StartScreen.dh.select_plant(temp.get(i)));
		}*/
		adapter = new PlantAdapter(this, R.layout.plant_list_item, plot.getPlants());
		setListAdapter(adapter);
	}

	// currently unused
	@Override
	public Dialog onCreateDialog(int id) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.text_entry_dialog, null);
		DialogInterface.OnClickListener confirmed = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				EditText plantName = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
				plot.addPlant(new Plant(plantName.getText().toString()));
				adapter.notifyDataSetChanged(); //refresh ListView
			}
		};
		DialogInterface.OnClickListener canceled = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				finish();
			}
		};
		dialog = new AlertDialog.Builder(this)
		.setTitle(R.string.new_plant_prompt)
		.setView(textEntryView)
		.setPositiveButton(R.string.alert_dialog_ok, confirmed)
		.setNegativeButton(R.string.alert_dialog_cancel, canceled)
		.create();

		// automatically show soft keyboard
		EditText input = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
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
	public void onClick(View view) {
		//showDialog(0);
		String plantString = plantName.getText().toString();
		if (plantString.length() == 0)
			plantString = "Untitled plant";
		
		plot.addPlant(new Plant(plantString));
		//StartScreen.dh.insert_plant(plantName.getText().toString(), 0);
		//StartScreen.dh.insert_map_pp(po_pk, StartScreen.dh.count_plant());
		plantName.setText("");
		adapter.notifyDataSetChanged(); //refresh ListView
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
			/*
			v.findViewById(R.id.lookup_plant).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
	  				Intent intent = new Intent(PlotScreen.this, Encyclopedia.class);
	  				Bundle bundle = new Bundle(1);
	  				bundle.putString("name", plant.getName());


	  				intent.putExtras(bundle);
	  				startActivity(intent);
				}
			});
			v.findViewById(R.id.add_journal).setOnClickListener(new OnClickListener() {
		        public void onClick(View v) {
		      		Intent intent = new Intent(PlotScreen.this, PlantScreen.class);
		      		Bundle bundle = new Bundle(4);
		      		bundle.putString("name", garden.getPlot(plotID).getPlants().get(position).getName());
		      		bundle.putInt("garden_id", gardenID);
		      		bundle.putInt("plot_id", plotID);
		      		bundle.putInt("plant_id", position);

		      		intent.putExtras(bundle);
		      		startActivity(intent);
		        }
			});
			v.findViewById(R.id.delete_plant).setOnClickListener(new OnClickListener() {
		        public void onClick(View v) {
		        	remove(plant);
		        }
			});
			 */

			return v;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(PlotScreen.this, PlantScreen.class);
		intent.putExtra("name", plot.getPlant(position).getName());
		intent.putExtra("garden_id", gardenID);
		intent.putExtra("plot_id", plotID);
		intent.putExtra("plant_id", position);
		startActivity(intent);
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
		case R.id.m_showhints:
			/*
				StartScreen.showHints = !StartScreen.showHints;
				item.setTitle(StartScreen.showHints ? "Hide Hints" : "Show Hints");			
				break;
			 */
		case R.id.m_deleteplot:
			garden.remove(plot);
			//StartScreen.dh.delete_plot(po_pk);
			//StartScreen.dh.delete_map_gp(po_pk);
			//EditScreen.editView.invalidate();
			finish();

		}
		return super.onOptionsItemSelected(item);
	}


}
