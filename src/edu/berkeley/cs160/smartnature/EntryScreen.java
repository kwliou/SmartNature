package edu.berkeley.cs160.smartnature;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EntryScreen extends ListActivity implements View.OnClickListener{

	Garden garden;
	Plot plot;
	Plant plant;
	Entry entry;
	int gardenID, plotID, plantID, entryID, pa_pk; 

	TextView entryDate, entryText;
	Button editEntryButton, deleteEntryButton, speakButton;
	EditText newJournalText;
	
	/** Voice Recognition */
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	ArrayList<String> matches = new ArrayList<String>();
	
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
		
		/** Voice Recognition **/
    speakButton = (Button) findViewById(R.id.recordVoiceButton2);
    
    PackageManager pm = getPackageManager();
    List<ResolveInfo> activities = pm.queryIntentActivities(
            new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
    if (activities.size() != 0) {
        speakButton.setOnClickListener(this);
    } else {
        //speakButton.setEnabled(false);
        //speakButton.setText("Recognizer not present");
        speakButton.setVisibility(View.GONE);
    }
		
	}
	
  /** Handle the click on the start recognition button. */
	@Override
	public void onClick(View v) {
    if (v.getId() == R.id.recordVoiceButton2) {
      startVoiceRecognitionActivity();
    }
	}

  /** Fire an intent to start the speech recognition activity. */
  private void startVoiceRecognitionActivity() {
      Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
              RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
      intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
      startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
  }

  /** Handle the results from the recognition activity. */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
      		matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);  
  				EditText entry = (EditText) findViewById(R.id.entryText);  				
					entry.setText(entry.getText() + matches.get(0).toString());
      }
      super.onActivityResult(requestCode, resultCode, data);
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
