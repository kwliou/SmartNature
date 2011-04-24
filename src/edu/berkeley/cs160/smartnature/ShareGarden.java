package edu.berkeley.cs160.smartnature;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class ShareGarden extends Activity implements Runnable, View.OnClickListener {
	
	Garden garden;
	Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_garden);
		garden = GardenGnome.gardens.get(getIntent().getIntExtra("garden_id", 0));
		findViewById(R.id.share_confirm).setOnClickListener(this);
		findViewById(R.id.share_cancel).setOnClickListener(this);
	}
	
	@Override public void onClick(View view) {
		if (view.getId() == R.id.share_confirm)
			new Thread(this).start();
		finish();
	}
	
	@Override
	public void run() {
		boolean permit = ((CheckBox)findViewById(R.id.garden_permissions)).isChecked();
		HttpClient httpclient = new DefaultHttpClient();
		
		HttpPost httppost = new HttpPost("http://gardengnome.heroku.com/gardens");
		String json = gson.toJson(garden);
		String revised = "{\"garden\":{" + "\"public\":" + permit + "," + json.substring(1) + "}";
		try {
			StringEntity entity = new StringEntity(revised);
			entity.setContentType("application/json");
			httppost.setEntity(entity);
			httpclient.execute(httppost);
		} catch (Exception e) { e.printStackTrace(); }
		
		runOnUiThread(new Runnable() {
			
			@Override public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(ShareGarden.this, "Garden has been uploaded online", Toast.LENGTH_SHORT).show();				
			}
		});
		
		/*
		File f = new File(Environment.getExternalStorageDirectory(), "photo.jpg");
		try {
			//byte[] imageByteArray = getBytesFromFile(f);
			
			FilePart photo = new FilePart("photo", f); // new ByteArrayPartSource("photo", imageByteArray));
			
			photo.setContentType("image/jpeg");
			photo.setCharSet(null);
			
			Part[] parts = { new StringPart("param_name", "value"), photo };
			httppost.setEntity(new MultipartEntity(parts, httppost.getParams()));
			
			httpclient.execute(httppost);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
	}
	
	// read the photo file into a byte array...
	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		// Get the size of the file
		long length = file.length();
		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
			System.out.println("File is too large");
		}
		
		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];
		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}
		
		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}
		
		// Close the input stream and return bytes
		is.close();
		return bytes;
	}
	
}