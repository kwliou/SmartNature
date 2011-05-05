package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

public class GardenGallery extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

	int gardenID, numPhotos;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    Intent intent = getIntent();
	    if (!intent.hasExtra("garden_id")) {
			finish();
			return;
		}
		gardenID = intent.getIntExtra("garden_id", 0);
		numPhotos = GardenGnome.getGarden(gardenID).numImages();
		Toast.makeText(GardenGallery.this, "" + numPhotos, Toast.LENGTH_SHORT).show();

	    
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

	        i.setImageURI(GardenGnome.getGarden(gardenID).getImage(position).getUri());
	        i.setLayoutParams(new Gallery.LayoutParams(100, 100));
	        i.setScaleType(ImageView.ScaleType.FIT_XY);

	        return i;
	    }
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
