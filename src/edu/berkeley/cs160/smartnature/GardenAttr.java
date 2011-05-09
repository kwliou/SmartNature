package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

public class GardenAttr extends Activity implements View.OnClickListener {

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
	
	/** Called when the activity is first created. */
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.garden_attr);
		garden = GardenGnome.getGarden(getIntent().getIntExtra("garden_index", 0));
		name = (EditText)findViewById(R.id.garden_name_edit); 
		city = (EditText)findViewById(R.id.garden_city); 
		state = (AutoCompleteTextView) findViewById(R.id.garden_state); 
		name.setText(garden.getName());
		city.setText(garden.getCity());
		state.setText(garden.getState());
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, STATES);
		state.setAdapter(adapter);
		findViewById(R.id.attr_confirm).setOnClickListener(this);
		findViewById(R.id.attr_cancel).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.attr_confirm)
			onBackPressed();
		else {
			name.setText(garden.getName());
			city.setText(garden.getCity());
			state.setText(garden.getState());
		}
	}
	
	@Override
	public void onBackPressed() {
		garden.setName(name.getText().toString());
		String gardenName = name.getText().toString().trim();
		if (gardenName.length() == 0)
			gardenName = "Untitled Garden"; 
		garden.setName(gardenName);
		garden.setCity(city.getText().toString());
		garden.setState(state.getText().toString());
		super.onBackPressed();
	}
	
}
