package edu.berkeley.cs160.smartnature;
//http://www.screaming-penguin.com/node/7742

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Rect;
import android.graphics.RectF;

public class DatabaseHelper {
	private static final String DATABASE_NAME = "garden_gnome.db";
	private static final int DATABASE_VERSION = 1;

	private static final String TABLE_NAME_GARDEN = "garden";
	private static final String TABLE_NAME_PLOT = "plot";
	private static final String TABLE_NAME_PLANT = "plant";
	private static final String TABLE_NAME_ENTRY = "entry";
	private static final String TABLE_NAME_MAP_GP = "map_garden_plot";
	private static final String TABLE_NAME_MAP_PP = "map_plot_plant";
	private static final String TABLE_NAME_MAP_PE = "map_plant_entry";
	private static final String INSERT_GARDEN = "insert into " + TABLE_NAME_GARDEN + " (g_pk, name, previewId, bounds) values (NULL, ?, ?, ?)";
	private static final String INSERT_PLOT = "insert into " + TABLE_NAME_PLOT + " (po_pk, name, shape, type, color, polyPoints, rotation, id) values (NULL, ?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT_PLANT = "insert into " + TABLE_NAME_PLANT + " (pa_pk, name, id) values (NULL, ?, ?)";
	private static final String INSERT_ENTRY = "insert into " + TABLE_NAME_ENTRY + " (e_pk, name, date) values (NULL, ?, ?)";
	private static final String INSERT_MAP_GP = "insert into " + TABLE_NAME_MAP_GP + " (g_map, po_map) values (?, ?)";
	private static final String INSERT_MAP_PP = "insert into " + TABLE_NAME_MAP_PP + " (po_map, pa_map) values (?, ?)";
	private static final String INSERT_MAP_PE = "insert into " + TABLE_NAME_MAP_PE + " (pa_map, e_map) values (?, ?)";


	private Context context;
	private SQLiteDatabase db;
	private SQLiteStatement insertStmt_garden, insertStmt_plot, insertStmt_plant, insertStmt_entry, insertStmt_map_gp, insertStmt_map_pp, insertStmt_map_pe;

	public DatabaseHelper(Context context) {
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
		this.insertStmt_garden = this.db.compileStatement(INSERT_GARDEN);
		this.insertStmt_plot = this.db.compileStatement(INSERT_PLOT);
		this.insertStmt_plant = this.db.compileStatement(INSERT_PLANT);
		this.insertStmt_entry = this.db.compileStatement(INSERT_ENTRY);
		this.insertStmt_map_gp = this.db.compileStatement(INSERT_MAP_GP);
		this.insertStmt_map_pp = this.db.compileStatement(INSERT_MAP_PP);
		this.insertStmt_map_pe = this.db.compileStatement(INSERT_MAP_PE);
	}

	public long insert_garden(String name, int previewId, String bounds) {
		this.insertStmt_garden.clearBindings();
		this.insertStmt_garden.bindString(1, name);
		this.insertStmt_garden.bindLong(2, (long) previewId);
		this.insertStmt_garden.bindString(3, bounds);
		return this.insertStmt_garden.executeInsert();
	}

	public long update_garden(int g_pk, String name, int previewId, String bounds) {
		ContentValues cv = new ContentValues();
		cv.put("name", name);
		cv.put("previewId", previewId);
		cv.put("bounds", bounds);
		String selection = "g_pk = ?";
		return db.update(TABLE_NAME_GARDEN, cv, selection, new String[] {Integer.toString(g_pk)});
	}

	public void delete_garden(String name) {
		String selection = "name = ?";
		this.db.delete(TABLE_NAME_GARDEN, selection, new String[] {name});
	}

	public void delete_all_garden() {
		this.db.delete(TABLE_NAME_GARDEN, null, null);
	}

