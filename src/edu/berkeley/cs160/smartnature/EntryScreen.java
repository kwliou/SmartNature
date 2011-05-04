package edu.berkeley.cs160.smartnature;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EntryScreen extends ListActivity {

	Garden garden;
	Plot plot;
	Plant plant;
	Entry entry;
	int gardenID, plotID, plantID, entryID, pa_pk; 

	TextView entryDate, entryText;
	Button editEntryButton, deleteEntryButton;
	EditText newJournalText;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		gardenID = extras.getInt("garden_id");
		garden = GardenGnome.getGarden(gardenID);
		plotID = extras.getInt("plot_id");
		plot = garden.getPlot(plotID);
		plantID = extras.getInt("plant_id");
		plant = plot.getPlant(plantID);
		entryID = extras.getInt("entry_id");
		entry = plant.getEntry(entryID);
		pa_pk = extras.getInt("pa_pk");
		
		setTitle("Edit Entry");
		setContentView(R.layout.entry);

		newJournalText = (EditText) findViewById(R.id.newJournalText);
		newJournalText.setText(entry.getName());
		//entryDate = (TextView) findViewById(R.id.entryDate);
		//entryDate.setText(entry.getDate());
		
		//entryText = (TextView) findViewById(R.id.entryText);
		//entryText.setText(entry.getName());
		
		editEntryButton = (Button) findViewById(R.id.editEntryButton);
		editEntryButton.setOnClickListener(new OnClickListener() {
			@Override    
			public void onClick(View v) {

				String journalName = newJournalText.getText().toString().trim();

				entry.setName(journalName);
				//entryText.setText(journalName);
				//setTitle(journalName);
				PlantScreen.adapter.notifyDataSetChanged(); 
				finish();
      }
    });
		
		deleteEntryButton = (Button) findViewById(R.id.deleteEntryButton);
		deleteEntryButton.setOnClickListener(new OnClickListener() {
			@Override  
			public void onClick(View v) {
				plant.getEntries().remove(entryID);
				PlantScreen.adapter.notifyDataSetChanged(); 
				finish();
      }
    });
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.entry_menu, menu);
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
		case R.id.m_deleteentry:
			GardenGnome.removeEntry(entryID, GardenGnome.getEntryPk(pa_pk, entry), plant);
			PlantScreen.adapter.notifyDataSetChanged(); 
			finish();

		}
		return super.onOptionsItemSelected(item);
	}


}
