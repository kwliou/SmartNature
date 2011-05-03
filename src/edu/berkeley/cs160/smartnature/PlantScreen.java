package edu.berkeley.cs160.smartnature;

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
	EditText entryText;
	ImageView addImage;
	TextView plantTextView, plantHint;
	Button addEntryButton, deleteEntryButton, searchPlantButton, deletePlantButton;
	int po_pk, pa_pk;
	Garden garden;
	Plot plot;
	Plant plant;
	
	View textEntryView;
	EditText input;
	
	/** Voice Recognition */
	Button speakButton;
	private ListView mList;
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	ArrayList<String> matches = new ArrayList<String>();
	
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

		garden = GardenGnome.getGarden(gardenID);
		plot = garden.getPlot(plotID);
		plant = plot.getPlants().get(plantID);
		
		po_pk = GardenGnome.getPlotPk(gardenID, plot);
		pa_pk = GardenGnome.getPlantPk(po_pk, plant);
	
		initMockData();
		getListView().setOnItemClickListener(PlantScreen.this);

		entryText = (EditText) findViewById(R.id.entryText);
		// addImage = (ImageView) findViewById(R.id.addImage);
		addEntryButton = (Button) findViewById(R.id.addEntryButton);


		plantHint = (TextView)findViewById(R.id.plant_hint);
		plantHint.setText(R.string.hint_plantscreen);

		/*
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
				StartScreen.dh.delete_plant(pa_pk);
				StartScreen.dh.delete_map_pp(pa_pk);
				PlotScreen.adapter.notifyDataSetChanged(); 
				finish();
			}
		});

		if (StartScreen.showHints){
			plantHint.setVisibility(View.VISIBLE);
		}else{
			plantHint.setVisibility(View.GONE);
		}

		*/

		// Button addPicButton = (Button) findViewById(R.id.addPicButton);
		Button addEntryButton = (Button) findViewById(R.id.addEntryButton);
		addEntryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText entry = (EditText) findViewById(R.id.entryText);
				Date currentDate = new Date();
				String dateStr = currentDate.toString();

				String entryName = entryText.getText().toString();
				if (entryName.length() == 0)
					entryName = "Untitled entry";
				
				if (matches.isEmpty()){
					Entry temp = new Entry(entryName, dateStr);
					GardenGnome.addEntry(pa_pk, plant, temp);
				}else{
					Entry temp = new Entry(matches.get(0).toString(), dateStr);
					GardenGnome.addEntry(pa_pk, plant, temp);					
				
				}
				adapter.notifyDataSetChanged(); // refresh ListView
				entry.setText("");

			}
		});
		
		/** Voice Recognition **/
    speakButton = (Button) findViewById(R.id.recordVoiceButton);
    //mList = (ListView) findViewById(R.id.entryList);
    
    PackageManager pm = getPackageManager();
    List<ResolveInfo> activities = pm.queryIntentActivities(
            new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
    if (activities.size() != 0) {
        speakButton.setOnClickListener(this);
    } else {
        speakButton.setEnabled(false);
        speakButton.setText("Recognizer not present");
    }
    
    
	}

	public void initMockData() {
		plant.getEntries().clear();
		GardenGnome.initEntry(pa_pk, plant);
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
		//System.out.println("clicked");
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
          // Fill the list view with the strings the recognizer thought it could have heard
      		matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);  
      		//ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
          //mList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, matches));
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
			((TextView) v.findViewById(R.id.entry_date)).setText(e.getDate());

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

			return v;
		}
	}


	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(PlantScreen.this, EntryScreen.class);
		intent.putExtra("name", name);
		intent.putExtra("garden_id", gardenID);
		intent.putExtra("plot_id", plotID);
		intent.putExtra("plant_id", plantID);
		intent.putExtra("entry_id", position);
		intent.putExtra("pa_pk", pa_pk);
		startActivity(intent);
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
    switch(id) {
	    case 0:
	    	
				textEntryView = LayoutInflater.from(this).inflate(R.layout.text_entry_dialog, null);
				builder = new AlertDialog.Builder(this).setView(textEntryView);
		
				dialog = builder.setTitle("Enter new plant name")
				.setPositiveButton(R.string.alert_dialog_ok, this)
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
	      break;
	      
	    case 1:
	    	/*
				textEntryView = LayoutInflater.from(this).inflate(R.layout.edit_entry_dialog, null);
				builder = new AlertDialog.Builder(this).setView(textEntryView);
		
				dialog = builder.setTitle("Enter new plant name")
				.setPositiveButton(R.string.alert_dialog_ok, this)
				.setNegativeButton(R.string.alert_dialog_cancel, null) // this means cancel was pressed
				.create();
		
				// automatically show soft keyboard
				input = (EditText) textEntryView.findViewById(R.id.journal_text_entry);
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
				*/ 
	        break;
	    default:
	        dialog = null;
    }

    

		
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
