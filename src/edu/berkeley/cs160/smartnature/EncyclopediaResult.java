package edu.berkeley.cs160.smartnature;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EncyclopediaResult extends Activity implements View.OnClickListener{

	String pName = "";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encycl_result);
        
        
    		Bundle extras = getIntent().getExtras();
    		if (extras != null && extras.containsKey("name")) {
    			pName = extras.getString("name");
    			setTitle(pName);
    		
    		
	    		String plantURL = extras.getString("linkURL");
	    		LinearLayout details = (LinearLayout) findViewById(R.id.plantDescription);
	    		
	    		details.removeAllViews();
	    		
	    		TextView plantName = (TextView)findViewById(R.id.searchName);
	    		plantName.setText(pName);
	    		
	    		
	    		Document doc;
	    		try {
	    			doc = Jsoup.connect(plantURL).get();
	    			Elements tables = doc.getElementsByTag("table");
	    			Element table = tables.get(0);
	    			Elements tableValues = table.children();
	    			for(int i = 0; i < tableValues.size(); i++){
	    				Element e = tableValues.get(i);
	    				String category = e.child(0).text();
	    				String value = e.child(1).text();
	    				
	    				TextView detail = new TextView(EncyclopediaResult.this);
	    				TextView categ = new TextView(EncyclopediaResult.this);
	    				
	    				categ.setText(category);
	    				categ.setTypeface(Typeface.DEFAULT_BOLD, 0);
	    				categ.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	    				categ.setPadding(10, 0, 0, 0);
	    				details.addView(categ);
	    				
	    				detail.setText(value);
	    				detail.setPadding(20, 0, 0, 0);
	    				detail.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	    				details.addView(detail);
	    			}
	    		} catch (IOException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
	    		
	    		Button addToPlot = (Button)findViewById(R.id.addToPlot);
	    		addToPlot.setOnClickListener(this);
    		}

	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (data != null){
			int plotId, gardenId;
			plotId = (Integer) data.getExtras().get("plotId");
			gardenId = (Integer) data.getExtras().get("gardenId");
			int po_pk = GardenGnome.getPlotPk(gardenId, GardenGnome.getPlot(gardenId, plotId));
			GardenGnome.addPlant(po_pk, pName, GardenGnome.getGardens().get(gardenId).getPlot(plotId));
			PlotScreen.adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {
		startActivityForResult(new Intent(EncyclopediaResult.this, AddPlant.class), 0);
		
		
	}
}

