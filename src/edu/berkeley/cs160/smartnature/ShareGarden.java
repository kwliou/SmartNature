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
		
		final EditText passwordInput = (EditText)findViewById(R.id.garden_password);
		passwordInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (passwordInput.getText().length() > 0)
					findViewById(R.id.garden_permissions2).setEnabled(true);
				else {
					((CheckBox)findViewById(R.id.garden_permissions2)).setChecked(false);
					findViewById(R.id.garden_permissions2).setEnabled(false);
				}			
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}		
		});
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.share_confirm)
			Toast.makeText(this, "Garden has been uploaded to the Internet", Toast.LENGTH_SHORT).show();
		finish();
	}
}