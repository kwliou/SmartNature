package edu.berkeley.cs160.smartnature;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper {
	private static final String DATABASE_NAME = "gardengnome.db";
	private static final int DATABASE_VERSION = 3;
	
	private static final String CREATE = "CREATE TABLE ";
	private static final String DROP = "DROP TABLE IF EXISTS ";
	
	private static final String GARDEN = "garden";
	private static final String PLOT = "plot";
	private static final String PLANT = "plant";
	private static final String ENTRY = "entry";
	private static final String PHOTO = "photo";
	
	private static final String GARDEN_ID = "garden_id";
	private static final String PLOT_ID = "plot_id";
	private static final String PLANT_ID = "plant_id";
	
	private SQLiteDatabase db;
	
	public DatabaseHelper(Context context) {
		OpenHelper openHelper = new OpenHelper(context);
		db = openHelper.getWritableDatabase();
	}
	
	public int insertGarden(Garden garden) {
		int id = (int) db.insert(GARDEN, null, garden.getContentValues());
		garden.setId(id);
		return id;
	}
	
	public int insertPhoto(Garden garden, Photo photo) {
		ContentValues values = photo.getContentValues();
		values.put(GARDEN_ID, garden.getId());
		int id = (int) db.insert(PHOTO, null, values);
		photo.setId(id);
		return id;
	}
	
	public int insertPlot(Garden garden, Plot plot) {
		ContentValues values = plot.getContentValues();
		values.put(GARDEN_ID, garden.getId());
		int id = (int) db.insert(PLOT, null, values);
		plot.setId(id);
		return id;
	}
	
	public int insertPlant(Plot plot, Plant plant) {
		ContentValues values = plant.getContentValues();
		values.put(PLOT_ID, plot.getId());
		int id = (int) db.insert(PLANT, null, values);
		plant.setId(id);
		return id;
	}
	
	public int insertEntry(Plant plant, Entry entry) {
		ContentValues values = entry.getContentValues();
		values.put(PLANT_ID, plant.getId());
		int id = (int) db.insert(ENTRY, null, values);
		entry.setId(id);
		return id;
	}
	
	public ArrayList<Garden> selectGardens() {
		Cursor cursor = db.query(GARDEN, null, null, null, null, null, null);
		ArrayList<Garden> gardens = new ArrayList<Garden>(cursor.getCount());
		if (cursor.moveToFirst()) {
			do gardens.add(new Garden(cursor));
			while (cursor.moveToNext());
		}
		
		if (!cursor.isClosed())
			cursor.close();
		return gardens;
	}
	
	public ArrayList<Photo> selectPhotos(Garden garden) {
		String[] selectionArgs = { Integer.toString(garden.getId()) };
		Cursor cursor = db.query(PHOTO, null, GARDEN_ID + "=?", selectionArgs, null, null, null);
		ArrayList<Photo> photos = new ArrayList<Photo>(cursor.getCount());
		if (cursor.moveToFirst()) {
			do photos.add(new Photo(cursor));
			while (cursor.moveToNext());
		}
		
		if (!cursor.isClosed())
			cursor.close();
		return photos;
	}
	
	public ArrayList<Plot> selectPlots(Garden garden) {
		String[] selectionArgs = { Integer.toString(garden.getId()) };
		Cursor cursor = db.query(PLOT, null, GARDEN_ID + "=?", selectionArgs, null, null, null);
		ArrayList<Plot> plots = new ArrayList<Plot>(cursor.getCount());
		if (cursor.moveToFirst()) {
			do plots.add(new Plot(cursor));
			while (cursor.moveToNext());
		}
		
		if (!cursor.isClosed())
			cursor.close();
		return plots;
	}
	
	public ArrayList<Plant> selectPlants(Plot plot) {
		String[] selectionArgs = { Integer.toString(plot.getId()) };
		Cursor cursor = db.query(PLANT, null, PLOT_ID + "=?", selectionArgs, null, null, null);
		ArrayList<Plant> plants = new ArrayList<Plant>(cursor.getCount());
		if (cursor.moveToFirst()) {
			do plants.add(new Plant(cursor));
			while (cursor.moveToNext());
		}
		
		if (!cursor.isClosed())
			cursor.close();
		return plants;
	}
	
	public ArrayList<Entry> selectEntries(Plant plant) {
		String[] selectionArgs = { Integer.toString(plant.getId()) };
		Cursor cursor = db.query(ENTRY, null, PLANT_ID + "=?", selectionArgs, null, null, null);
		ArrayList<Entry> entries = new ArrayList<Entry>(cursor.getCount());
		if (cursor.moveToFirst()) {
			do entries.add(new Entry(cursor));
			while (cursor.moveToNext());
		}
		
		if (!cursor.isClosed())
			cursor.close();
		return entries;
	}
	
	public int update(Garden garden) { return updateRow(GARDEN, garden.getId(), garden.getContentValues()); } 
	
	public int update(Photo photo) { return updateRow(PHOTO, photo.getId(), photo.getContentValues()); } 
	
	public int update(Plot plot) { return updateRow(PLOT, plot.getId(), plot.getContentValues()); } 
	
	public int update(Plant plant) { return updateRow(PLANT, plant.getId(), plant.getContentValues()); } 
	
	public int update(Entry entry) { return updateRow(ENTRY, entry.getId(), entry.getContentValues()); } 
	
	public int delete(Garden garden) { return deleteRow(GARDEN, garden.getId()); }
	
	public int delete(Photo photo) { return deleteRow(PHOTO, photo.getId()); }
	
	public int delete(Plot plot) { return deleteRow(PLOT, plot.getId()); }
	
	public int delete(Plant plant) { return deleteRow(PLANT, plant.getId()); }
	
	public int delete(Entry entry) { return deleteRow(ENTRY, entry.getId()); }
	
	public int updateRow(String table, int id, ContentValues values) {
		String[] whereArgs = { Integer.toString(id) };
		return db.update(table, values, "_id=?", whereArgs);
	}
	
	public int deleteRow(String table, int id) {
		String[] whereArgs = { Integer.toString(id) };
		return db.delete(table, "_id=?", whereArgs);
	}
	
	private class OpenHelper extends SQLiteOpenHelper {
		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			String SCAFFOLD = " (_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, server_id INTEGER, name TEXT, ";
			db.execSQL(CREATE + GARDEN + SCAFFOLD + "is_public INTEGER, bounds TEXT, city TEXT, state TEXT, info TEXT)");
			db.execSQL(CREATE + PLOT   + SCAFFOLD + "garden_id INTEGER, shape INTEGER, color INTEGER, angle REAL, bounds TEXT, points TEXT)");
			db.execSQL(CREATE + PLANT  + SCAFFOLD + "plot_id INTEGER)");
			db.execSQL(CREATE + ENTRY  + SCAFFOLD + "plant_id INTEGER, date TEXT)");
			db.execSQL(CREATE + PHOTO  + SCAFFOLD + "garden_id INTEGER, uri TEXT)");
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String[] tables = { GARDEN, PHOTO, PLOT, PLANT, ENTRY };
			for (String table : tables)
				db.execSQL(DROP + table);
			onCreate(db);
		}
	}
	
}
