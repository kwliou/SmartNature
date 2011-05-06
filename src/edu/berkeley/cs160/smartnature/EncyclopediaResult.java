package edu.berkeley.cs160.smartnature;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
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
	String pName = "";
	Elements tableValues;
	String plantURL;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.encycl_result);
		
		Intent intent = getIntent();
		if (intent.hasExtra("name")) {
			pName = intent.getStringExtra("name");
			setTitle(pName);
			plantURL = intent.getStringExtra("linkURL");
			((TextView) findViewById(R.id.searchName)).setText(pName);
			Button addToPlot = (Button) findViewById(R.id.addToPlot);
			addToPlot.setOnClickListener(this);
			throbber = ProgressDialog.show(this, "Please wait", "Downloading entry...", true, true, this);
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
			int plotId, gardenId;
			gardenId = data.getIntExtra("gardenId", 0);
			plotId = data.getIntExtra("plotId", 0);
			// int po_pk = GardenGnome.getPlotPk(gardenId,
			// GardenGnome.getPlot(gardenId, plotId));
			Plot plot = GardenGnome.getGarden(gardenId).getPlot(plotId);
			Plant plant = new Plant(pName);
			GardenGnome.addPlant(plot, plant);
			//GardenGnome.addPlant(po_pk, pName, GardenGnome.getGardens().get(gardenId).getPlot(plotId));
			PlotScreen.adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onClick(View view) {
		startActivityForResult(new Intent(this, AddPlant.class), 0);
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
	public void onCancel(DialogInterface dialog) {
		finish();
	}
}
