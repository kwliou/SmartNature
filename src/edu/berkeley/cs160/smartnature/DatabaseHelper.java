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
import android.graphics.RectF;

public class DatabaseHelper {
	private static final String DATABASE_NAME = "garden_gnome.db";
	private static final int DATABASE_VERSION = 1;

	private static final String TABLE_NAME_GARDEN = "garden";
	private static final String TABLE_NAME_PLOT = "plot";
	private static final String TABLE_NAME_PLANT = "plan";
	private static final String INSERT_GARDEN = "insert into " + TABLE_NAME_GARDEN + " (g_pk, name, previewId, plots, bounds) values (NULL, ?, ?, ?, ?)";
	private static final String INSERT_PLOT = "insert into " + TABLE_NAME_PLOT + " (po_pk, name, shape, type, color, polyPoints, rotation, id, plants) values (NULL, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT_PLANT = "insert into " + TABLE_NAME_PLANT + " (pan_pk, name, id, entries) values (NULL, ?, ?, ?)";

	private Context context;
	private SQLiteDatabase db;
	private SQLiteStatement insertStmt_garden;

	public DatabaseHelper(Context context) {
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
		this.insertStmt_garden = this.db.compileStatement(INSERT_GARDEN);
	}

	public long insert_garden(String name, int previewId, String plots, String bounds) {
		this.insertStmt_garden.clearBindings();
		this.insertStmt_garden.bindString(1, name);
		this.insertStmt_garden.bindLong(2, (long)previewId);
		this.insertStmt_garden.bindString(3, plots);
		this.insertStmt_garden.bindString(4, bounds);
		return this.insertStmt_garden.executeInsert();
	}

	public long update_to_do(int g_pk, String name, int previewId, String plots, String bounds) {
		ContentValues cv = new ContentValues();
		cv.put("name", name);
		cv.put("previewId", previewId);
		cv.put("plots", plots);
		cv.put("bounds", bounds);
		String selection = "g_pk = ?";
		return db.update(TABLE_NAME_GARDEN, cv, selection, new String[] {Integer.toString(g_pk)});
	}

	public void delete_all_garden() {
		this.db.delete(TABLE_NAME_GARDEN, null, null);
	}

	public void delete_garden(int g_pk) {
		String selection = "g_pk = ?";
		this.db.delete(TABLE_NAME_GARDEN, selection, new String[] {Integer.toString(g_pk)});
	}

	public List<String> select_all_garden(String column) {
		List<String> list = new ArrayList<String>();
		Cursor cursor = this.db.query(TABLE_NAME_GARDEN, new String[] {column}, null, null, null, null, "td_id asc");
		if (cursor.moveToFirst()) {
			do 
				list.add(cursor.getString(cursor.getColumnIndex(column)));
			while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) 
			cursor.close();
		return list;
	}

	public Garden select_garden(String name) {
		String selection = "name" + " = '" + name + "'";
		Cursor cursor = this.db.query(TABLE_NAME_GARDEN, null, selection, null, null, null, null);
		Garden temp = null;
		if (cursor.moveToFirst()) {
			do {
				temp = new Garden(Integer.parseInt(cursor.getString(cursor.getColumnIndex("previewId"))), cursor.getString(cursor.getColumnIndex("name")));
				//list.add(cursor.getString(cursor.getColumnIndex("plots")));
				String[] bound_s = cursor.getString(cursor.getColumnIndex("bounds")).split(",");
				RectF bound_rf = new RectF(Integer.parseInt(bound_s[0]), Integer.parseInt(bound_s[1]), Integer.parseInt(bound_s[2]), Integer.parseInt(bound_s[3]));
				temp.setRawBounds(bound_rf);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		return temp;
	}

	private static class OpenHelper extends SQLiteOpenHelper {
		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME_GARDEN + " (g_pk INTEGER PRIMARY KEY, name TEXT, previewId NUM, plots TEXT, bounds TEXT)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_GARDEN);
			onCreate(db);
		}
	}
}
