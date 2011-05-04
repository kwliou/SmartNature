package edu.berkeley.cs160.smartnature;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class FindGarden extends ListActivity implements AdapterView.OnItemClickListener, DialogInterface.OnClickListener, View.OnKeyListener {
	
	static private int ID = 0, NAME = 1, CITY = 2, STATE = 3, PUBLIC = 4;
	static StubAdapter adapter;
	ArrayList<String[]> stubs = new ArrayList<String[]>();
	Gson gson = new Gson();
	View textEntryView;
	
	@Override @SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); // Window.FEATURE_PROGRESS
		Object previousData = getLastNonConfigurationInstance(); 
		if (previousData != null)
			stubs = (ArrayList<String[]>) previousData;
		setContentView(R.layout.find_garden);
		adapter = new StubAdapter(this, R.layout.findgarden_list_item, stubs);
		setListAdapter(adapter);
		getListView().setOnItemClickListener(this);

		findViewById(R.id.search_garden_name).setOnKeyListener(new View.OnKeyListener() {
			@Override public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					findViewById(R.id.search_garden_city).requestFocus();
					return true;
				}
				return false;
			}
		});
		
		findViewById(R.id.search_garden_city).setOnKeyListener(new View.OnKeyListener() {
			@Override public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					findViewById(R.id.search_garden_state).requestFocus();
					return true;
				}
				return false;
			}
		});

		findViewById(R.id.search_garden_state).setOnKeyListener(new View.OnKeyListener() {
			@Override public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});
		
		if (previousData == null) {
			setProgressBarIndeterminateVisibility(true);
			new Thread(getStubs).start();
		}
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return stubs.isEmpty() ? null : stubs;
	}
	
	Runnable getStubs = new Runnable() {
		@Override
		public void run() {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(getString(R.string.server_url) + "stubs");
			boolean success = true;
			String[][] results = null;
			try {
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				String result = EntityUtils.toString(entity);
				results = gson.fromJson(result, String[][].class);
			} catch (Exception e) { success = false; e.printStackTrace(); }
			
			if (success) {
				stubs.addAll(new ArrayList<String[]>(java.util.Arrays.asList(results)));
				runOnUiThread(new Runnable() {
					@Override public void run() { adapter.notifyDataSetChanged(); }
				});
				/*for (int i = 0; i < results.length; i++) {
					stubs.add(results[i]);
					runOnUiThread(new Runnable() {
						@Override public void run() { adapter.notifyDataSetChanged(); }
					});
				}*/
			} else {
				runOnUiThread(new Runnable() {
					@Override public void run() {
						findViewById(R.id.find_garden_msg).setVisibility(View.VISIBLE);
					}
				});
			}
			
			runOnUiThread(new Runnable() {
				@Override public void run() { setProgressBarIndeterminateVisibility(false); }
			});
		}
	};
	
	public Garden getGarden(String serverId) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(getString(R.string.server_url) + "gardens/" + serverId + ".json");
		String result = "";
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);
		} catch (Exception e) { e.printStackTrace(); }
		
		System.out.println("garden_json=" + result);
		Garden garden = gson.fromJson(result, Garden.class);
		System.out.println("garden_id=" + garden.getServerId());
		getPlots(garden);
		
		return garden;
	}
	
	public void getPlots(Garden garden) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(getString(R.string.server_url) + "gardens/" + garden.getServerId() + "/plots.json");
		String result = "";
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);
		} catch (Exception e) { e.printStackTrace(); }
		System.out.println("plots_json=" + result);
		Plot[] plots = gson.fromJson(result, Plot[].class);
		
		for (Plot plot : plots) {
			plot.postDownload();
			garden.addPlot(plot);
			getPlants(plot);
		}
	}
	
	public void getPlants(Plot plot) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(getString(R.string.server_url) + "plots/" + plot.getServerId() + "/plants.json");
		String result = "";
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);
		} catch (Exception e) { e.printStackTrace(); }
		System.out.println("plants_json=" + result);
		Plant[] plants = gson.fromJson(result, Plant[].class);
		
		for (Plant plant : plants) {
			plot.addPlant(plant);
			getEntries(plant);
		}
	}
	
	public void getEntries(Plant plant) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(getString(R.string.server_url) + "plants/" + plant.getServerId() + "/journals.json");
		String result = "";
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);
		} catch (Exception e) { e.printStackTrace(); }
		System.out.println("journals_json=" + result);
		Entry[] entries = gson.fromJson(result, Entry[].class);
		plant.setEntries(new ArrayList<Entry>());
		System.out.println("plant_entries?=" + Boolean.toString(plant.getEntries() != null));
		System.out.println("plant_entries_size=" + plant.getEntries().size());
		System.out.println("entries_size=" + entries.length);
		System.out.println("entry=" + entries[0].getServerId() + "," + entries[0].getName() + "," + entries[0].getDate());
		
		for (Entry entry : entries) {
			plant.addEntry(entry);
		}
	}
	
	@Override public boolean onKey(View view, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (view.getId()) {
				//case R.id.search_garden_name
			}
		}
		return false;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String[] stub = stubs.get(position);
		GardenGnome.addGarden(getGarden(stub[ID]));
		/*if (Boolean.parseBoolean(stub[PUBLIC]))
			startActivityForResult(new Intent(this, GardenAttr.class), 0);
		else {
			showDialog(0);
		}*/
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		textEntryView = LayoutInflater.from(this).inflate(R.layout.text_entry_dialog, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(textEntryView);
		
		final Dialog dialog = builder.setTitle("Enter garden password")
		.setPositiveButton(R.string.alert_dialog_ok, this)
		.setNegativeButton(R.string.alert_dialog_cancel, null) // this means cancel was pressed
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
		//String password = textEntry.getText().toString();
		//Intent intent = new Intent(this, GardenAttr.class);
		removeDialog(0);
	}
	
	class StubAdapter extends ArrayAdapter<String[]> {
		
		private ArrayList<String[]> stubs;
		private LayoutInflater li;
		
		public StubAdapter(Context context, int textViewResourceId, ArrayList<String[]> items) {
			super(context, textViewResourceId, items);
			li = ((ListActivity) context).getLayoutInflater();
			stubs = items;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null)
				view = li.inflate(R.layout.findgarden_list_item, null);
			String[] stub = stubs.get(position);
			((TextView) view.findViewById(R.id.garden_name)).setText(stub[NAME]);
			((TextView) view.findViewById(R.id.garden_info)).setText(stub[CITY] + ", " + stub[STATE]);
			return view;
		}
	}

}