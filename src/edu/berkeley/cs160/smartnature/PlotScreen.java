package edu.berkeley.cs160.smartnature;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ZoomControls;

public class PlotScreen extends ListActivity implements View.OnTouchListener, View.OnClickListener, AdapterView.OnItemClickListener {
	
	static ArrayList<Plant> plants = new ArrayList<Plant>();
	LinearLayout plantTextLayout;
	TextView text, plotTitle;
	ListView plantListView;
	Button addPlantButton, backButton;
	AlertDialog dialog;
	
	static PlantAdapter adapter;
	int gardenID, plotID; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("name")) {
			setTitle(extras.getString("name"));
			gardenID = extras.getInt("gardenID");
			plotID = extras.getInt("plotID");
		} else {
			showDialog(0);
		}
		
		setContentView(R.layout.plot);
		initMockData();
		getListView().setOnItemClickListener(PlotScreen.this);
		
		plantTextLayout = (LinearLayout) findViewById(R.id.plantTextLayout);	
		plotTitle = (TextView) findViewById(R.id.plotTextView);
		plotTitle.setText(extras.getString("name"));
		addPlantButton = (Button) findViewById(R.id.addPlantButton);
		addPlantButton.setOnClickListener(new OnClickListener() {
				@Override
        public void onClick(View v) {
  				showDialog(0);
        }
    });
		backButton = (Button) findViewById(R.id.backButton);
		backButton.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
        	//setContentView(R.layout.garden);
        	finish();
        }
    });
	}
	
	public void initMockData() {
		plants.add(new Plant("Carrot"));
		plants.add(new Plant("Tomato"));
		adapter = new PlantAdapter(this, R.layout.list_item, StartScreen.gardens.get(gardenID).getPlots().get(plotID).getPlants());
		setListAdapter(adapter);

	}
	
	public void loadPlants(){
		for (final Plant p: StartScreen.gardens.get(gardenID).getPlots().get(plotID).getPlants()) {
			text = new TextView(PlotScreen.this);
			text.setTextColor(0xFF000000); //black
			text.setText(p.getName());
			final String name = p.getName();
			
			text.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(PlotScreen.this, PlantScreen.class);
					Bundle bundle = new Bundle(4);
					bundle.putString("name", name.toString());
					bundle.putInt("gardenID", gardenID);
					bundle.putInt("plotID", plotID);
					bundle.putInt("plantID", StartScreen.gardens.get(gardenID).getPlots().get(plotID).getPlants().indexOf(p));
					
					intent.putExtras(bundle);
					startActivity(intent);
				}		
			});	
			
			plantTextLayout.addView(text);
			plantListView.addFooterView(text);
		}
	}

	@Override
	public Dialog onCreateDialog(int id) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.text_entry_dialog, null);
		DialogInterface.OnClickListener confirmed = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				EditText plantName = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
				StartScreen.gardens.get(gardenID).getPlots().get(plotID).addPlant( new Plant(plantName.getText().toString()) );
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.garden_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.m_home:
				finish();
				break;
			case R.id.m_resetzoom:
				break;
			case R.id.m_share:
				startActivity(new Intent(this, ShareGarden.class));
				break;
			case R.id.m_showlabels:			
				break;
		}
		return super.onOptionsItemSelected(item);
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

	
	class PlantAdapter extends ArrayAdapter<Plant> {

		private ArrayList<Plant> items;
		private LayoutInflater li;
		
		public PlantAdapter(Context context, int textViewResourceId, ArrayList<Plant> items) {
			super(context, textViewResourceId, items);
			li = ((ListActivity) context).getLayoutInflater();
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null)
				v = li.inflate(R.layout.plant_list, null);
			Plant p = items.get(position);
			((TextView) v.findViewById(R.id.plant_name)).setText(p.getName());
			return v;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(PlotScreen.this, PlantScreen.class);
		Bundle bundle = new Bundle(4);
		bundle.putString("name", StartScreen.gardens.get(gardenID).getPlots().get(plotID).getPlants().get(position).getName());
		bundle.putInt("gardenID", gardenID);
		bundle.putInt("plotID", plotID);
		bundle.putInt("plantID", position);
		
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
}
