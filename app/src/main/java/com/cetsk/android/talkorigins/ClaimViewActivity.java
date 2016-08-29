package com.cetsk.android.talkorigins;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

public class ClaimViewActivity extends AppCompatActivity {
    WebView webview;
    private ProgressDialog dialog;
    boolean isLiked = false;
    String html = null;
    MenuItem likeButton = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        dialog = ProgressDialog.show(this, "Loading", "Loading claim...", true);
        final Claim claim = (Claim) getIntent().getSerializableExtra("claim");
        Log.d("LOAD_CLAIM", claim.toString());
        new Thread() {
            public void run() {
                try {
                    setTitle(claim.getName());
                    HTMLParse hp = new HTMLParse(getApplicationContext());
                    html = hp.getClaimContent(claim);
                    hp.close();
                } catch (Exception e) {
                    Log.e(ClaimViewActivity.class.getName(), "HTTP Error", e);
                    e.printStackTrace();
                }

                handler.sendEmptyMessage(0);
            }
        }.start();

        super.onCreate(savedInstanceState);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setContentView(R.layout.claim_view);
            webview = (WebView) findViewById(R.id.webview);
            webview.setWebViewClient(new WebViewClient() {
                // Dismisses the loading dialog after the webview loads
                public void onPageFinished(WebView view, String url) {
                    try {
                        dialog.dismiss();
                    } catch (Exception e) {
                        // Doing this triggers and exception on orientation change. Not sure why
                    }
                }

                // Will force any hyperlinks clicked in webview to prompt via
                // intent
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                    return true;
                }
            });
            if (html != null && html.length() > 0) {
                webview.loadData("<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/skeleton/2.0.4/skeleton.min.css\">" + html, "text/html", "utf-8");
            } else {
                Toast.makeText(getApplicationContext(), R.string.load_error, Toast.LENGTH_SHORT).show();
                Log.i("HTML_ERROR", "Could not Parse");
                dialog.dismiss();
                finish();
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.claim_menu, menu);
        likeButton = (MenuItem) menu.findItem(R.id.favorite);

        //ShareActionProvider mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.share).getActionProvider();
        //mShareActionProvider.setShareIntent(getDefaultShareIntent());
        Claim claim = (Claim) getIntent().getSerializableExtra("claim");
        DatabaseHelper dbh = new DatabaseHelper(this);
        if (LikeHelper.isLiked(claim, dbh)) {
            likeButton.setTitle(getString(R.string.unlike));
        }
        return true;
    }

    public Intent getDefaultShareIntent() {
        Claim claim = (Claim) getIntent().getSerializableExtra("claim");
        String url = "http://www.talkorigins.org/indexcc/" + claim.getUrl();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "TalkOrigins article: " + claim.getName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);

        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.favorite:
                onAddFavorite(webview);
                return true;
            case R.id.share:
                onShare(webview);
                return true;
            case R.id.url:
                onClipboard(webview);
                return true;
            case R.id.open:
                onOpen(webview);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onAddFavorite(View v) {
        Claim claim = (Claim) getIntent().getSerializableExtra("claim");
        DatabaseHelper dbh = new DatabaseHelper(this);
        LikeHelper.toggleFavorite(claim, dbh);
        if (LikeHelper.isLiked(claim, dbh)) {
            Snackbar.make(webview, R.string.liked, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            likeButton.setTitle(getString(R.string.like));
        } else {
            Snackbar.make(webview, R.string.unliked, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            likeButton.setTitle(getString(R.string.unlike));
        }
    }

    public void onShare(View v) {
        Claim claim = (Claim) getIntent().getSerializableExtra("claim");
        String url = "http://www.talkorigins.org/indexcc/" + claim.getUrl();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "TalkOrigins article: " + claim.getName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);

        startActivity(Intent.createChooser(shareIntent, "Share Claim"));
    }

    public void onClipboard(View v) {
        Claim claim = (Claim) getIntent().getSerializableExtra("claim");
        String url = "http://www.talkorigins.org/indexcc/" + claim.getUrl();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.setText(url);
        Snackbar.make(v, "URL \"" + url + "\" copied to clipboard.", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void onOpen(View v) {
        Claim claim = (Claim) getIntent().getSerializableExtra("claim");
        String url = "http://www.talkorigins.org/indexcc/" + claim.getUrl();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    public void onViewSiblings(View v) {
        Claim claim = (Claim) getIntent().getSerializableExtra("claim");
        Intent intent = new Intent(v.getContext(), MainActivity.class);
        Claim c = new Claim();
        c.setId(claim.getParentId());
        intent.putExtra("claim", c);
        startActivity(intent);
    }
}
