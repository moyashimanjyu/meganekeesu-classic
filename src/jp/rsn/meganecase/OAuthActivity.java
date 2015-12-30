package jp.rsn.meganecase;

import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class OAuthActivity extends BaseActivity {

    private RequestToken requestToken = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ("jp.rsn.meganecase.OAUTH".equals(getIntent().getAction())) {
            Bundle extras = getIntent().getExtras();
            if (extras != null && "xxxxxxxxxxxxxxxxxxxx".equals(extras.getString("P0"))) {
                SharedPreferences pref = getSharedPreferences(App.TAG, MODE_PRIVATE);
                String token = pref.getString("token", null);
                String tokenSecret = pref.getString("tokenSecret", null);
                if (token != null && tokenSecret != null) {
                    Intent data = new Intent();
                    data.putExtra("P1", "xxxxxxxxxxxxxxxxxxxx");
                    data.putExtra("P2", token);
                    data.putExtra("P3", tokenSecret);
                    setResult(RESULT_OK, data);
                    finish();
                }
                else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
            else {
                setResult(RESULT_CANCELED);
                finish();
            }
        }

        setContentView(R.layout.oauth);
        try {
            requestToken = MeganeCaseTwitter.getRequestToken();
        }
        catch (TwitterException e) {
            e.printStackTrace();
        }
        if (requestToken == null) {
            trackEvent("auth-error");
            showError();
            finish();
            return;
        }
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new OAuthWebViewClient());
        webView.loadUrl(requestToken.getAuthorizationURL());
        webView.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        requestToken = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.oauth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.oauthClear:
            trackEvent("oauth-clear");
            SharedPreferences pref = getSharedPreferences(App.TAG, MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.remove("token");
            editor.remove("tokenSecret");
            editor.commit();
            Toast.makeText(this, "OAuth Token Cleared.", Toast.LENGTH_LONG).show();
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class OAuthWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.v(App.TAG, "onPageStarted: " + url);
            if (url.startsWith("http://bonheur.rsn.jp/hisaki/oauth.html")) {
                view.stopLoading();
                String verifier = Uri.parse(url).getQueryParameter("oauth_verifier");
                try {
                    AccessToken accessToken = MeganeCaseTwitter.getAccessToken(requestToken,
                            verifier);
                    String token = accessToken.getToken();
                    String tokenSecret = accessToken.getTokenSecret();
                    SharedPreferences pref = getSharedPreferences(App.TAG, MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("token", token);
                    editor.putString("tokenSecret", tokenSecret);
                    editor.commit();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                trackEvent("authed");
                setResult(RESULT_OK, new Intent());
                finish();
            }
            super.onPageStarted(view, url, favicon);
        }
    }
}
