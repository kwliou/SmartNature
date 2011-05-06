package edu.berkeley.cs160.smartnature;

import java.io.InputStream;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class GardenGallery extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

	int gardenID, numPhotos;
	Garden garden;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    Intent intent = getIntent();
	    if (!intent.hasExtra("garden_id")) {
			finish();
			return;
		}
		gardenID = intent.getIntExtra("garden_id", 0);
		garden = GardenGnome.getGarden(gardenID);
		numPhotos = garden.numImages();
		//Toast.makeText(GardenGallery.this, "" + numPhotos, Toast.LENGTH_SHORT).show();
	    
	    setContentView(R.layout.gallery);

	    Gallery g = (Gallery) findViewById(R.id.gardenPics);
	    g.setAdapter(new ImageAdapter(this));

	}
	
	public class ImageAdapter extends BaseAdapter {
	    private Context mContext;

	    public ImageAdapter(Context c) {
	        mContext = c;
	    }

	    public int getCount() {
	        return numPhotos;
	    }

	    public Object getItem(int position) {
	        return position;
	    }

	    public long getItemId(int position) {
	        return position;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	        ImageView i = new ImageView(mContext);
	        Uri imageUri = garden.getImage(position).getUri();
			BitmapFactory.Options options = new BitmapFactory.Options();
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			options.inSampleSize = (int) getSampleSize(imageUri);
			options.inTargetDensity = metrics.densityDpi;
			
			try {
				InputStream stream = getContentResolver().openInputStream(imageUri);
				i.setImageBitmap(BitmapFactory.decodeStream(stream, null, options));
				stream.close();
			} catch (Exception e) { e.printStackTrace(); }
		
			i.setLayoutParams(new Gallery.LayoutParams(100, 100));
	        i.setScaleType(ImageView.ScaleType.FIT_XY);

	        return i;
	    }
	}

	
	/** @see http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue */
	public double getSampleSize(Uri uri) {
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
			try {
				InputStream stream = getContentResolver().openInputStream(uri);
				BitmapFactory.decodeStream(stream, null, o);
				stream.close();
			} catch (Exception e) { e.printStackTrace(); }
		}
		else
			BitmapFactory.decodeFile(uri.toString().replace("file://", ""), o);
		double scale = 1;
        if (o.outWidth > 75 || o.outHeight > 50)
            scale = Math.pow(2, (int) Math.round(Math.log(75 / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        
		return scale;
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
