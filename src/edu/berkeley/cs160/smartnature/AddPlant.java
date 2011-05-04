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

public class AddPlant extends Activity implements View.OnClickListener{

	String garden = "";
	String plot = "";
	ArrayList<Garden> gardenList;
	ArrayList<Plot> plotList;
	int gardenId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_plant);
		
		findViewById(R.id.next).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);
		
		RadioGroup options = (RadioGroup)findViewById(R.id.rg_gardens);
		
		gardenList = GardenGnome.getGardens();
		for(int i=0; i<gardenList.size(); i++){
			
			RadioButton r = new RadioButton(this);
			r.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			r.setText(gardenList.get(i).getName());
			r.setTextSize(18);
			r.setPadding(45, 0, 0, 0);
			
			options.addView(r);
			
		}
		
	}
	
	public void nextDialogue(){

		for(int i=0; i<gardenList.size();i++){
			if (gardenList.get(i).getName().equals(garden))
				gardenId = i;
		}
		plotList = gardenList.get(gardenId).getPlots();
		Button next = (Button)findViewById(R.id.next);
		next.setText("Create");
		TextView prompt = (TextView)findViewById(R.id.choosePrompt);
		prompt.setText("Choose plot");
		
		RadioGroup options = (RadioGroup)findViewById(R.id.rg_gardens);
		options.removeAllViews();
		for(int i=0; i<plotList.size(); i++){
			
			RadioButton r = new RadioButton(this);
			r.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			r.setText(plotList.get(i).getName());
			r.setTextSize(18);
			r.setPadding(45, 0, 0, 0);
			
			options.addView(r);
			
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.cancel)
			finish();
		else if(((Button) v).getText().toString().equals("Next")){
			int radioId = ((RadioGroup)findViewById(R.id.rg_gardens)).getCheckedRadioButtonId();
			garden = ((RadioButton)findViewById(radioId)).getText().toString();
			nextDialogue();
			
		}
		else{
			Intent intent = new Intent(this, EditScreen.class);
			Bundle bundle = new Bundle();
			
			//add plant to plot in garden
			int radioId = ((RadioGroup)findViewById(R.id.rg_gardens)).getCheckedRadioButtonId();
			String plotName = ((RadioButton)findViewById(radioId)).getText().toString();
			
			int plotId = 0;
			for(int i=0; i<plotList.size();i++){
				if (plotList.get(i).getName().equals(plotName))
					plotId = i;
			}
			bundle.putInt("plotId", plotId);
			bundle.putInt("gardenId", gardenId);
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			finish();
		}
	}

}
