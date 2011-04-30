package edu.berkeley.cs160.smartnature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

class GardenGnome extends Application {
	static ArrayList<Garden> gardens = new ArrayList<Garden>();
}

public class StartScreen extends ListActivity implements DialogInterface.OnClickListener, View.OnClickListener, AdapterView.OnItemClickListener {

	GardenAdapter adapter;
	ArrayList<Garden> gardens;
	AlertDialog dialog;
	View textEntryView;
	public static DatabaseHelper dh;

	LocationManager lm;
	Geocoder geocoder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		geocoder = new Geocoder(this, Locale.getDefault());
		gardens = GardenGnome.gardens;

		dh = new DatabaseHelper(this);
		initAll();
		//if(gardens.isEmpty())
		//	initMockData();
		adapter = new GardenAdapter(this, R.layout.garden_list_item, gardens);
		setListAdapter(adapter);

		getListView().setOnItemClickListener(this);
		findViewById(R.id.new_garden).setOnClickListener(this);
		findViewById(R.id.search_encyclopedia).setOnClickListener(this);
		findViewById(R.id.find_garden).setOnClickListener(this);
	}

	public void initAll() {
		gardens.clear();
		for(int i = 0; i < dh.count_exist_garden(); i++) {
			gardens.add(dh.select_garden(i + 1));
			List<Integer> temp = dh.select_map_gp_po(i + 1);
			for(int j = 0; j < temp.size(); j++)
				gardens.get(i).addPlot(dh.select_plot(temp.get(j)));
		}
	}
	
	public void initMockData() {
		/*
		Garden g1 = new Garden("Berkeley Youth Alternatives");	
		Garden g2 = new Garden("Karl Linn");
		g1.setCity("Berkeley"); g1.setState("California");
		g2.setCity("Berkeley"); g2.setState("California");

		Rect bounds1 = new Rect(40, 60, 90, 200);
		Rect bounds2 = new Rect(140, 120, 210, 190);
		Rect bounds3 = new Rect(270, 120, 270 + 90, 120 + 100);
		Rect bounds4 = new Rect(40, 200, 90, 300);
		Rect bounds5 = new Rect(140, 50, 210, 190);
		Rect bounds6 = new Rect(270, 120, 270 + 90, 120 + 140);

		float[] pts = { 0, 0, 50, 10, 90, 100 };
		float[] pts2 = { 0, 0, 50, 10, 90, 100, 70, 140, 60, 120 };

		g1.addPlot("Jerry's Plot", bounds1, 10, Plot.RECT);
		g1.addPlot("Amy's Plot", bounds2, 0, Plot.OVAL);
		g1.addPlot("Shared Plot", bounds3, 0, pts);
		g2.addPlot("Cyndi's Plot", bounds4, 0, Plot.RECT);
		g2.addPlot("Alex's Plot", bounds5, 10, Plot.OVAL);
		g2.addPlot("Flowers", bounds6, 0, pts2);

		gardens.add(g1);
		gardens.add(g2);
		*/
		Log.w("debug", "initMockData called");
		dh.insert_garden("Berkeley Youth Alternatives", R.drawable.preview, "0,0,800,480", "Berkeley", "California", 0, -1, "");
		dh.insert_garden("Karl Linn", R.drawable.preview, "0,0,800,480", "Berkeley", "California", 0, -1, "");

		dh.insert_plot("Jerry Plot", "40,60,90,200," + Color.BLACK, Plot.RECT, Color.BLACK, "", 10, 0);
		dh.insert_plot("Amy Plot", "140,120,210,190," + Color.BLACK, Plot.OVAL, Color.BLACK, "", 0, 0);
		dh.insert_plot("Shared Plot", "270,120,360,220," + Color.BLACK, Plot.POLY, Color.BLACK, "0,0,50,10,90,100", 0, 0);
		dh.insert_plot("Cyndi Plot", "40,200,90,300," + Color.BLACK, Plot.RECT, Color.BLACK, "", 0, 0);
		dh.insert_plot("Alex Plot", "140,50,210,190," + Color.BLACK, Plot.OVAL, Color.BLACK, "", 10, 0);
		dh.insert_plot("Flowers", "270,120,360,260," + Color.BLACK, Plot.POLY, Color.BLACK, " 0,0,50,10,90,100,70,140,60,120", 0, 0);

		Garden g1 = dh.select_garden(1);
		Garden g2 = dh.select_garden(2);

		for(int i = 1; i < 4; i++) {
			g1.addPlot(dh.select_plot(i));
			dh.insert_map_gp(1, i);
		}
		
		for(int i = 4; i < 7; i++) {
			g2.addPlot(dh.select_plot(i));
			dh.insert_map_gp(2, i);
		}

		gardens.add(g1);
		gardens.add(g2);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.new_garden:
			showDialog(0);
			break;
		case R.id.search_encyclopedia:
			startActivityForResult(new Intent(this, Encyclopedia.class), 0);
			break;
		case R.id.find_garden:
			startActivityForResult(new Intent(this, FindGarden.class), 0);
			break;
		}
	}

	@Override
	public Dialog onCreateDialog(int id) {
		textEntryView = LayoutInflater.from(this).inflate(R.layout.text_entry_dialog, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(textEntryView);

		dialog = builder.setTitle(R.string.new_garden_prompt)
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
		String gardenName = textEntry.getText().toString();
		if (gardenName.length() == 0)
			gardenName = "Untitled garden";
		Intent intent = new Intent(this, GardenScreen.class);
		intent.putExtra("garden_id", gardens.size());
		Garden garden = new Garden(gardenName);
		gardens.add(garden);
		dh.insert_garden(garden.getName(), R.drawable.preview, "0,0,800,480", "", "", 0, -1, "");
		adapter.notifyDataSetChanged();
		startActivityForResult(intent, 0);
		new Thread(setLocation).start();
		removeDialog(0);
	}
	
	/** sets garden location using user's physical location */
	Runnable setLocation = new Runnable() {
		@Override
		public void run() {
			Garden garden = gardens.get(gardens.size() - 1);
			List<String> providers = lm.getProviders(false);
			if (!providers.isEmpty()) {
				Location loc = lm.getLastKnownLocation(providers.get(0));
				List<Address> addresses = new ArrayList<Address>();
				try {
					addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
				} catch (IOException e) { e.printStackTrace(); }
				if (!addresses.isEmpty()) {
					Address addr = addresses.get(0);
					System.out.println(addr.getLocality() + "," + addr.getAdminArea() + "," + addr.getCountryCode());
					garden.setCity(addr.getLocality());
					if (addr.getAdminArea() != null)
						garden.setState(addr.getAdminArea());
					else
						garden.setState(addr.getCountryCode());
				}
			}
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, GardenScreen.class);
		intent.putExtra("garden_id", position);
		startActivityForResult(intent, 0);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.m_contact:
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			intent.setData(Uri.parse("mailto:" + getString(R.string.dev_email)));
			intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "GardenGnome feedback");
			startActivity(intent); //startActivity(Intent.createChooser(intent, "Send mail..."));
			break;
		case R.id.m_globaloptions:
			startActivity(new Intent(this, GlobalSettings.class));
			break;
		case R.id.m_help:
		}
		return super.onOptionsItemSelected(item);
	}

	class GardenAdapter extends ArrayAdapter<Garden> {
		private ArrayList<Garden> gardens;
		private LayoutInflater li;

		public GardenAdapter(Context context, int textViewResourceId, ArrayList<Garden> items) {
			super(context, textViewResourceId, items);
			li = ((ListActivity) context).getLayoutInflater();
			gardens = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null)
				view = li.inflate(R.layout.garden_list_item, null);
			Garden garden = gardens.get(position);
			((TextView) view.findViewById(R.id.garden_name)).setText(garden.getName());
			ImageView image = (ImageView) view.findViewById(R.id.preview_img); 
			if (garden.getImages().isEmpty())
				image.setImageResource(R.drawable.preview);
			else
				image.setImageURI(garden.getPreview());
			return view;
		}
	}
}
