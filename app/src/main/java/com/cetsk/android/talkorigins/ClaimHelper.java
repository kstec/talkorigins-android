package com.cetsk.android.talkorigins;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import java.util.ArrayList;

public class ClaimHelper {
	public ClaimHelper() {
		
	}
	public static ArrayList<Claim> getClaims(Context context, int parentId, boolean recurse){
		ArrayList<Claim> claims = new ArrayList<Claim>();
		DatabaseHelper dbh = new DatabaseHelper(context);
		try {
			Cursor cu = dbh.getWritableDatabase().query(DatabaseHelper.CLAIMS_TABLE, null, "ParentId="+parentId, null, null, null, "Name ASC");
			if (cu != null && cu.getCount() > 0) {
				cu.moveToFirst();
				do { 
					int Id = cu.getInt(cu.getColumnIndex("Id")); 
					String Name = cu.getString(cu.getColumnIndex("Name")); 
					String Url = cu.getString(cu.getColumnIndex("Url"));
					Claim c = new Claim();
					c.setId(Id);
					c.setName(Name);
					c.setUrl(Url);
					c.setParentId(parentId);
					if(recurse){
						c.setChildren(getClaims(context, Id, false));
					}
					claims.add(c);
				} while (cu.moveToNext()); 
			}
			cu.close();
		} catch(SQLException sqle){
			
		} finally { 
            if (dbh != null) 
            	dbh.close(); 
		}
		return claims;
	}
	public static ArrayList<Claim> searchClaims(Context context, String query, boolean recurse){
		ArrayList<Claim> claims = new ArrayList<Claim>();
		DatabaseHelper dbh = new DatabaseHelper(context);
		try {
			Cursor cu = dbh.getWritableDatabase().query(DatabaseHelper.CLAIMS_TABLE, null, "Name LIKE '%"+query+"%'", null, null, null, "Name ASC");
			if (cu != null && cu.getCount() > 0) {
				cu.moveToFirst();
				do { 
					int Id = cu.getInt(cu.getColumnIndex("Id")); 
					String Name = cu.getString(cu.getColumnIndex("Name")); 
					String Url = cu.getString(cu.getColumnIndex("Url"));
					Claim c = new Claim();
					c.setId(Id);
					c.setName(Name);
					c.setUrl(Url);
					if(recurse){
						c.setChildren(getClaims(context, Id, false));
					}
					claims.add(c);
				} while (cu.moveToNext()); 
			}
			cu.close();
		} catch(SQLException sqle){
			
		} finally { 
            if (dbh != null) 
            	dbh.close(); 
		}
		return claims;
	}
}