package com.cetsk.android.talkorigins;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

public class HTMLParse {
	Context appContext = null;
	DatabaseHelper dbh = null;
	File filesDirectory = null;
	int counter = 0;
	SQLiteStatement claimInsertStatement;

	HTMLParse(Context context) {
		filesDirectory = context.getFilesDir();
		Log.i("FILES_DIR", filesDirectory.getAbsolutePath());
		appContext = context;
		dbh = new DatabaseHelper(appContext);
		claimInsertStatement = dbh.getWritableDatabase().compileStatement(
				"INSERT INTO " + DatabaseHelper.CLAIMS_TABLE
						+ " (Id,ParentId,Key,Name,Url,Content) VALUES (null,?,?,?,?,?)");
		Integer claimCount = getClaimCount();
		if (claimCount != 638) {
			if (claimCount > 0) {
				clearAllClaims();
			} else {
				Log.w(this.getClass().getName(), "Not enough claims but nothing to delete!");
			}
			addClaims();
			Log.w(this.getClass().getName(), String.format("%d claims have been loaded", getClaimCount()));
		}
		claimInsertStatement.close();
		dbh.close();
	}

	private void clearAllClaims() {
		SQLiteStatement stmt = dbh.getWritableDatabase().compileStatement(
				"DELETE FROM " + DatabaseHelper.CLAIMS_TABLE + "; VACUUM");
		stmt.execute();
		stmt.close();
		Log.i(this.getClass().getName(), "Claims purged.");
	}

	public int getClaimCount() {
		SQLiteStatement stmt = dbh.getWritableDatabase().compileStatement(
				"select count(*) from " + DatabaseHelper.CLAIMS_TABLE);
		Integer claimCount = (int) stmt.simpleQueryForLong();
		stmt.close();
		return claimCount;
	}

	public String getClaimContent(Claim claim) {
		File f = new File(filesDirectory.getAbsolutePath() + "/" + claim.getUrl());
		f.getParentFile().mkdirs();
        f.delete();

        if (!f.exists() && f.length() < 1) {
			Log.d(this.getClass().getName(), "File does not exist: " + f.getAbsolutePath());
			writeFile(f, claim.getFullUrl());
		}
        Log.i("LOG",f.toString());
		try {
			Log.d(this.getClass().getName(), "File exists; reading from cache: " + f.getAbsolutePath());
			return readFile(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void writeFile(File filePath, String url) {
		String html = getHtml(url);
		try {
			Log.d(this.getClass().getName(), "Writing file: " + filePath.getAbsolutePath());
			FileWriter fstream = new FileWriter(filePath.getAbsolutePath());
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(html);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String readFile(File f) throws IOException {
		FileInputStream stream = new FileInputStream(f);
		try {
			StringBuffer fileContent = new StringBuffer("");

			byte[] buffer = new byte[1024];
			int length;
			while ((length = stream.read(buffer)) != -1) {
				fileContent.append(new String(buffer, 0, length));
			}
			return fileContent.toString();
		} finally {
			stream.close();
		}
	}

	public String getHtml(String url) {
		Document doc;
		try {
			doc = Jsoup.parse(new URL(url), 10000);
			String html = doc.toString();

			int startPos = html.indexOf("<h2 class=\"c\">");
			int endPos = html.lastIndexOf("<div align=\"center\">");
			if (startPos > 0 && endPos > 0) {
				html = html.substring(startPos, endPos);
			}
			html = html.replaceAll("\\.\\./", "http://talkorigins.org/indexcc/");
			return html;
		} catch (Exception e) {
            Log.e("ERROR", e.getMessage());
            Log.e("ERROR", url);
			e.printStackTrace();
		}
		return null;
	}

	public int getLastInsertedClaimId() {
		SQLiteStatement stmt = dbh.getWritableDatabase().compileStatement(
				"SELECT Id FROM " + DatabaseHelper.CLAIMS_TABLE + " ORDER BY Id DESC LIMIT 1");
		Integer lastClaimId = (int) stmt.simpleQueryForLong();
		stmt.close();
		return lastClaimId;
	}

	public void addClaims() {
		Document doc;
		try {
			doc = Jsoup.parse(appContext.getAssets().open("list.html"), "UTF-8", "");
			Elements cats = doc.getElementsByTag("h2");

			for (Element e : cats) {
				Elements anchors = e.getElementsByTag("a");
				Element anchor = anchors.get(0);
				Elements subClaims = e.nextElementSibling().children();

				try {
					dbh.getWritableDatabase().beginTransaction();
					int parentId = 0;
					claimInsertStatement.bindLong(1, parentId);
					claimInsertStatement.bindString(2, anchor.attr("name"));
					claimInsertStatement.bindString(3, e.text());
					claimInsertStatement.bindString(4, "");
					claimInsertStatement.executeInsert();
					recurseClaims(subClaims, getLastInsertedClaimId());
					dbh.getWritableDatabase().setTransactionSuccessful();
				} finally {
					dbh.getWritableDatabase().endTransaction();
				}
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception e) {
			Log.e("ERROR", e.getMessage());
		}
	}

	public void recurseClaims(final Elements elements, final int parentId) {
		for (final Element e : elements) {
			Elements anchors = e.getElementsByTag("a");
			if (anchors.size() > 0) {
				counter++;
				Element anchor = anchors.get(0);
				claimInsertStatement.bindLong(1, parentId);
				claimInsertStatement.bindString(2, anchor.attr("name"));
				if (anchor.attr("href").equals("")) {
					claimInsertStatement.bindString(3, anchor.text() + "" + e.ownText());
				} else {
					claimInsertStatement.bindString(3, e.ownText() + "" + anchor.text());
				}
				claimInsertStatement.bindString(4, anchor.attr("href"));
				claimInsertStatement.executeInsert();
				Elements ch = e.getElementsByTag("ul");
				if (ch.size() > 0) {
					recurseClaims(ch.get(0).children(), getLastInsertedClaimId());
				}
			} else {
				continue;
			}
		}
	}

	public void close() {
		// TODO Auto-generated method stub
	}

}
