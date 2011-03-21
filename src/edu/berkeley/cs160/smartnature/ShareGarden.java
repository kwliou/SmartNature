package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class ShareGarden extends Activity implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_garden);
		findViewById(R.id.share_confirm).setOnClickListener(this);
		findViewById(R.id.share_cancel).setOnClickListener(this);
		
		EditText passwordInput = (EditText) findViewById(R.id.garden_password);
		passwordInput.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (((EditText)view).getText().length() > 0)
					findViewById(R.id.garden_permissions2).setEnabled(true);
				else {
					((CheckBox)findViewById(R.id.garden_permissions2)).setChecked(false);
					findViewById(R.id.garden_permissions2).setEnabled(false);
				}
				return false;
			}
		});
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.share_confirm)
			Toast.makeText(this, "Garden has been uploaded to the Internet", Toast.LENGTH_SHORT).show();
		finish();
	}
}