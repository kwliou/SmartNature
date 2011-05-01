package edu.berkeley.cs160.smartnature;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
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
	Button shareButton;
	/** for AWS since certain Android versions do not have org.xml.sax.driver */
	static {
		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
		try { XMLReaderFactory.createXMLReader(); }
		catch ( Exception e ) { e.printStackTrace(); }
	}
	
	Garden garden;
	Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	/** used in adding hash code to image filename */
	MessageDigest digester;
	NotificationManager manager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		setContentView(R.layout.share_garden);
		garden = GardenGnome.getGarden(getIntent().getIntExtra("garden_id", 0));
		try {
			digester = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
		shareButton = (Button) findViewById(R.id.share_confirm);
		if (garden.getServerId() == -1)
			shareButton.setText(R.string.btn_sharing);
		else if (garden.getServerId() > 0)
			shareButton.setText(R.string.btn_shared);
		else {
			shareButton.setEnabled(true);
			findViewById(R.id.share_confirm).setOnClickListener(this);
		}
		findViewById(R.id.share_cancel).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.share_confirm && garden.getServerId() == 0) {
			garden.setServerId(-1);
			shareButton.setEnabled(false);
			shareButton.setText(R.string.btn_sharing);
			makeNote(android.R.drawable.stat_sys_upload, "Now uploading", Notification.FLAG_ONGOING_EVENT, false);
			new Thread(this).start();
		}
		else if (view.getId() == R.id.share_cancel)
			finish();
	}
	
	@Override
	public void run() {
		if (!uploadGarden()) {
			garden.setServerId(0);
			makeNote(android.R.drawable.stat_notify_error, "Failed to upload", Notification.FLAG_AUTO_CANCEL, true);
			return;
		}
		
		if (garden.numImages() > 0 && !uploadImages()) {
			makeNote(android.R.drawable.stat_notify_error, "Failed to upload photos from", Notification.FLAG_AUTO_CANCEL, true);
			return;
		}
		
		makeNote(android.R.drawable.stat_sys_upload_done, "Successfully uploaded", Notification.FLAG_AUTO_CANCEL, true);
		runOnUiThread(new Runnable() {
			@Override public void run() {
				shareButton.setText(R.string.btn_shared);
			}
		});
	}
	
	public boolean uploadGarden() {
		CheckBox permiss = (CheckBox) findViewById(R.id.garden_permissions);
		EditText password = (EditText) findViewById(R.id.garden_password);
		garden.setPublic(permiss.isChecked());
		garden.setPassword(password.getText().toString());
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(getString(R.string.server_url) + "gardens.json");
		// rails server expects "garden" to be key value
		String json = "{\"garden\":" + gson.toJson(garden) + "}";
		boolean success = true;
		try {
			StringEntity entity = new StringEntity(json);
			entity.setContentType("application/json");
			httppost.setEntity(entity);
			HttpResponse response = httpclient.execute(httppost);
			String result = EntityUtils.toString(response.getEntity());
			garden.setServerId(gson.fromJson(result, int.class));
		} catch (Exception e) { success = false; e.printStackTrace(); }
		
		/*
		boolean success = false;
		if (postSuccess) {
			String query = "http://gardengnome.heroku.com/search";
			query += "?name=" + Uri.encode(garden.getName());
			query += "&city=" + Uri.encode(garden.getCity());
			query += "&state=" + Uri.encode(garden.getState());
			HttpGet httpget = new HttpGet(query);
			try {
				HttpResponse response = httpclient.execute(httpget);
				String result = EntityUtils.toString(response.getEntity());
				garden.setServerId(gson.fromJson(result, int.class));
				success = true;
			} catch (Exception e) { e.printStackTrace(); }
		}*/
		return success && garden.getServerId() > 0;
	}
	
	public boolean uploadImages() {
		boolean success = true;
		String accessKey = "AKIAIPOGJD62WOASLQYA";
		String secretKey = "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";
		BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		AmazonS3Client s3 = new AmazonS3Client(credentials);
		String bucketName = "gardengnome";
		//ObjectListing objlist = s3.listObjects(bucketName);
		HttpClient httpclient = new DefaultHttpClient();
		for (Photo photo : garden.getImages()) {
			// get image id from rails server
			HttpPost httppost = new HttpPost(getString(R.string.server_url) + "gardens/" + garden.getServerId() + "/photos.json");
			String json = "{\"photo\":" + gson.toJson(photo) + "}";
			try {
				StringEntity entity = new StringEntity(json);
				entity.setContentType("application/json");
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				String result = EntityUtils.toString(response.getEntity());
				photo.setServerId(gson.fromJson(result, int.class));
			} catch (Exception e) { success = false; e.printStackTrace(); }
			
			if (!success || photo.getServerId() == 0)
				return false;
			
			// upload image to s3
			String code = garden.getCity() + "|" + garden.getState() + "|" + garden.getPassword() + "|" + photo.getServerId();
			String fileName = garden.getName() + hexCode(code) + ".jpg";
			Uri imageUri = photo.getUri();
			try {
				InputStream stream = getContentResolver().openInputStream(imageUri);
				long fileSize = getContentResolver().openFileDescriptor(imageUri, "r").getStatSize();
				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentLength(fileSize);
				s3.putObject(bucketName, fileName, stream, metadata);
				stream.close();
			} catch (Exception e) { success = false; e.printStackTrace(); }
		}
		
		return success;
	}
	
	/** meant for SHA-256 */
	public String hexCode(String input) {
		char[] hash = new char[64];
		digester.update(input.getBytes());
		byte[] digest = digester.digest();
		for (int i = 0; i < digest.length; i++) {
			byte b = digest[i];
			hash[2 * i] = HEX[(b >> 4) & 0xf];
			hash[2 * i + 1] = HEX[b & 0xf];
		}
		return new String(hash);
	}
	
	public void makeNote(int icon, String text, int flags, boolean vibrate) {
		manager.cancelAll();
		Notification notification = new Notification(icon, text + " garden", System.currentTimeMillis());
		Intent intent = new Intent(this, ShareGarden.class).putExtra("garden_id", getIntent().getIntExtra("garden_id", 0));
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		int pendingflags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT; // for Samsung Galaxy S
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, pendingflags);
		notification.flags |= flags;
		if (vibrate)
			notification.defaults |= Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
		String title = "GardenGnome";
		String contentText = text + " " + garden.getName(); 
		notification.setLatestEventInfo(this, title, contentText, contentIntent);
		
		manager.notify(1, notification); // NOTE: HTC does not like an id parameter of 0
	}
	
}