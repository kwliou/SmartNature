package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

public class GardenAttr extends Activity {

	static String[] ABBR = new String[] { "AL", "AK", "AS", "AZ", "AR", "CA", "CO", "CT", "DE", "DC",
											"FM", "FL", "GA", "GU", "HI", "ID", "IL", "IN", "IA", "KS",
											"KY", "LA", "ME", "MH", "MD", "MA", "MI", "MN", "MS", "MO",
											"MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "MP",
											"OH", "OK", "OR", "PW", "PA", "PR", "RI", "SC", "SD", "TN",
											"TX", "UT", "VT", "VI", "VA", "WA", "WV", "WI", "WY" };
	
	static String[] STATES = new String[] { "Alabama", "Alaska" ,"American Samoa", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "District of Columbia",
											"Federated States of Micronesia", "Florida", "Georgia", "Guam", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas",
											"Kentucky", "Louisiana", "Maine", "Marshall Islands", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri",
											"Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Northern Marianas Islands",
											"Ohio", "Oklahoma", "Oregon", "Palau", "Pennsylvania", "Puerto Rico", "Rhode Island", "South Carolina", "South Dakota", "Tennessee",
											"Texas", "Utah", "Vermont", "Virginia", "Virgin Islands", "Washington", "West Virginia", "Wisconsin", "Wyoming" };
	
	Garden garden;
	EditText name, city;
	AutoCompleteTextView state;
	int g_pk = -1;
	
	/** Called when the activity is first created. */
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.garden_attr);
		garden = GardenGnome.getGarden(getIntent().getIntExtra("garden_id", 0));
		g_pk = getIntent().getIntExtra("garden_id", 0) + 1;
		name = (EditText)findViewById(R.id.garden_name_edit); 
		city = (EditText)findViewById(R.id.garden_city); 
		state = (AutoCompleteTextView) findViewById(R.id.garden_state); 
		name.setText(garden.getName());
		city.setText(garden.getCity());
		state.setText(garden.getState());
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, STATES);
		state.setAdapter(adapter);
	}
	
	@Override
	public void onBackPressed() {
		garden.setName(name.getText().toString());
		garden.setCity(city.getText().toString());
		garden.setState(state.getText().toString());
		//StartScreen.dh.update_garden(g_pk, garden.getName(), garden.getCity(), garden.getState());
		super.onBackPressed();
	}
	
}
