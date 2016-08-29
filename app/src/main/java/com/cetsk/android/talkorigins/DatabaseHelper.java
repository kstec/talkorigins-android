package com.cetsk.android.talkorigins;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 10;
	
	static final String DBNAME = "talkorigins.db";
	static final String FAVORITES_TABLE = "Favorites";
	static final String CLAIMS_TABLE = "Claims";
	
	private final Context myContext;
	
	// Database creation sql statement
	static final String CREATE_FAVOURITES = "CREATE TABLE IF NOT EXISTS " + FAVORITES_TABLE + " (Id INTEGER PRIMARY KEY AUTOINCREMENT, ClaimId INTEGER)";
	static final String CREATE_CLAIMS = "CREATE TABLE IF NOT EXISTS " + CLAIMS_TABLE + " (Id INTEGER PRIMARY KEY AUTOINCREMENT, ParentId INTEGER, Key TEXT, Name TEXT, Url TEXT, Content TEXT)";
	static final String CREATE_INDEX = "CREATE UNIQUE INDEX IF NOT EXISTS idx_url ON " + FAVORITES_TABLE + " (ClaimId)";
	
	
	public DatabaseHelper(Context context) {
		super(context, DBNAME, null, DATABASE_VERSION);
		this.myContext = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + FAVORITES_TABLE );
		db.execSQL("DROP TABLE IF EXISTS " + CLAIMS_TABLE );
		
		db.execSQL( CREATE_FAVOURITES );
		Log.w( this.getClass().getName(), "Created new table: " + FAVORITES_TABLE);
		db.execSQL( CREATE_CLAIMS );
		db.execSQL( CREATE_INDEX );
		Log.w( this.getClass().getName(), "Created new table: " + CLAIMS_TABLE);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w( this.getClass().getName(), String.format("Migrating DB from %d to %s",oldVersion,newVersion ));
		if(newVersion <  DATABASE_VERSION ){
			db.execSQL("DROP TABLE IF EXISTS " + FAVORITES_TABLE );
			db.execSQL("DROP TABLE IF EXISTS " + CLAIMS_TABLE );
		}
		onCreate(db);
		
	}
	
	
}