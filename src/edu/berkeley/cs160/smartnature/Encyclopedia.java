package edu.berkeley.cs160.smartnature;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class Encyclopedia extends ListActivity implements View.OnClickListener, AdapterView.OnItemClickListener{
	
	static ResultAdapter adapter;

	boolean searchScreen = true;

	String pName = "";
	String name = "";
	EditText search;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    		Bundle extras = getIntent().getExtras();
    		if (extras != null && extras.containsKey("name")) {
    			name = extras.getString("name");
    			setTitle(name);
    		}
    		
    		
        setContentView(R.layout.encycl);
        
        
        getListView().setOnItemClickListener(this);
		
		Button searchButton = (Button) findViewById(R.id.searchButton);
		search = (EditText)findViewById(R.id.searchText);
		search.setText(name);
		
		searchButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick (View v) {
        		if(!searchScreen){
        			ViewFlipper vf = (ViewFlipper) findViewById(R.id.ViewFlipper01);
        			vf.showNext();
        			searchScreen = true;
        		}
        		EditText search = (EditText)findViewById(R.id.searchText);

        		if(!search.getText().toString().equals("")){
	        		String searchURL = "http://www.plantcare.com/encyclopedia/search.aspx?q=" + search.getText().toString();
	        		//LinearLayout content = (LinearLayout) findViewById(R.id.content);
					//content.removeAllViews();
	        		
	        		try {
	        			
	        			ArrayList <SearchResult> resultList = new ArrayList <SearchResult> ();
						Document doc = Jsoup.connect(searchURL).get();
						Element resultBox = doc.getElementById("searchEncyclopedia");
						if(resultBox.child(1).attr("id").equals("_ctl0_mainHolder_noresults")){
							//Toast.makeText(Encyclopedia.this, "Sorry, no results were found.", Toast.LENGTH_SHORT).show();
							resultList.add(new SearchResult ("No results found", "Please try refining your search", "", ""));
						}
						else{
							final Elements results = resultBox.child(1).children();
						
							int numResults = results.size();
							for(int i = 0; i < numResults; i++){
								
								final Element next = results.first();
								String plantURL = "http://www.plantcare.com" + next.child(0).child(0).child(0).attr("src");
								String name = next.child(1).text();
								String altNames = next.child(2).text();
								String linkURL = "http://www.plantcare.com/encyclopedia/" + next.child(1).attr("href");
								resultList.add(new SearchResult (name, altNames, plantURL, linkURL));
								
								results.remove(0);
								
							}
							//ListView listView = (ListView) findViewById(R.id.searchList);
		
							
							
						}
						adapter = new ResultAdapter(Encyclopedia.this, R.layout.search_item, resultList);
						setListAdapter(adapter);
						
						} catch (IOException e) {
						
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
        		else{
        			Toast.makeText(Encyclopedia.this, "Please enter the plant you are looking for.", Toast.LENGTH_SHORT).show();
        		}
				//Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(searchURL));
        		//startActivity(browserIntent);
        		

        	
        	}

			
		});
        
    }
    
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(searchScreen){	
			searchScreen = false;
			ViewFlipper vf = (ViewFlipper) findViewById(R.id.ViewFlipper01);
			vf.showNext();
		}
		String plantURL = ((TextView)arg1.findViewById(R.id.linkURL)).getText().toString();
		LinearLayout details = (LinearLayout) findViewById(R.id.plantDescription);
		pName = ((TextView)arg1.findViewById(R.id.name)).getText().toString();
		
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
				TextView detail = new TextView(Encyclopedia.this);
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
		addToPlot.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(Encyclopedia.this, AddPlant.class), 0);
				
			}
		});
		//Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(plantURL));
		//startActivity(browserIntent);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (data != null){
			int plotId, gardenId;
			plotId = (Integer) data.getExtras().get("plotId");
			gardenId = (Integer) data.getExtras().get("gardenId");
			GardenGnome.gardens.get(gardenId).getPlot(plotId).addPlant(new Plant(pName));
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
			StartScreen.dh.insert_map_pp(po_pk, StartScreen.dh.count_plant());
		}
	}

	@Override
	public void onClick(View arg0) {
		
		String plantURL = ((TextView)arg0.findViewById(R.id.linkURL)).getText().toString();
		Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(plantURL));
		startActivity(browserIntent);
		
	}
	
    class ResultAdapter extends ArrayAdapter<SearchResult> {
		private ArrayList<SearchResult> items;
		private LayoutInflater li;
		
		public ResultAdapter(Context context, int textViewResourceId, ArrayList<SearchResult> items) {
			super(context, textViewResourceId, items);
			li = ((ListActivity) context).getLayoutInflater();
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null)
				v = li.inflate(R.layout.search_item, null);
			SearchResult s = items.get(position);
			((TextView) v.findViewById(R.id.name)).setText(s.getName());
			((TextView) v.findViewById(R.id.altNames)).setText(s.getAltNames());
			if(!s.getLinkURL().equals("")){
				((TextView) v.findViewById(R.id.linkURL)).setText(s.getLinkURL());
				try {
					((ImageView) v.findViewById(R.id.searchPic)).setImageBitmap(BitmapFactory.decodeStream((new URL(s.getPicURL())).openConnection().getInputStream()));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			return v;
		}
	}


}