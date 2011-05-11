package edu.berkeley.cs160.smartnature;

import java.io.InputStream;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore.Images;

public class Helper {
	
	public static float getFloat(Cursor cursor, String column) {
		return cursor.getFloat(cursor.getColumnIndex(column));
	}
	
	public static int getInt(Cursor cursor, String column) {
		return cursor.getInt(cursor.getColumnIndex(column));
	}
	
	public static String getString(Cursor cursor, String column) {
		return cursor.getString(cursor.getColumnIndex(column));
	}
	
	/** converts image content URI to file path */
	public static String resolveUri(Activity activity, Uri uri) {
		String[] projection = { Images.Media.DATA };
		Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	
	/** @see http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue */
	public static int getSampleSize(Context context, Uri uri, float maxSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
			try {
				InputStream stream = context.getContentResolver().openInputStream(uri);
				BitmapFactory.decodeStream(stream, null, options);
				stream.close();
			} catch (Exception e) { e.printStackTrace(); }
		}
		else
			BitmapFactory.decodeFile(uri.getPath(), options);
		int scale = 1;
		int longSide = Math.max(options.outHeight, options.outWidth);
		if (longSide > maxSize)
			scale = 1 << (int) (Math.log(maxSize / longSide) / Math.log(0.5));
		System.out.println("inSampleSize=" + scale);
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