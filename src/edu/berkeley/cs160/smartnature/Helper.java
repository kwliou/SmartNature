package edu.berkeley.cs160.smartnature;

import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;

public class Helper {

	/** @see http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue */
	public static int getSampleSize(Context context, Uri uri, float maxSize) {
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
			try {
				InputStream stream = context.getContentResolver().openInputStream(uri);
				BitmapFactory.decodeStream(stream, null, o);
				stream.close();
			} catch (Exception e) { e.printStackTrace(); }
		}
		else
			BitmapFactory.decodeFile(uri.getPath(), o);
		int scale = 1;
		int longSide = Math.max(o.outHeight, o.outWidth);
        if (longSide > maxSize)
            scale = (int) Math.pow(2, (int) Math.round(Math.log(maxSize / longSide) / Math.log(0.5)));
        
		return scale;
	}
	
	public static String toString(RectF rect) {
		return rect.left + " " + rect.top + " " + rect.right + " " + rect.bottom; 
	}
	
	public static String toString(float[] array) {
		String points = "";
		for (float f : array)
			points += f + " ";
		return points;
	}

	public static float[] toFloatArray(String flattened) {
		String[] list = flattened.split(" ");
		float[] points = new float[list.length];
		for (int i = 0; i < list.length; i++)
				points[i] = Float.parseFloat(list[i]);
		return points;
	}
	public static RectF toRectF(String flattened) {
		float[] bounds = new float[4];
		String[] strings = flattened.split(" ");
		for (int i = 0; i < 4; i++)
			bounds[i] = Float.parseFloat(strings[i]);
		return new RectF(bounds[0], bounds[1], bounds[2], bounds[3]); 
	}

}