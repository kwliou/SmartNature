package edu.berkeley.cs160.smartnature;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class AddPlant extends Activity implements View.OnClickListener {
	
	String garden = "";
	String plot = "";
	ArrayList<Garden> gardens;
	ArrayList<Plot> plots;
	int gardenIndex;
	RadioGroup radioGroup;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_plant);
		
		findViewById(R.id.next).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);
		
		radioGroup = (RadioGroup) findViewById(R.id.rg_gardens);
		
		gardens = GardenGnome.getGardens();
		for (int i = 0; i < gardens.size(); i++) {
			RadioButton r = new RadioButton(this);
			r.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			r.setText(gardens.get(i).getName());
			r.setTextSize(18);
			//r.setPadding(45, 0, 0, 0);
			radioGroup.addView(r);
			
		}
		
	}
	
	public void nextDialog() {
		for (int i = 0; i < gardens.size(); i++) {
			if (gardens.get(i).getName().equals(garden))
				gardenIndex = i;
		}
		plots = gardens.get(gardenIndex).getPlots();
		Button next = (Button) findViewById(R.id.next);
		next.setText("Create");
		TextView prompt = (TextView) findViewById(R.id.choosePrompt);
		prompt.setText("Choose plot");
		
		radioGroup.removeAllViews();
		for (int i = 0; i < plots.size(); i++) {
			RadioButton r = new RadioButton(this);
			r.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			r.setText(plots.get(i).getName());
			r.setTextSize(18);
			radioGroup.addView(r);
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.cancel)
			finish();
		else if (((Button) v).getText().toString().equals("Next")) {
			int radioId = radioGroup.getCheckedRadioButtonId();
			garden = ((RadioButton) findViewById(radioId)).getText().toString();
			nextDialog();
		} else {
			int radioId = radioGroup.getCheckedRadioButtonId();
			int plotIndex = radioGroup.indexOfChild(findViewById(radioId));
			
			Intent intent = new Intent(this, EditScreen.class);
			intent.putExtra("plot_index", plotIndex);
			intent.putExtra("garden_index", gardenIndex);
			setResult(RESULT_OK, intent);
			finish();
		}
	}
	
}
