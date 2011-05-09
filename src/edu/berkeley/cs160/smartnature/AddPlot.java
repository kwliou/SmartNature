package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;

public class AddPlot extends Activity implements View.OnClickListener, View.OnKeyListener {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_plot);
		findViewById(R.id.et_plot_name).setOnKeyListener(this);
		findViewById(R.id.b_add_confirm).setOnClickListener(this);
		findViewById(R.id.b_add_cancel).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		EditText et_plot_name = (EditText) findViewById(R.id.et_plot_name);
		
		if (view.getId() == R.id.b_add_cancel) {
			finish();
			return;
		}
		Intent intent = new Intent(this, EditScreen.class);
		
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
		String plotName = et_plot_name.getText().toString().trim();
		if (plotName.length() == 0)
			plotName = "Untitled plot"; 
		intent.putExtra("name", plotName);
		intent.putExtra("type", shapeType);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	@Override
	public boolean onKey(View view, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
			InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
			return true;
		}
		
		return false;
	}
}