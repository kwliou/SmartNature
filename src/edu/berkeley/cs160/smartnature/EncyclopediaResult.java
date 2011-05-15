package edu.berkeley.cs160.smartnature;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EncyclopediaResult extends Activity implements Runnable, DialogInterface.OnCancelListener, View.OnClickListener {
	ProgressDialog throbber;
	String plantName = "";
	Elements tableValues;
	String plantURL;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.encycl_result);
		
		Intent intent = getIntent();
		if (intent.hasExtra("name")) {
			plantName = intent.getStringExtra("name");
			setTitle(plantName);
			plantURL = intent.getStringExtra("linkURL");
			((TextView) findViewById(R.id.searchName)).setText(plantName);
			Button addToPlot = (Button) findViewById(R.id.addToPlot);
			addToPlot.setOnClickListener(this);
			throbber = ProgressDialog.show(this, null, "Downloading entry...", true, true, this);
			new Thread(this).start();
		}
	}
	
	public void loadInfo() {
		LinearLayout details = (LinearLayout) findViewById(R.id.plantDescription);
		details.removeAllViews();
		
		for (int i = 0; i < tableValues.size(); i++) {
			Element e = tableValues.get(i);
			String category = e.child(0).text();
			String value = e.child(1).text();
			
			TextView detail = new TextView(this);
			TextView categ = new TextView(this);
			
			categ.setText(category);
			categ.setTypeface(Typeface.DEFAULT_BOLD, 0);
			categ.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			categ.setPadding(10, 0, 0, 0);
			details.addView(categ);
			
			detail.setText(value);
			detail.setTextSize(15);
			detail.setPadding(20, 0, 5, 0);
			detail.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			details.addView(detail);
		}
		
		throbber.dismiss();
	}
	
	public void searchFailed() {
		runOnUiThread(new Runnable() {
			@Override public void run() {
				throbber.dismiss();
				findViewById(R.id.search_result_msg).setVisibility(View.VISIBLE);
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (data != null) {
			int plotIndex, gardenIndex;
			gardenIndex = data.getIntExtra("garden_index", 0);
			plotIndex = data.getIntExtra("plot_index", 0);
			Plot plot = GardenGnome.getGarden(gardenIndex).getPlot(plotIndex);
			Plant plant = new Plant(plantName);
			GardenGnome.addPlant(plot, plant);
			if (PlotScreen.adapter != null)
				PlotScreen.adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void run() {
		Document doc = null;
		int i, tries = 20;
		for (i = 0; i < tries; i++) {
			try {
				doc = Jsoup.connect(plantURL).get();
				break;
			} catch (IOException e) { e.printStackTrace(); }
		}
		if (i == tries) {
			searchFailed(); 
			return;
		}
		Elements tables = doc.getElementsByTag("table");
		Element table = tables.get(0);
		tableValues = table.children();
		runOnUiThread(new Runnable() {
			@Override public void run() { loadInfo(); }
		});
		
	}
	
	@Override
	public void onClick(View view) {
		showDialog(0);
	}
	
	int gardenIndex;
	Garden chosenGarden;
	
	@Override
	public Dialog onCreateDialog(int id) {
		DialogInterface.OnClickListener choseGarden = new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int whichButton) {
				gardenIndex = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
				chosenGarden = GardenGnome.getGarden(gardenIndex);
				removeDialog(1);
				showDialog(1);
			}
		};
		
		DialogInterface.OnClickListener chosePlot = new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int whichButton) {
				int plotIndex = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
				Plot plot = chosenGarden.getPlot(plotIndex);
				Intent intent = new Intent(EncyclopediaResult.this, PlantScreen.class);
				intent.putExtra("garden_index", gardenIndex);
				intent.putExtra("plot_index", plotIndex);
				intent.putExtra("plant_index", plot.numPlants());
				GardenGnome.addPlant(plot, new Plant(plantName));
				startActivity(intent);
			}
		};
		
		if (id == 0) {
			String[] names = new String[GardenGnome.numGardens()];
			for (int i = 0; i < GardenGnome.numGardens(); i++)
				names[i] = GardenGnome.getGarden(i).getName();
			return new AlertDialog.Builder(this)
				.setTitle("Choose garden")
				.setSingleChoiceItems(names, 0, null)
				.setPositiveButton("Next", choseGarden)
				.setNegativeButton(R.string.alert_dialog_cancel, null) // this means cancel was pressed
				.create();
		}
		
		String[] names = new String[chosenGarden.numPlots()];
		for (int i = 0; i < chosenGarden.numPlots(); i++)
			names[i] = chosenGarden.getPlot(i).getName();
		
		return new AlertDialog.Builder(this)
			.setTitle("Choose plot")
			.setSingleChoiceItems(names, 0, null)
			.setPositiveButton(R.string.alert_dialog_ok, chosePlot)
			.setNegativeButton(R.string.alert_dialog_cancel, null) // this means cancel was pressed
			.create();
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		finish();
	}
}
