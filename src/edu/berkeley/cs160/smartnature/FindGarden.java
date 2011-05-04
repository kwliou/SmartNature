package edu.berkeley.cs160.smartnature;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class FindGarden extends ListActivity implements AdapterView.OnItemClickListener, DialogInterface.OnClickListener, View.OnKeyListener {
	
	static private int ID = 0, NAME = 1, CITY = 2, STATE = 3, PUBLIC = 4, PASSWORD = 4;
	static StubAdapter adapter;
	ArrayList<String[]> stubs = new ArrayList<String[]>();
	Gson gson = new Gson();
	View textEntryView;
	NotificationManager manager;
	MessageDigest digester;
	MediaScannerConnection scanner;
	/** position of item clicked */
	int positionClicked;
	
	@Override @SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); // Window.FEATURE_PROGRESS
		scanner = new MediaScannerConnection(this, null);
		scanner.connect();
		Object previousData = getLastNonConfigurationInstance(); 
		if (previousData != null)
			stubs = (ArrayList<String[]>) previousData;
		setContentView(R.layout.find_garden);
		adapter = new StubAdapter(this, R.layout.findgarden_list_item, stubs);
		setListAdapter(adapter);
		getListView().setOnItemClickListener(this);
		customFocus();
		
		if (previousData == null) {
			setProgressBarIndeterminateVisibility(true);
			new Thread(getStubs).start();
		}
	}
	
	public void customFocus() {
		findViewById(R.id.search_garden_name).setOnKeyListener(new View.OnKeyListener() {
			@Override public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					findViewById(R.id.search_garden_city).requestFocus();
					return true;
				}
				return false;
			}
		});
		
		findViewById(R.id.search_garden_city).setOnKeyListener(new View.OnKeyListener() {
			@Override public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					findViewById(R.id.search_garden_state).requestFocus();
					return true;
				}
				return false;
			}
		});
		
		AutoCompleteTextView searchState = (AutoCompleteTextView) findViewById(R.id.search_garden_state); 
		
		searchState.setOnKeyListener(new View.OnKeyListener() {
			@Override public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, GardenAttr.STATES);
		searchState.setAdapter(adapter);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return stubs.isEmpty() ? null : stubs;
	}
	
	Runnable getStubs = new Runnable() {
		@Override
		public void run() {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(getString(R.string.server_url) + "stubs");
			boolean success = true;
			String[][] results = null;
			try {
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				String result = EntityUtils.toString(entity);
				results = gson.fromJson(result, String[][].class);
			} catch (Exception e) { success = false; e.printStackTrace(); }
			
			if (success) {
				stubs.addAll(new ArrayList<String[]>(java.util.Arrays.asList(results)));
				runOnUiThread(new Runnable() {
					@Override public void run() { adapter.notifyDataSetChanged(); }
				});
			} else {
				runOnUiThread(new Runnable() {
					@Override public void run() {
						findViewById(R.id.find_garden_msg).setVisibility(View.VISIBLE);
					}
				});
			}
			
			runOnUiThread(new Runnable() {
				@Override public void run() { setProgressBarIndeterminateVisibility(false); }
			});
		}
	};
	
	public Garden getGarden(String serverId) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(getString(R.string.server_url) + "gardens/" + serverId + ".json");
		String result = "";
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);
		} catch (Exception e) { e.printStackTrace(); }
		
		System.out.println("garden_json=" + result);
		Garden garden = gson.fromJson(result, Garden.class);
		return garden;
	}
	
	public Garden getWholeGarden(String serverId) {
		Garden garden = getGarden(serverId);
		getPlots(garden);
		getImages(garden);
		return garden;
	}
	
	public void getPlots(Garden garden) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(getString(R.string.server_url) + "gardens/" + garden.getServerId() + "/plots.json");
		String result = "";
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);
		} catch (Exception e) { e.printStackTrace(); }
		System.out.println("plots_json=" + result);
		Plot[] plots = gson.fromJson(result, Plot[].class);
		
		for (Plot plot : plots) {
			plot.postDownload();
			garden.addPlot(plot);
			getPlants(plot);
		}
	}
	
	public void getPlants(Plot plot) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(getString(R.string.server_url) + "plots/" + plot.getServerId() + "/plants.json");
		String result = "";
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);
		} catch (Exception e) { e.printStackTrace(); }
		System.out.println("plants_json=" + result);
		Plant[] plants = gson.fromJson(result, Plant[].class);
		
		for (Plant plant : plants) {
			plot.addPlant(plant);
			getEntries(plant);
		}
	}
	
	public void getEntries(Plant plant) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(getString(R.string.server_url) + "plants/" + plant.getServerId() + "/journals.json");
		String result = "";
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);
		} catch (Exception e) { e.printStackTrace(); }
		System.out.println("journals_json=" + result);
		Entry[] entries = gson.fromJson(result, Entry[].class);
		System.out.println("plant_entries.nil?=" + Boolean.toString(plant.getEntries() == null));
		plant.setEntries(new ArrayList<Entry>());
		for (Entry entry : entries)
			plant.addEntry(entry);
	}
	
	public void getImages(Garden garden) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(getString(R.string.server_url) + "gardens/" + garden.getServerId() + "/photos.json");
		String result = "";
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);
		} catch (Exception e) { e.printStackTrace(); }
		System.out.println("photos_json=" + result);
		Photo[] photos = gson.fromJson(result, Photo[].class);
		
	 	try {
			digester = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) { return; }
	 	String accessKey = "AKIAIPOGJD62WOASLQYA";
		String secretKey = "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";
		BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		AmazonS3Client s3 = new AmazonS3Client(credentials);
		String bucketName = "gardengnome";
		//ObjectListing objlist = s3.listObjects(bucketName);
		for (Photo photo : photos) {
			// download image from s3
			String code = garden.getCity() + "|" + garden.getState() + "|" + garden.getPassword() + "|" + photo.getServerId();
			String fileName = garden.getName() + hexCode(code) + ".jpg";
			InputStream input = s3.getObject(bucketName, fileName).getObjectContent();
			
			Uri imageUri = writeBitmap2(fileName, input);
			photo.setUri(imageUri);
			garden.addImage(photo);
		}
	}
	
	/** writes to custom folder /sdcard/Pictures/GardenGnome
 		and shows up under "GardenGnome" in the Gallery app */
	public Uri writeBitmap1(String fileName, InputStream input) {
		File dir = new File(Environment.getExternalStorageDirectory(), "Pictures");
		dir.mkdir();
		dir = new File(dir, "GardenGnome");
		dir.mkdir();
		File file = new File(dir, fileName);
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(file);
		} catch (FileNotFoundException e) { e.printStackTrace(); return null; }
		Uri imageUri = Uri.fromFile(file);
		System.out.println("imageUri=" + imageUri.toString());
		
		int buffer = 0;
		try {
			while ((buffer = input.read()) != -1)
				output.write(buffer);
			input.close();
			output.close();
		} catch (IOException e) { e.printStackTrace(); return null; }
		
		// notify change
		scanner.scanFile(imageUri.getPath(), "image/jpeg");
		return imageUri;
	}
	
	/** writes to device's "external image storage" ex. /sdcard/DCIM/camera
	 	and shows up under "Camera images" in the Gallery app */
	public Uri writeBitmap2(String fileName, InputStream input) {
		ContentValues values = new ContentValues();
		values.put(Images.Media.TITLE, fileName); //values.put(Images.Media.DESCRIPTION, "Image capture by camera");
		Uri imageUri = getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
		System.out.print("imageUri=" + imageUri.toString() + " -> ");
		android.database.Cursor cursor = managedQuery(imageUri, new String[] {MediaStore.Images.Media.DATA}, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String resolvedPath = cursor.getString(column_index);
		System.out.println(resolvedPath);
		
		OutputStream output = null;
		try {
			output = getContentResolver().openOutputStream(imageUri);
		} catch (FileNotFoundException e) { e.printStackTrace(); return null; }
		int buffer = 0;
		try {
			while ((buffer = input.read()) != -1)
				output.write(buffer);
			input.close();
			output.close();
		} catch (IOException e) { e.printStackTrace(); return null; }
		getContentResolver().notifyChange(imageUri, null);
		
		scanner.scanFile(resolvedPath, "image/jpeg");
		
		return imageUri;
	}
	
	@Override public boolean onKey(View view, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (view.getId()) {
				//case R.id.search_garden_name
			}
		}
		return false;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		positionClicked = position;
		String[] stub = stubs.get(positionClicked);
		boolean is_public = Boolean.parseBoolean(stub[PUBLIC]);
		showDialog(is_public ? PUBLIC : 0);
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		textEntryView = LayoutInflater.from(this).inflate(R.layout.text_entry_dialog, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String[] stub = stubs.get(positionClicked);
		boolean is_public = Boolean.parseBoolean(stub[PUBLIC]);
		
		if (!is_public)
			builder.setView(textEntryView);
		else
			builder.setMessage("Confirm you want to download");
		
		final Dialog dialog = builder.setTitle("Download garden")
		.setPositiveButton(R.string.alert_dialog_confirm, this)
		.setNegativeButton(R.string.alert_dialog_cancel, cancelled) // this means cancel was pressed
		.create();
		
		if (!is_public) {
			// automatically show soft keyboard
			EditText input = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
			input.setHint("Enter password");
			input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
			input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus)
						dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			});
		}
		/*input.setOnKeyListener(new View.OnKeyListener() {
			@Override public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});*/
		
		return dialog;
	}
	
	DialogInterface.OnClickListener cancelled = new DialogInterface.OnClickListener() {
		@Override public void onClick(DialogInterface dialog, int whichButton) {
			dialog.cancel();
			removeDialog(0);
			removeDialog(PUBLIC);
		}	
	};
	
	class getGarden implements Runnable {
		String serverId;
		getGarden(String serverId) { this.serverId = serverId; }
		
		@Override
		public void run() {
			Garden garden = getWholeGarden(serverId);
			GardenGnome.addGarden(garden);
			int gardenId = GardenGnome.getGardens().indexOf(garden);
			Intent intent = new Intent(FindGarden.this, GardenScreen.class).putExtra("garden_id", gardenId);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			
			String[] text = { "Successfully downloaded", garden.getName() };
			makeNote(Integer.parseInt(serverId), android.R.drawable.stat_sys_download_done, text, Notification.FLAG_AUTO_CANCEL, true, intent);
		}
	}; 
	
	public void onClick(DialogInterface dialog, int whichButton) {
		String[] stub = stubs.get(positionClicked);
		boolean is_public = Boolean.parseBoolean(stub[PUBLIC]);
		
		String password = "";
		if (!is_public) {
			EditText textEntry = ((EditText) textEntryView.findViewById(R.id.dialog_text_entry));
			password = textEntry.getText().toString();
		}
		if (is_public || password.equals(stub[PASSWORD])) {
			String[] text = {"Now downloading", stub[NAME] };
			makeNote(Integer.parseInt(stub[ID]), android.R.drawable.stat_sys_download, text, Notification.FLAG_ONGOING_EVENT, false);
			removeDialog(PUBLIC); removeDialog(0);
			new Thread(new getGarden(stub[ID])).start();
			//GardenGnome.addGarden(garden);
		}
		else {
			Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void makeNote(int id, int icon, String[] text, int flags, boolean vibrate) {
		makeNote(id, icon, text, flags, vibrate, null);
	}
	
	public void makeNote(int id, int icon, String[] text, int flags, boolean vibrate, Intent intent) {
		manager.cancelAll();
		Notification notification = new Notification(icon, text[0] + " garden", System.currentTimeMillis());
		//Intent intent = new Intent(this, ShareGarden.class).putExtra("garden_id", getIntent().getIntExtra("garden_id", 0));
		//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		int pendingflags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT; // for Samsung Galaxy S
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, pendingflags);
		notification.flags |= flags;
		if (vibrate)
			notification.defaults |= Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
		String title = "GardenGnome";
		String contentText = text[0] + " " + text[1] + " garden"; 
		notification.setLatestEventInfo(this, title, contentText, contentIntent);
		
		manager.notify(id + 1, notification); // NOTE: HTC does not like an id parameter of 0
	}
	
	private static final char[] HEX = "0123456789abcdef".toCharArray();
	
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
	
	class StubAdapter extends ArrayAdapter<String[]> {
		
		private ArrayList<String[]> stubs;
		private LayoutInflater li;
		
		public StubAdapter(Context context, int textViewResourceId, ArrayList<String[]> items) {
			super(context, textViewResourceId, items);
			li = ((ListActivity) context).getLayoutInflater();
			stubs = items;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null)
				view = li.inflate(R.layout.findgarden_list_item, null);
			String[] stub = stubs.get(position);
			((TextView) view.findViewById(R.id.garden_name)).setText(stub[NAME]);
			((TextView) view.findViewById(R.id.garden_info)).setText(stub[CITY] + ", " + stub[STATE]);
			return view;
		}
	}
	
}