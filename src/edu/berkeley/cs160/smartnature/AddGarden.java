package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class AddGarden extends Activity implements View.OnClickListener{
	EditText et_plot_name;
	RadioButton rb_eclipse;
	RadioButton rb_rectangle;
	RadioButton	rb_custom;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_garden);
		findViewById(R.id.b_add_confirm).setOnClickListener(this);
		findViewById(R.id.b_add_cancel).setOnClickListener(this);

		et_plot_name = (EditText) findViewById(R.id.et_plot_name);
		rb_eclipse = (RadioButton) findViewById(R.id.rb_eclipse);
		rb_rectangle = (RadioButton) findViewById(R.id.rb_rectangle);
		rb_custom = (RadioButton) findViewById(R.id.rb_custom);	
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.b_add_confirm) {
			if(et_plot_name.getText().length() == 0) {
				Toast.makeText(this, "Please set your plot name", Toast.LENGTH_SHORT).show();
				return;
			}
			else {
				Intent intent = new Intent(this, EditScreen.class);
				Bundle bundle = new Bundle();
				bundle.putString("name", et_plot_name.getText().toString());
				if(rb_eclipse.isChecked())
					bundle.putString("type", "elipse");
				else if(rb_rectangle.isChecked())
					bundle.putString("type", "rectangle");
				else if(rb_custom.isChecked())
					bundle.putString("type", "custom");
				intent.putExtras(bundle);
				startActivity(intent);
				Toast.makeText(this, bundle.getString("type") + " is selected", Toast.LENGTH_SHORT).show();
			}
		}
		finish();
	}
}