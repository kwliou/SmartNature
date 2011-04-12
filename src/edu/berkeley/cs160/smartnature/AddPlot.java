package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class AddPlot extends Activity implements View.OnClickListener {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_plot);
		findViewById(R.id.b_add_confirm).setOnClickListener(this);
		findViewById(R.id.b_add_cancel).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		EditText et_plot_name = (EditText) findViewById(R.id.et_plot_name);
		
		if (view.getId() == R.id.b_add_cancel)
			finish();
		else if (et_plot_name.getText().length() == 0)
			Toast.makeText(this, "Please set your plot name", Toast.LENGTH_SHORT).show();
		else {
			Intent intent = new Intent(this, EditScreen.class);
			Bundle bundle = new Bundle();
			
			int radioId = ((RadioGroup)findViewById(R.id.rg_shape)).getCheckedRadioButtonId();
			int shapeType;
			switch (radioId) {
				case R.id.rb_rectangle:
					shapeType = Plot.RECT;
					break;
				case R.id.rb_ellipse:
					shapeType = Plot.OVAL;
					break;
				default:
					shapeType = Plot.POLY;
					break;
			}
			bundle.putString("name", et_plot_name.getText().toString());
			bundle.putInt("type", shapeType);
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			finish();
		}
	}
}