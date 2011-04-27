package edu.berkeley.cs160.smartnature;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.helpers.XMLReaderFactory;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.auth.BasicAWSCredentials;

public class ShareGarden extends Activity implements Runnable, View.OnClickListener {
	
	private static final char[] HEX = "0123456789abcdef".toCharArray();
	
	/** for AWS since certain Android versions do not have org.xml.sax.driver */
	static {
		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
		try { XMLReaderFactory.createXMLReader(); }
		catch ( Exception e ) { e.printStackTrace(); }
	}
	
	Garden garden;
	boolean success;
	Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	/** used in adding hash code to image filename */
	MessageDigest digester;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_garden);
		garden = GardenGnome.gardens.get(getIntent().getIntExtra("garden_id", 0));
		try {
			digester = java.security.MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
		findViewById(R.id.share_confirm).setOnClickListener(this);
		findViewById(R.id.share_cancel).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View view) {
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
		// rails server expects "garden" to be key value
		String revised = "{\"garden\":{" + "\"public\":" + permit + "," + json.substring(1) + "}";
		boolean postSuccess = false;
		try {
			StringEntity entity = new StringEntity(revised);
			entity.setContentType("application/json");
			httppost.setEntity(entity);
			httpclient.execute(httppost);
			postSuccess = true;
		} catch (Exception e) { e.printStackTrace(); }
		
		// get back server id of created garden
		if (postSuccess) {
			String query = "http://gardengnome.heroku.com/search?";
			query += "name=" + Uri.encode(garden.getName());
			query += "&city=" + Uri.encode(garden.getCity());
			query += "&state=" + Uri.encode(garden.getState());
			HttpGet httpget = new HttpGet(query);
			try {
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				String result = EntityUtils.toString(entity);
				garden.setServerId(gson.fromJson(result, int.class));
				success = true;
			} catch (Exception e) { e.printStackTrace(); }
		}
		if (success) {
			runOnUiThread(new Runnable() {
				@Override public void run() {
					Toast.makeText(ShareGarden.this, "Garden has been uploaded online", Toast.LENGTH_SHORT).show();				
				}
			});
			
			uploadImages();
		}
		else
			runOnUiThread(new Runnable() {
				@Override public void run() {
					Toast.makeText(ShareGarden.this, "Garden was not successfully uploaded.", Toast.LENGTH_SHORT).show();				
				}
			});
	}
	
	public void uploadImages() {
		String accessKey = "AKIAIPOGJD62WOASLQYA";
		String secretKey = "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";
		BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		AmazonS3Client s3 = new AmazonS3Client(credentials);
		String bucketName = "gardengnome";
		//ObjectListing objlist = s3.listObjects(bucketName);
		HttpClient httpclient = new DefaultHttpClient();
		for (int i = 0; i < garden.numImages(); i++) {
			Uri uri = garden.getImage(i);
			
			String hash = "";
			String code = garden.getCity() + "|" + garden.getState() + "|" + garden.getName() + "|" + i;
			byte[] bytes = code.getBytes();
			digester.update(bytes);
			byte[] digest = digester.digest();
			for (byte b : digest)
				hash += HEX[(b & 0xf0) >>> 4] + HEX[b & 0xf];
			
			String imageUrl = garden.getName() + i + ".jpg";
			try {
				InputStream stream = getContentResolver().openInputStream(uri);
				long size = getContentResolver().openFileDescriptor(uri, "r").getStatSize();
				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentLength(size);
				s3.putObject(bucketName, imageUrl, stream, metadata);
				stream.close();
			} catch (Exception e) { e.printStackTrace(); }
			
			// notify rails server that image is available 
			HttpPost httppost = new HttpPost("http://gardengnome.heroku.com/gardens/" + garden.getServerId() + "/photos");
			try {
				StringEntity entity = new StringEntity("{\"photo\":{\"title\":\"Untitled\"}}");
				entity.setContentType("application/json");
				httppost.setEntity(entity);
				httpclient.execute(httppost);
			} catch (Exception e) { e.printStackTrace(); }
		}
		
		runOnUiThread(new Runnable() {
			@Override public void run() {
				Toast.makeText(ShareGarden.this, "Photos have been uploaded online", Toast.LENGTH_SHORT).show();				
			}
		});
		
	}
		
}