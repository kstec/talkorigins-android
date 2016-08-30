package com.cetsk.android.talkorigins;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ProgressDialog dialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_scrolling);

        Claim claim = (Claim) getIntent().getSerializableExtra("claim");

        final ListView lv = (ListView) findViewById(R.id.listView);
        ClaimAdapter ca = new ClaimAdapter(getBaseContext(), R.layout.list_item, ClaimHelper.getClaims(this, 0, true));
        if (claim != null) {
            ca = new ClaimAdapter(getBaseContext(), R.layout.list_item, ClaimHelper.getClaims(this,claim.getId(), true));
            setTitle(claim.getName());
        }
        lv.setAdapter(ca);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Claim claim = (Claim) ((LinearLayout) view).getTag();
                    if(claim.getChildren().size() == 1) {
                        Intent intent = new Intent(parent.getContext(), ClaimViewActivity.class);
                        intent.putExtra("claim", claim.getChildren().get(0));
                        startActivityForResult(intent, 0);
                    } else if (claim.getChildren().size() != 0) {
                        Intent intent = new Intent(parent.getContext(), MainActivity.class);
                        intent.putExtra("claim", claim);
                        startActivityForResult(intent, 0);
                    } else {
                        Intent intent = new Intent(parent.getContext(), ClaimViewActivity.class);
                        intent.putExtra("claim", claim);
                        startActivityForResult(intent, 0);
                    }
                } catch (Exception e) {
                     Log.e("ERROR", e.getMessage());
                }
            }
        });

        lv.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                int i = 0;
                Claim c = (Claim) info.targetView.getTag();
                try {
                    if (c.getChildren().size() > 0) {
                        menu.add(Menu.NONE, 1, ++i, "View Children");
                    }
                    if (c.getUrl().length() > 1) {
                        menu.add(Menu.NONE, 1, ++i, "View Claim");
                        menu.add(Menu.NONE, 1, ++i, "Share");
                        menu.add(Menu.NONE, 1, ++i, "Add as Favorite");
                        menu.add(Menu.NONE, 1, ++i, "Open in Browser");
                    }
                } catch (Exception e) {
                    Log.e("Problem parsing claim", e.toString());
                }
            }
        });


    }

    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Claim c = (Claim) info.targetView.getTag();
        Log.e("ERROR", "Item not found: "+item.toString());
        if (item.toString().equals("View Claim")) {
            Intent intent = new Intent(info.targetView.getContext(), ClaimViewActivity.class);
            intent.putExtra("claim", c);
            startActivity(intent);
        } else if (item.toString().equals("View Children")) {
            Intent intent = new Intent(info.targetView.getContext(), MainActivity.class);
            intent.putExtra("claim", c);
            startActivity(intent);
        } else if (item.toString().equals("Add as Favorite")) {
            DatabaseHelper dbh = new DatabaseHelper(this);
            try {
                SQLiteStatement stmt = dbh.getWritableDatabase().compileStatement(
                        "INSERT INTO " + DatabaseHelper.FAVORITES_TABLE + " (Id,ClaimId) VALUES (null,?)");
                stmt.bindLong(1, c.getId());
                stmt.execute();
                Toast.makeText(getApplicationContext(), "Favorite Added", Toast.LENGTH_SHORT).show();
            } catch (SQLException sqle) {
                if (sqle.getMessage().startsWith("error code 19:")) {
                    Toast.makeText(getApplicationContext(), "Could not add favorite, because it already exists!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error adding favorite: " + sqle.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
                Log.e("SQLError", sqle.getMessage());
            } finally {
                dbh.close();
            }
        } else if (item.toString().equals("Share")) {
            Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "TalkOrigins article: " + c.getName());
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, c.getFullUrl());
            startActivity(Intent.createChooser(shareIntent, "Share via..."));
        } else if (item.toString().equals("Open in Browser")) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(c.getFullUrl()));
            startActivity(browserIntent);
        } else {
            Log.e("ERROR", "Item not found: "+item.toString());
        }
        return true;
    }

}

