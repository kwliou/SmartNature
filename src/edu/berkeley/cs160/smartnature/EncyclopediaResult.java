package edu.berkeley.cs160.smartnature;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EncyclopediaResult extends Activity implements View.OnClickListener{

	String name = "";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encycl_result);
        
        
    		Bundle extras = getIntent().getExtras();
    		if (extras != null && extras.containsKey("name")) {
    			name = extras.getString("name");
    			setTitle(name);
    		
    		
	    		String plantURL = extras.getString("linkURL");
	    		LinearLayout details = (LinearLayout) findViewById(R.id.plantDescription);
	    		
	    		details.removeAllViews();
	    		
	    		TextView plantName = (TextView)findViewById(R.id.searchName);
	    		plantName.setText(name);
	    		
	    		
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
	    				detail.setText(category + " " + value);
	    				detail.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	    				//Toast.makeText(Encyclopedia.this, category + " " + value, Toast.LENGTH_SHORT).show();
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
			/*GardenGnome.gardens.get(gardenId).getPlot(plotId).addPlant(new Plant(pName));
			int po_pk;
			List<Integer> temp = StartScreen.dh.select_map_gp_po(gardenId + 1);
			po_pk = -1;
			for(int i = 0; i < temp.size(); i++) {
				if(po_pk != -1) 
					break;
				if(GardenGnome.gardens.get(gardenId).getPlot(plotId).getName().equalsIgnoreCase(StartScreen.dh.select_plot_name(temp.get(i).intValue())))
					po_pk = temp.get(i);
			}
			StartScreen.dh.insert_plant(pName, 0);
			StartScreen.dh.insert_map_pp(po_pk, StartScreen.dh.count_plant());*/
		}
	}

	@Override
	public void onClick(View v) {
		startActivityForResult(new Intent(EncyclopediaResult.this, AddPlant.class), 0);
		
		
	}
}

