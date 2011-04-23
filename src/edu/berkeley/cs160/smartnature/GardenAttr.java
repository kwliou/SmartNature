package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class GardenAttr extends Activity {
	
	static String[] STATES = new String[] { "AL", "AK", "AS", "AZ", "AR", "CA", "CO", "CT", "DE", "DC",
											"FM", "FL", "GA", "GU", "HI", "ID", "IL", "IN", "IA", "KS",
											"KY", "LA", "ME", "MH", "MD", "MA", "MI", "MN", "MS", "MO",
											"MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "MP",
											"OH", "OK", "OR", "PW", "PA", "PR", "RI", "SC", "SD", "TN",
											"TX", "UT", "VT", "VI", "VA", "WA", "WV", "WI", "WY" };
	
	/** Called when the activity is first created. */
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.garden_attr);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, STATES);
		AutoCompleteTextView autoView = (AutoCompleteTextView) findViewById(R.id.garden_state);
		autoView.setAdapter(adapter);
	}
	
}
