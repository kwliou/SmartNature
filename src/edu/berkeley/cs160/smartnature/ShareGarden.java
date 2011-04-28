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
	NotificationManager mNotificationManager;
	
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
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		setContentView(R.layout.share_garden);
		garden = GardenGnome.gardens.get(getIntent().getIntExtra("garden_id", 0));
		try {
			digester = java.security.MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
		findViewById(R.id.share_footer).getBackground().setAlpha(0xff);
		findViewById(R.id.share_confirm).setOnClickListener(this);
		findViewById(R.id.share_cancel).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.share_confirm && garden.getServerId() == 0) {
			//Intent notifyIntent = new Intent(this, GardenScreen.class);
			//notifyIntent.putExtra("garden_id", GardenGnome.gardens.indexOf(garden));
			makeNote(null, android.R.drawable.stat_sys_upload, "Now uploading", Notification.FLAG_AUTO_CANCEL);
			
			new Thread(this).start();
		}
		else
			finish();
	}
	
	@Override
	public void run() {
		garden.setServerId(-1);
		
		if (!uploadGarden()) {
			garden.setServerId(0);
			makeNote(null, android.R.drawable.stat_notify_error, "Failed to upload", Notification.FLAG_AUTO_CANCEL);
			return;
		}
		
		if (!uploadImages()) {
			makeNote(null, android.R.drawable.stat_notify_error, "Failed to upload photos from", Notification.FLAG_AUTO_CANCEL);
			return;
		}
		
		makeNote(null, android.R.drawable.stat_sys_upload_done, "Successfully uploaded", Notification.FLAG_AUTO_CANCEL);
	}
	
	public boolean uploadGarden() {
		CheckBox permiss = (CheckBox) findViewById(R.id.garden_permissions);
		garden.setPublic(permiss.isChecked());
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://gardengnome.heroku.com/gardens");
		String json = gson.toJson(garden);
		// rails server expects "garden" to be key value
		String revised = "{\"garden\":{" + "\"public\":" + garden.isPublic() + "," + json.substring(1) + "}";
		boolean postSuccess = false;
		try {
			StringEntity entity = new StringEntity(revised);
			entity.setContentType("application/json");
			httppost.setEntity(entity);
			httpclient.execute(httppost);
			postSuccess = true;
		} catch (Exception e) { e.printStackTrace(); }
		
		// get back server id of created garden
		boolean success = false;
		if (postSuccess) {
			String query = "http://gardengnome.heroku.com/search";
			query += "?name=" + Uri.encode(garden.getName());
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
		return success;
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
		for (int i = 0; i < garden.numImages(); i++) {
			Uri uri = garden.getImage(i);
			
			String hash = "";
			String code = garden.getCity() + "|" + garden.getState() + "|" + garden.getName() + "|" + i;
			byte[] bytes = code.getBytes();
			digester.update(bytes);
			byte[] digest = digester.digest();
			for (byte b : digest)
				hash += HEX[(b & 0xf0) >>> 4] + HEX[b & 0xf];
			
			String fileName = garden.getName() + i + ".jpg";
			try {
				InputStream stream = getContentResolver().openInputStream(uri);
				long fileSize = getContentResolver().openFileDescriptor(uri, "r").getStatSize();
				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentLength(fileSize);
				s3.putObject(bucketName, fileName, stream, metadata);
				stream.close();
			} catch (Exception e) { success = false; e.printStackTrace(); }
			
			// notify rails server that image is available 
			HttpPost httppost = new HttpPost("http://gardengnome.heroku.com/gardens/" + garden.getServerId() + "/photos");
			try {
				StringEntity entity = new StringEntity("{\"photo\":{\"title\":\"Untitled\"}}");
				entity.setContentType("application/json");
				httppost.setEntity(entity);
				httpclient.execute(httppost);
			} catch (Exception e) { success = false; e.printStackTrace(); }
		}
		
		return success;
	}
	
	int noteIndex;
	public void makeNote(Intent intent, int icon, String text, int flags) {
		mNotificationManager.cancelAll();
		Notification notification = new Notification(icon, text + " garden", System.currentTimeMillis());
		CharSequence contentTitle = "GardenGnome";
		String contentText = text + " " + garden.getName(); 
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
		//notification.flags = Notification.DEFAULT_ALL;
		notification.flags |= flags;
		//notification.flags |= Notification.DEFAULT_VIBRATE | Notification.FLAG_SHOW_LIGHTS;
		notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, contentIntent);
		
		mNotificationManager.notify(0, notification);
	}
	
	public void makeToast(final String message) {
		runOnUiThread(new Runnable() {
			@Override public void run() {
				Toast.makeText(ShareGarden.this, message, Toast.LENGTH_SHORT).show();				
			}
		});
	}
	
}