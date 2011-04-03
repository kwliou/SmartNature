package edu.berkeley.cs160.smartnature;

import java.io.IOException;

import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Encyclopedia extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encycl);
		
		Button searchButton = (Button) findViewById(R.id.searchButton);
		
		searchButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick (View v) {
        		EditText search = (EditText)findViewById(R.id.searchText);
        		String searchURL = "http://www.plantcare.com/encyclopedia/search.aspx?q=" + search.getText().toString();
        		LinearLayout content = (LinearLayout) findViewById(R.id.content);
				content.removeAllViews();
        		
        		try {
        			
        			
					Document doc = Jsoup.connect(searchURL).get();
					Element resultBox = doc.getElementById("searchEncyclopedia");
					//if(resultBox.attr("id").equals("_ctl0_mainHolder_noresults"))
					final Elements results = resultBox.child(1).children();
					
					int numResults = results.size();
					for(int i = 0; i < numResults; i++){
						
						final Element next = results.first();
						LinearLayout border = new LinearLayout(Encyclopedia.this);
						LinearLayout plantEntry = new LinearLayout(Encyclopedia.this);
						LinearLayout plantText = new LinearLayout(Encyclopedia.this);
						ImageView pic = new ImageView(Encyclopedia.this);
						TextView name = new TextView(Encyclopedia.this);
						TextView altNames = new TextView(Encyclopedia.this);
						
						border.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
						border.setOrientation(LinearLayout.VERTICAL);
						border.setBackgroundColor(Color.BLACK);
						border.setPadding(0, 2, 0, 2);
						
						plantEntry.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
						plantEntry.setBackgroundColor(Color.WHITE);
						plantEntry.setPadding(10, 0, 10, 0);
						
						//create pic
						pic.setLayoutParams(new LayoutParams(60,60));
						//Toast.makeText(Encycl.this, results.first().child(0).child(0).attr("href"), Toast.LENGTH_SHORT).show();
						
						String picURL = "http://www.plantcare.com" + next.child(0).child(0).child(0).attr("src");
						try {
							pic.setImageBitmap(BitmapFactory.decodeStream((new URL(picURL)).openConnection().getInputStream()));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						pic.setPadding(0, 10, 10, 10);

						plantText.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
						plantText.setOrientation(LinearLayout.VERTICAL);
						plantText.setBackgroundColor(Color.WHITE);
						
						name.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
						name.setTextColor(Color.BLUE);
						name.setTextSize(14);
						name.setTypeface(Typeface.DEFAULT_BOLD);
						name.setText(next.child(1).text());
						name.setLinksClickable(true);
						name.setHint(next.child(1).attr("href"));
						
						
						altNames.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
						altNames.setTextColor(Color.BLACK);
						altNames.setTextSize(14);
						altNames.setAutoLinkMask(Linkify.ALL);
						altNames.setLinksClickable(true);
						altNames.setText(next.child(2).text());
						altNames.setPadding(0, 5, 0, 0);
						
						name.setOnClickListener(new OnClickListener(){
							public void onClick(View v){
								//Toast.makeText(Encycl.this, name.getHint(), Toast.LENGTH_SHORT).show();
								
								String plantURL = "http://www.plantcare.com/encyclopedia/" + next.child(1).attr("href");
								Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(plantURL));
				        		startActivity(browserIntent);
							}
						});
						
						plantText.addView(name);
						plantText.addView(altNames);
						plantEntry.addView(pic);
						plantEntry.addView(plantText);
						border.addView(plantEntry);
						content.addView(border);
						
						results.remove(0);
						
					}
				} catch (IOException e) {
					
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(searchURL));
        		//startActivity(browserIntent);
        		

        	
        	}
		});
        
        
        

    }
}