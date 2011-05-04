package edu.berkeley.cs160.smartnature;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class PlantScreen extends ListActivity implements DialogInterface.OnClickListener, View.OnClickListener, View.OnTouchListener, AdapterView.OnItemClickListener {

	AlertDialog dialog;

	static EntryAdapter adapter;

	int gardenID, plotID, plantID;

	LinearLayout entries;
	TextView dateText;
	TextView text;

	String name;
	EditText entryText, editEntryText;
	ImageView addImage;
	TextView plantTextView, plantHint, entryName;
	Button addEntryButton, deleteEntryButton, searchPlantButton, deletePlantButton;
	Button editJournalButton, deleteJournalButton, replaceJournalButton, speakButton;
	int po_pk, pa_pk;
	Garden garden;
	Plot plot;
	Plant plant;
	
	View textEntryView;
	EditText input;
	
	/** Voice Recognition */
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	ArrayList<String> matches = new ArrayList<String>();
	SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy h:mm a");
	
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
		
		garden = GardenGnome.getGarden(gardenID);
		plot = garden.getPlot(plotID);
		plant = plot.getPlants().get(plantID);
		
		po_pk = GardenGnome.getPlotPk(gardenID, plot);
		pa_pk = GardenGnome.getPlantPk(po_pk, plant);
		
		setContentView(R.layout.plant);
		initMockData();
		getListView().setOnItemClickListener(PlantScreen.this);
		
		plantHint = (TextView)findViewById(R.id.plant_hint);
		plantHint.setText(R.string.hint_plantscreen);

		/*
		editJournalButton = (Button)findViewById(R.id.edit_journal);
		deleteJournalButton = (Button)findViewById(R.id.delete_journal);
		editEntryText = (EditText)findViewById(R.id.journal_edittext);
		entryName = (TextView)findViewById(R.id.entry_name); 
		
		if (StartScreen.showHints){
			plantHint.setVisibility(View.VISIBLE);
		}else{
			plantHint.setVisibility(View.GONE);
		}
		*/

		entryText = (EditText) findViewById(R.id.entryText);
		addEntryButton = (Button) findViewById(R.id.addEntryButton);
		addEntryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Date currentDate = new Date();
				String entryName = entryText.getText().toString();
				if (entryName.length() == 0){
					entryName = "Untitled entry";
				}
				Entry temp = new Entry(entryName, currentDate.getTime());
				GardenGnome.addEntry(pa_pk, plant, temp);
				adapter.notifyDataSetChanged(); // refresh ListView
				entryText.setText("");
			}
		});
		
		/** Voice Recognition **/
    speakButton = (Button) findViewById(R.id.recordVoiceButton);
    
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

	public void initMockData() {
		//plant.getEntries().clear();
		//GardenGnome.initEntry(pa_pk, plant);
		adapter = new EntryAdapter(this, R.layout.journal_list_item, plant.getEntries());	
		setListAdapter(adapter);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		System.out.println("touched");
		return false;
	}

  /** Handle the click on the start recognition button. */
	@Override
	public void onClick(View v) {
    if (v.getId() == R.id.recordVoiceButton) {
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
					entry.setText(matches.get(0).toString());
      }
      super.onActivityResult(requestCode, resultCode, data);
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
			String dateStr = formatter.format(e.getDate()); // currentDate.toLocaleString();
			((TextView) v.findViewById(R.id.entry_date)).setText(dateStr);

			/*
			((Button) v.findViewById(R.id.delete_journal)).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(PlantScreen.this, EntryScreen.class);
					intent.putExtra("name", name);
					intent.putExtra("garden_id", gardenID);
					intent.putExtra("plot_id", plotID);
					intent.putExtra("plant_id", plantID);
					intent.putExtra("entry_id", position);
					startActivity(intent);
				}
			});
			*/
			
			
			final int entryID = position;
			final Button replaceJournalButton = (Button)v.findViewById(R.id.replace_journal);
			final Button deleteJournalButton = (Button)v.findViewById(R.id.delete_journal);
			final EditText editEntryText = (EditText)v.findViewById(R.id.journal_edittext);
			final TextView entryName = (TextView)v.findViewById(R.id.entry_name); 
			
			/*
			final Button editJournalButton = (Button)v.findViewById(R.id.edit_journal);
			editJournalButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					
					replaceJournalButton.setVisibility(View.VISIBLE);
					deleteJournalButton.setVisibility(View.VISIBLE);
					editEntryText.setVisibility(View.VISIBLE);
					entryName.setVisibility(View.GONE);
					
					editEntryText.setText(entryName.getText().toString());
				}
			});

			editEntryText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus)
						dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			});
			*/
			
			replaceJournalButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					entryName.setText(editEntryText.getText().toString());
					
					replaceJournalButton.setVisibility(View.GONE);
					deleteJournalButton.setVisibility(View.GONE);
					editEntryText.setVisibility(View.GONE);
					entryName.setVisibility(View.VISIBLE);
				}
			});
			
			deleteJournalButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					plant.getEntries().remove(entryID);
					PlantScreen.adapter.notifyDataSetChanged(); 
					
				}
			});
			

			return v;
		}
	}


	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		/*
		Intent intent = new Intent(PlantScreen.this, EntryScreen.class);
		intent.putExtra("name", name);
		intent.putExtra("garden_id", gardenID);
		intent.putExtra("plot_id", plotID);
		intent.putExtra("plant_id", plantID);
		intent.putExtra("entry_id", position);
		intent.putExtra("pa_pk", pa_pk);
		startActivity(intent);
		*/
		
		
		replaceJournalButton = (Button)view.findViewById(R.id.replace_journal);
		deleteJournalButton = (Button)view.findViewById(R.id.delete_journal);
		editEntryText = (EditText)view.findViewById(R.id.journal_edittext);
		entryName = (TextView)view.findViewById(R.id.entry_name); 
		
		replaceJournalButton.setVisibility(View.VISIBLE);
		deleteJournalButton.setVisibility(View.VISIBLE);
		editEntryText.setVisibility(View.VISIBLE);
		entryName.setVisibility(View.GONE);
		
		editEntryText.setText(entryName.getText().toString());
		/*
		final int entryID = position;
		editJournalButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				entryName.setText(editEntryText.getText().toString());
				
				editJournalButton.setVisibility(View.GONE);
				deleteJournalButton.setVisibility(View.GONE);
				editEntryText.setVisibility(View.GONE);
				entryName.setVisibility(View.VISIBLE);
			}
		});
		
		deleteJournalButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				plant.getEntries().remove(entryID);
				PlantScreen.adapter.notifyDataSetChanged(); 
				
			}
		});
		*/
	}
	
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		plant.getEntries().remove(position);
		PlantScreen.adapter.notifyDataSetChanged(); 
		//finish();
		return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.plant_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent; 
		switch (item.getItemId()) {
		case R.id.m_home:
			intent = new Intent(PlantScreen.this, StartScreen.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
		case R.id.m_deleteplant:
			GardenGnome.removePlant(plantID, pa_pk, plot);
			PlotScreen.adapter.notifyDataSetChanged(); 
			finish();
			break;
		case R.id.m_searchplant:
			intent = new Intent(PlantScreen.this, Encyclopedia.class);
			Bundle bundle = new Bundle(1);
			bundle.putString("name", name);

			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.m_renameplant:
			showDialog(0);
			
			//plant.setName(name);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;

	    	
			textEntryView = LayoutInflater.from(this).inflate(R.layout.text_entry_dialog, null);
			builder = new AlertDialog.Builder(this).setView(textEntryView);
	
			dialog = builder.setTitle("Enter new plant name")
			.setPositiveButton("Rename", this)
			.setNegativeButton(R.string.alert_dialog_cancel, null) // this means cancel was pressed
			.create();
	
			// automatically show soft keyboard
			input = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
			input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus)
						dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			});
			
			input.setOnKeyListener(new View.OnKeyListener() {
				@Override public boolean onKey(View view, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
						InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
						mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
						return true;
					}
					return false;
				}
			});    	
		return dialog;
	}
	
	public void onClick(DialogInterface dialog, int whichButton) {
		EditText textEntry = ((EditText) textEntryView.findViewById(R.id.dialog_text_entry));
		String plantName = textEntry.getText().toString().trim();
		if (plantName.length() == 0)
			plantName = "Untitled plant";
		plant.setName(plantName);
		setTitle(plantName);
		PlotScreen.adapter.notifyDataSetChanged(); 
		removeDialog(0);
	}
	
}