	public Garden select_garden(String name) {
		String selection = "name" + " = '" + name + "'";
		Cursor cursor = this.db.query(TABLE_NAME_GARDEN, null, selection, null, null, null, null);
		Garden temp = null;
		if (cursor.moveToFirst()) {
			do {
				temp = new Garden(Integer.parseInt(cursor.getString(cursor.getColumnIndex("previewId"))), cursor.getString(cursor.getColumnIndex("name")));
				String[] bound_s = cursor.getString(cursor.getColumnIndex("bounds")).split(",");
				RectF bound_rf = new RectF(Integer.parseInt(bound_s[0].trim()), Integer.parseInt(bound_s[1].trim()), Integer.parseInt(bound_s[2].trim()), Integer.parseInt(bound_s[3].trim()));
				temp.setRawBounds(bound_rf);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		return temp;
	}

	public List<String> select_all_garden(String column) {
		List<String> list = new ArrayList<String>();
		Cursor cursor = this.db.query(TABLE_NAME_GARDEN, new String[] {column}, null, null, null, null, "g_pk asc");
		if (cursor.moveToFirst()) {
			do 
				list.add(cursor.getString(cursor.getColumnIndex(column)));
			while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) 
			cursor.close();
		return list;
	}

	public long insert_plot(String name, String shape, int type, int color, String polyPoints, float rotation, int id) {
		this.insertStmt_plot.clearBindings();
		this.insertStmt_plot.bindString(1, name);
		this.insertStmt_plot.bindString(2, shape);
		this.insertStmt_plot.bindLong(3, (long) type);
		this.insertStmt_plot.bindLong(4, (long) color);
		this.insertStmt_plot.bindString(5, polyPoints);
		this.insertStmt_plot.bindDouble(6, (double) rotation);
		this.insertStmt_plot.bindLong(7, (long) id);
		return this.insertStmt_plot.executeInsert();
	}

	public Plot select_plot(String name) {
		String selection = "name" + " = '" + name + "'";
		Cursor cursor = this.db.query(TABLE_NAME_PLOT, null, selection, null, null, null, null);
		Plot temp = null;
		if (cursor.moveToFirst()) {
			do {
				String[] shape_s = cursor.getString(cursor.getColumnIndex("shape")).split(",");
				Rect bound_r = new Rect(Integer.parseInt(shape_s[0]), Integer.parseInt(shape_s[1]), Integer.parseInt(shape_s[2]), Integer.parseInt(shape_s[3]));
				int color = Integer.parseInt(shape_s[4]);
				if(cursor.getInt(cursor.getColumnIndex("type")) == 0 || cursor.getInt(cursor.getColumnIndex("type")) == 1)
					temp = new Plot(cursor.getString(cursor.getColumnIndex("name")), bound_r, cursor.getFloat(cursor.getColumnIndex("rotation")), cursor.getInt(cursor.getColumnIndex("type")));
				else {
					String[] polyPoints_s = cursor.getString(cursor.getColumnIndex("polyPoints")).split(",");
					float[] polyPoints_f = new float[polyPoints_s.length];
					for(int i = 0; i < polyPoints_f.length; i++)
						polyPoints_f[i] = Float.valueOf(polyPoints_s[i].trim());
					temp = new Plot(cursor.getString(cursor.getColumnIndex("name")), bound_r, cursor.getFloat(cursor.getColumnIndex("rotation")), polyPoints_f);
				}
				temp.setColor(cursor.getInt(cursor.getColumnIndex("color")));
				temp.getPaint().setColor(color);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		return temp;
	}

	public long insert_plant(String name, int id) {
		this.insertStmt_plant.clearBindings();
		this.insertStmt_plant.bindString(1, name);
		this.insertStmt_plant.bindLong(2, (long)id);
		return this.insertStmt_plant.executeInsert();
	}

	public Plant select_plant(String name) {
		String selection = "name" + " = '" + name + "'";
		Cursor cursor = this.db.query(TABLE_NAME_PLANT, null, selection, null, null, null, null);
		Plant temp = null;
		if (cursor.moveToFirst()) {
			do {
				temp = new Plant(cursor.getString(cursor.getColumnIndex("name")));
				temp.setID(cursor.getInt(cursor.getColumnIndex("id")));
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		return temp;
	}

	public long insert_entry(String name, String date) {
		this.insertStmt_map_pp.clearBindings();
		this.insertStmt_map_pp.bindString(1, name);
		this.insertStmt_map_pp.bindString(2, date);
		return this.insertStmt_entry.executeInsert();
	}

	public Entry select_entry(String name) {
		String selection = "name" + " = '" + name + "'";
		Cursor cursor = this.db.query(TABLE_NAME_ENTRY, null, selection, null, null, null, null);
		Entry temp = null;
		if (cursor.moveToFirst()) {
			do
				temp = new Entry(cursor.getString(cursor.getColumnIndex("name")), cursor.getString(cursor.getColumnIndex("date")));
			while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		return temp;
	}

	public long insert_map_gp(int g_map, int po_map) {
		this.insertStmt_map_gp.clearBindings();
		this.insertStmt_map_gp.bindLong(1, (long)g_map);
		this.insertStmt_map_gp.bindLong(2, (long)po_map);
		return this.insertStmt_map_gp.executeInsert();
	}

	public List<Integer> select_map_gp_po(int g_map) {
		List<Integer> list = new ArrayList<Integer>();
		String selection = "g_map" + " = '" + g_map + "'";
		Cursor cursor = this.db.query(TABLE_NAME_MAP_GP, null, selection, null, null, null, null);
		if (cursor.moveToFirst()) {
			do
				list.add(cursor.getInt(cursor.getColumnIndex("po_map")));
			while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		return list;
	}

	public long insert_map_pp(int po_map, int pa_map) {
		this.insertStmt_map_pp.clearBindings();
		this.insertStmt_map_pp.bindLong(1, (long)po_map);
		this.insertStmt_map_pp.bindLong(2, (long)pa_map);
		return this.insertStmt_map_pp.executeInsert();
	}
	
	public List<Integer> select_map_pp_pa(int po_map) {
		List<Integer> list = new ArrayList<Integer>();
		String selection = "po_map" + " = '" + po_map + "'";
		Cursor cursor = this.db.query(TABLE_NAME_MAP_PP, null, selection, null, null, null, null);
		if (cursor.moveToFirst()) {
			do
				list.add(cursor.getInt(cursor.getColumnIndex("pa_map")));
			while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		return list;
	}
	
	public long insert_map_pe(int pa_map, int e_map) {
		this.insertStmt_map_pp.clearBindings();
		this.insertStmt_map_pp.bindLong(1, (long)pa_map);
		this.insertStmt_map_pp.bindLong(2, (long)e_map);
		return this.insertStmt_map_pe.executeInsert();
	}
	
	public List<Integer> select_map_pe_e(int pa_map) {
		List<Integer> list = new ArrayList<Integer>();
		String selection = "pa_map" + " = '" + pa_map + "'";
		Cursor cursor = this.db.query(TABLE_NAME_MAP_PE, null, selection, null, null, null, null);
		if (cursor.moveToFirst()) {
			do
				list.add(cursor.getInt(cursor.getColumnIndex("e_map")));
			while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		return list;
	}
	
	private static class OpenHelper extends SQLiteOpenHelper {
		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME_GARDEN + " (g_pk INT PRIMARY KEY, name TEXT, previewId INT, bounds TEXT)");
			db.execSQL("CREATE TABLE " + TABLE_NAME_PLOT + " (po_pk INT PRIMARY KEY, name TEXT, shape TEXT, type INT, color INT, polyPoints TEXT, rotation REAL, id INT)");
			db.execSQL("CREATE TABLE " + TABLE_NAME_PLANT + " (pa_pk INT PRIMARY KEY, name TEXT, id INT)");
			db.execSQL("CREATE TABLE " + TABLE_NAME_ENTRY + " (e_pk INT PRIMARY KEY, name TEXT, date TEXT)");
			db.execSQL("CREATE TABLE " + TABLE_NAME_MAP_GP + " (g_map INT, po_map INT, FOREIGN KEY(g_map) REFERENCES garden(g_pk), FOREIGN KEY(po_map) REFERENCES plot(po_pk))");
			db.execSQL("CREATE TABLE " + TABLE_NAME_MAP_PP + " (po_map INT, pa_map INT, FOREIGN KEY(po_map) REFERENCES plot(po_pk), FOREIGN KEY(pa_map) REFERENCES plant(pa_pk))");
			db.execSQL("CREATE TABLE " + TABLE_NAME_MAP_PE + " (pa_map INT, e_map INT, FOREIGN KEY(pa_map) REFERENCES plant(pa_pk), FOREIGN KEY(e_map) REFERENCES entry(e_pk))");		
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_GARDEN);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PLOT);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PLANT);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ENTRY);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MAP_GP);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MAP_PP);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MAP_PE);
			onCreate(db);
		}
	}
}
