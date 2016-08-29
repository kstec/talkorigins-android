package com.cetsk.android.talkorigins;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteStatement;
import android.support.design.widget.Snackbar;
import android.util.Log;

import java.util.ArrayList;

public class LikeHelper {
	public LikeHelper() {
		
	}

	public static Boolean isLiked(Claim claim, DatabaseHelper dbh) {

		try {
			SQLiteStatement stmt = dbh.getWritableDatabase().compileStatement(
					"select count(*) from Favorites f where f.ClaimId = ?");
			stmt.bindLong(1, claim.getId());
			long count = stmt.simpleQueryForLong();
			dbh.close();

			if (count == 0) {
				return false;
			}
			return true;
		} catch (SQLException sqle) {
			Log.e("SQLError", sqle.getMessage());
			throw sqle;
		}
	}

	public static void toggleFavorite(Claim claim, DatabaseHelper dbh) {
		boolean isLiked = !isLiked(claim, dbh);
		try {
			if (isLiked(claim, dbh)) {
				delete(claim, dbh);
			} else {
				create(claim, dbh);
			}
		} catch (SQLException sqle) {
			Log.e("SQLError", sqle.getMessage());
		} finally {
			dbh.close();
		}
	}

	private static void create(Claim claim, DatabaseHelper dbh) {
		SQLiteStatement stmt = dbh.getWritableDatabase().compileStatement(
				"INSERT INTO " + DatabaseHelper.FAVORITES_TABLE + " (Id,ClaimId) VALUES (null,?)");
		stmt.bindLong(1, claim.getId());
		stmt.execute();
		stmt.close();
		dbh.close();
	}

	private static void delete(Claim claim, DatabaseHelper dbh) {
		SQLiteStatement stmt = dbh.getWritableDatabase().compileStatement(
				"DELETE FROM " + DatabaseHelper.FAVORITES_TABLE + " WHERE ClaimId= ?");
		stmt.bindLong(1, claim.getId());
		stmt.execute();
		stmt.close();
		dbh.close();
	}
}