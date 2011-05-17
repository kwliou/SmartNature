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
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class PlantScreen extends ListActivity implements DialogInterface.OnClickListener, View.OnClickListener, AdapterView.OnItemClickListener {
	
	EntryAdapter adapter;
	
	EditText entryText;
	Button addEntryButton;
	Plot plot;
	Plant plant;
	
	AlertDialog dialog;
	View textEntryView;
	int clickedPosition;
	/** Voice Recognition */
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy h:mm a");
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		if (!intent.hasExtra("garden_index")) {
			finish();
			return;
		}
		GardenGnome.init(this);
		Garden garden = GardenGnome.getGarden(intent.getIntExtra("garden_index", 0));
		plot = garden.getPlot(intent.getIntExtra("plot_index", 0));
		plant = plot.getPlant(intent.getIntExtra("plant_index", 0));
		setTitle(plant.getName());	
		
		adapter = new EntryAdapter(this, R.layout.journal_list_item, plant.getEntries());
		setListAdapter(adapter);
		setContentView(R.layout.plant);
		getListView().setOnItemClickListener(this);
		registerForContextMenu(getListView());
		
		entryText = (EditText) findViewById(R.id.entry_body);
		addEntryButton = (Button) findViewById(R.id.btn_add_entry);
		addEntryButton.setOnClickListener(this);
		
		/** Voice Recognition **/
		Button speakButton = (Button) findViewById(R.id.btn_record_voice);
		
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0)
			speakButton.setOnClickListener(this);
		else
			speakButton.setVisibility(View.GONE);
	}
	
	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btn_record_voice) {
			startVoiceRecognitionActivity();
			return;
		}
		if (addEntryButton.getText().equals("Post")) {
			String entryName = entryText.getText().toString().trim();
			if (entryName.length() == 0)
				entryName = "Untitled entry";
			Entry entry = new Entry(entryName, new Date().getTime());
			GardenGnome.addEntry(plant, entry);
			adapter.notifyDataSetChanged(); // refresh ListView
			entryText.setText("");			
		}
		else {
			Entry entry = plant.getEntry(clickedPosition); 
			entry.setBody(entryText.getText().toString());
			GardenGnome.updateEntry(entry);
			entryText.setText("");
			entryText.setHint("Add journal entry");
			addEntryButton.setText("Post");
			addEntryButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_menu_compose, 0, 0);
		}
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(addEntryButton.getWindowToken(), 0);
	}
	
	/** Fire an intent to start the speech recognition activity. */
	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition");
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}
	
	/** Handle the results from the recognition activity. */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			EditText entry = (EditText) findViewById(R.id.entry_body);
			entry.setText(matches.get(0).toString());
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		clickedPosition = position;
		entryText.setText(plant.getEntry(position).getBody());
		entryText.setHint("Edit journal entry");
		addEntryButton.setText("Edit");
		addEntryButton.setCompoundDrawablesWithIntrinsicBounds(0, android.R.drawable.ic_menu_edit, 0, 0);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		menu.setHeaderTitle("Entry options");
		menu.add(Menu.NONE, 0, Menu.NONE, "Delete");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case 0:
				GardenGnome.removeEntry(plant, plant.getEntry(info.position));
				adapter.notifyDataSetChanged();
				break;
		}
		return super.onContextItemSelected(item);
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
				intent = new Intent(this, StartScreen.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;
			case R.id.m_deleteplant:
				GardenGnome.removePlant(plot, plant);
				PlotScreen.adapter.notifyDataSetChanged();
				finish();
				break;
			case R.id.m_searchplant:
				intent = new Intent(this, Encyclopedia.class);
				intent.setAction(Intent.ACTION_SEARCH);
				intent.putExtra(android.app.SearchManager.QUERY, plant.getName());
				startActivity(intent);
				break;
			case R.id.m_renameplant:
				showDialog(0);
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
			.setNegativeButton(R.string.alert_dialog_cancel, null)
			.create();
		
		// automatically show soft keyboard
		EditText input = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
		input.setText(plant.getName());
		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override public void onFocusChange(View v, boolean hasFocus) {
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
		GardenGnome.updatePlant(plant);
		setTitle(plantName);
		PlotScreen.adapter.notifyDataSetChanged();
		removeDialog(0);
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
			View view = convertView;
			if (view == null)
				view = li.inflate(R.layout.journal_list_item, null);
			Entry e = items.get(position);
			((TextView) view.findViewById(R.id.entry_name)).setText(e.getBody());
			String dateStr = formatter.format(e.getDate()); // currentDate.toLocaleString();
			((TextView) view.findViewById(R.id.entry_date)).setText(dateStr);
			
			return view;
		}
	}
}
