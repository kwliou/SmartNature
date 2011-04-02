package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class AddPlot extends Activity implements View.OnClickListener{
	EditText et_plot_name;
	RadioButton rb_ellipse;
	RadioButton rb_rectangle;
	RadioButton	rb_custom;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_plot);
		findViewById(R.id.b_add_confirm).setOnClickListener(this);
		findViewById(R.id.b_add_cancel).setOnClickListener(this);

		et_plot_name = (EditText) findViewById(R.id.et_plot_name);
		rb_ellipse = (RadioButton) findViewById(R.id.rb_ellipse);
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
				Bundle extras = getIntent().getExtras();
				Intent intent = new Intent(this, EditScreen.class);
				Bundle bundle = new Bundle();
				bundle.putString("name", et_plot_name.getText().toString());
				if(rb_ellipse.isChecked())
					bundle.putString("type", "elipse");
				else if(rb_rectangle.isChecked())
					bundle.putString("type", "rectangle");
				else if(rb_custom.isChecked())
					bundle.putString("type", "custom");
				bundle.putInt("id", extras.getInt("id"));
				intent.putExtras(bundle);
				startActivity(intent);
				Toast.makeText(this, bundle.getString("type") + " is selected", Toast.LENGTH_SHORT).show();
			}
		}
		finish();
	}
}