package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class ShareGarden extends Activity implements View.OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_garden);
		findViewById(R.id.share_confirm).setOnClickListener(this);
		findViewById(R.id.share_cancel).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.share_confirm)
			Toast.makeText(this, "Garden has been uploaded online", Toast.LENGTH_SHORT).show();
		finish();
	}
	

}