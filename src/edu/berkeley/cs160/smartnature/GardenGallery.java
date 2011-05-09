package edu.berkeley.cs160.smartnature;

import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class GardenGallery extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {
	
	PhotoAdapter adapter;
	ArrayList<Photo> photos;
	Gallery gallery;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (!intent.hasExtra("garden_id")) {
			finish();
			return;
		}
		int gardenID = intent.getIntExtra("garden_id", 0);
		Garden garden = GardenGnome.getGarden(gardenID);
		setTitle(garden.getName() + " Photo Gallery");
		photos = garden.getImages();
		adapter = new PhotoAdapter(this, R.layout.gallery_item, photos);
		setContentView(R.layout.garden_gallery);
		
		gallery = (Gallery) findViewById(R.id.gallery);
		gallery.setAdapter(adapter);
		gallery.setOnItemClickListener(this);
		
		findViewById(R.id.image_left).setOnClickListener(this);
		findViewById(R.id.image_right).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View view) {
		int position = gallery.getSelectedItemPosition();
		if (view.getId() == R.id.image_left && position > 0)
			gallery.setSelection(position - 1); //gallery.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(0, 0));
		else if (view.getId() == R.id.image_right && position < gallery.getCount() - 1)
			gallery.setSelection(position + 1); //gallery.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, new KeyEvent(0, 0));
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Uri contentUri = photos.get(position).getUri();
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		Uri resolvedUri = Uri.parse(ContentResolver.SCHEME_FILE + "://" + cursor.getString(column_index));
		Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(resolvedUri, "image/*");
		startActivity(intent);
	}
	
	public class PhotoAdapter extends ArrayAdapter<Photo> {
		private ArrayList<Photo> photos;
		private LayoutInflater li;
		
		public PhotoAdapter(Context context, int textViewResourceId, ArrayList<Photo> items) {
			super(context, textViewResourceId, items);
			li = ((Activity)context).getLayoutInflater();
			photos = items;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view != null)
				return view;
			
			view = li.inflate(R.layout.gallery_item, null);
			Photo photo = photos.get(position);
			
			Uri imageUri = photo.getUri();
			float maxSize = getWindowManager().getDefaultDisplay().getHeight() / 2f;
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = (int) getSampleSize(imageUri, maxSize);
			
			try {
				InputStream stream = getContentResolver().openInputStream(imageUri);
				Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
				((ImageView)view.findViewById(R.id.gallery_img)).setImageBitmap(bitmap);
				stream.close();
			} catch (Exception e) { e.printStackTrace(); }
			
			return view;
		}
		
		/** @see http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue */
		public double getSampleSize(Uri uri, float maxSize) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
				try {
					InputStream stream = getContentResolver().openInputStream(uri);
					BitmapFactory.decodeStream(stream, null, options);
					stream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else
				BitmapFactory.decodeFile(uri.getPath(), options);
			double scale = 1;
			int longSide = Math.max(options.outHeight, options.outWidth);
			if (longSide > maxSize)
				scale = Math.pow(2, (int) Math.round(Math.log(maxSize / longSide) / Math.log(0.5)));
			
			return scale;
		}
	}
	
}
