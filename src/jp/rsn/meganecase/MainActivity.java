package jp.rsn.meganecase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.app_c.cloud.sdk.AppCCloud;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends MainActionActivity implements TextWatcher {

    private static final int REQUEST_SETTING_ACTIVITY = 3;
    private static final Pattern PATTERN_SCREENNAME = Pattern.compile("@[a-zA-z0-9_]+");

    private boolean extream = false;
    private EditText postText = null;
    private TextView postLen = null;
    private Long inReplyTo = null;
    private TextView inReplyText = null;

    private final Timer timer = new Timer(true);
    private ServiceConnection conn = null;
    private MediaPlayerManager mpm = null;
    private AppCCloud appCCloud;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TimerTask task = new TimerTask() {
            public void run() {
                Intent intent = new Intent("jp.rsn.meganecase.ALERT30");
                if (conn == null) {
                    conn = new ServiceConnection() {
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            Info alert = new Info();
                            try {
                                alert.setText(service.getInterfaceDescriptor());
                            }
                            catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            alert.setId(-1L);
                            if (data != null && data.getList().size() > 0) {
                                data.addInfo(alert);
                            }
                        }

                        public void onServiceDisconnected(ComponentName name) {
                        }
                    };
                }
                bindService(intent, conn, BIND_AUTO_CREATE);
            }
        };
        timer.schedule(task, 1000 * 60, 1000 * 60 * 30);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int type = Integer.parseInt(prefs.getString("mode_voice_type", "0"));
        mpm = new MediaPlayerManager(this, type);
        if (prefs.getBoolean("mode_voice", false)) {
            mpm.voiceStart();
        }
        appCCloud = new AppCCloud(this).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayAd();
    }

    @Override
    protected void onDestroy() {
        if (conn != null) {
            unbindService(conn);
        }
        mpm.release();
        timer.cancel();
        if (postText != null) {
            postText.removeTextChangedListener(this);
        }

        super.onDestroy();
        conn = null;
        mpm = null;
        postText = null;
        postLen = null;
        inReplyText = null;
        inReplyTo = null;
    }

    @Override
    public void finish() {
        super.finish();
        appCCloud.finish();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if ((event.getMetaState() & KeyEvent.META_ALT_ON) == 0) {
            return super.dispatchKeyEvent(event);
        }
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return super.dispatchKeyEvent(event);
        }
        switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_E:
            trackEvent("key-e");
            extream = !extream;
            changeExtreme(extream);
            break;
        default:
            break;
        }
        return super.dispatchKeyEvent(event);
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void afterTextChanged(Editable s) {
        if (postText == null || postLen == null) {
            return;
        }
        int lastLen = 140 - s.length();
        postLen.setText(Integer.toString(lastLen));
        if (lastLen < 0) {
            postLen.setTextColor(Color.RED);
        }
        else {
            postLen.setTextColor(Color.WHITE);
        }
        postLen.invalidate();
        if (s.length() == 0) {
            inReplyText.setText(null);
            inReplyText.setVisibility(View.GONE);
            inReplyTo = null;
        }
    }

    public void clickTweet(View v) {
        String tweet = postText.getText().toString();
        if (tweet.length() > 140) {
            return;
        }
        trackEvent("tweet");
        TweetTask task = new TweetTask(this, tweet, inReplyTo);
        task.setOnErrorListener(this);
        task.start();
        postText.setText(null);
        postLen.setText("140");
        inReplyText.setText(null);
        inReplyText.setVisibility(View.GONE);
        inReplyTo = null;
    }

    public void onReply(Info info) {
        trackEvent("button-reply");
        StringBuilder sb = new StringBuilder();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("mode_multireply", false)) {
            Matcher matcher = PATTERN_SCREENNAME.matcher(info.getText());
            List<String> ids = new ArrayList<String>();
            while (matcher.find()) {
                String str = matcher.group();
                if (str.equals("@" + info.getUsername())) {
                    continue;
                }
                if (twitter != null && str.equals("@" + twitter.getScreenName())) {
                    continue;
                }
                if (ids.contains(str)) {
                    continue;
                }
                ids.add(str);
                sb.append(str);
                sb.append(" ");
            }
        }
        String postString = "@" + info.getUsername() + " " + sb.toString();
        if (extream) {
            if (postText.getText() == null || postText.getText().length() == 0) {
                postText.setText(postString);
            }
            else {
                String tmp = postText.getText() + " " + postString;
                if (tmp.length() < 140) {
                    postText.setText(postText.getText() + " " + postString);
                }
            }
            postText.requestFocus();
            postText.setSelection(postText.getText().length());
            if (inReplyTo == null || inReplyTo == 0) {
                inReplyText.setText(SpanBuilder.buildInReplyText(info));
                if (Util.isEmpty(info.getText())) {
                    inReplyText.setVisibility(View.GONE);
                }
                else {
                    inReplyText.setVisibility(View.VISIBLE);
                }
                inReplyTo = info.getId();
            }
        }
        else {
            Intent intent = new Intent(this, PostActivity.class);
            intent.putExtra("POST_STRING", postString);
            intent.putExtra("POST_INREPRYTO", info.getId());
            intent.putExtra("POST_REPLY", true);
            intent.putExtra("POST_INREPRYTEXT", info.getText());
            startActivityForResult(intent, MainActionActivity.REQUEST_POST_ACTIVITY);
        }
    }

    public void onQt(Info info) {
        trackEvent("button-qt");
        String postString = null;
        if (info.isProtected()) {
            postString = " RT " + info.getText().substring(info.getUsername().length() + 1);
        }
        else {
            postString = " RT @" + info.getText();
        }
        if (extream) {
            postText.setText(postString);
            postText.requestFocus();
            inReplyText.setText(SpanBuilder.buildInReplyText(info));
            if (Util.isEmpty(info.getText())) {
                inReplyText.setVisibility(View.GONE);
            }
            else {
                inReplyText.setVisibility(View.VISIBLE);
            }
            inReplyTo = info.getId();
        }
        else {
            Intent intent = new Intent(this, PostActivity.class);
            intent.putExtra("POST_STRING", postString);
            intent.putExtra("POST_INREPRYTO", info.getId());
            intent.putExtra("POST_INREPRYTEXT", info.getText());
            startActivityForResult(intent, MainActionActivity.REQUEST_POST_ACTIVITY);
        }
    }

    public void onRt(Info info) {
        trackEvent("button-rt");
        String postString = null;
        if (info.isProtected()) {
            postString = " RT " + info.getText().substring(info.getUsername().length() + 1);
        }
        else {
            postString = " RT @" + info.getText();
        }
        if (extream) {
            postText.setText(postString);
            postText.requestFocus();
            inReplyText.setText(null);
            inReplyText.setVisibility(View.GONE);
            inReplyTo = null;
        }
        else {
            Intent intent = new Intent(this, PostActivity.class);
            intent.putExtra("POST_STRING", postString);
            startActivityForResult(intent, MainActionActivity.REQUEST_POST_ACTIVITY);
        }
    }

    public void onLead(Info info) {
        trackEvent("button-lead");
        String postString = null;
        if (info.isProtected()) {
            postString = "c RT " + info.getText().substring(info.getUsername().length() + 1);
        }
        else {
            postString = "c RT @" + info.getText();
        }
        if (extream) {
            postText.setText(postString);
            postText.requestFocus();
            inReplyText.setText(null);
            inReplyText.setVisibility(View.GONE);
            inReplyTo = null;
        }
        else {
            Intent intent = new Intent(this, PostActivity.class);
            intent.putExtra("POST_STRING", postString);
            startActivityForResult(intent, MainActionActivity.REQUEST_POST_ACTIVITY);
        }
    }

    public void onAaa(Info info) {
        trackEvent("button-aaa");
        String postString = null;
        if (info.isProtected()) {
            postString = "±±±¯ RT " + info.getText().substring(info.getUsername().length() + 1);
        }
        else {
            postString = "±±±¯ RT @" + info.getText();
        }
        if (extream) {
            postText.setText(postString);
            postText.requestFocus();
            inReplyText.setText(null);
            inReplyText.setVisibility(View.GONE);
            inReplyTo = null;
        }
        else {
            Intent intent = new Intent(this, PostActivity.class);
            intent.putExtra("POST_STRING", postString);
            startActivityForResult(intent, MainActionActivity.REQUEST_POST_ACTIVITY);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
        case R.id.menuPost:
            trackEvent("menu-post");
            post();
            return true;
        case R.id.menuAuth:
            trackEvent("menu-auth");
            intent = new Intent(this, OAuthActivity.class);
            startActivityForResult(intent, REQUEST_OAUTH_ACTIVITY);
            return true;
        case R.id.menuSite:
            trackEvent("menu-about");
            String url = "http://bonheur.rsn.jp/hisaki/android.html";
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        case R.id.menuPower:
            trackEvent("menu-power");
            try {
                Intent tepco = new Intent();
                tepco.setAction("jp.rsn.meganecase.den.VIEW");
                startActivity(tepco);
            }
            catch (ActivityNotFoundException e1) {
                Intent market = new Intent();
                market.setAction(Intent.ACTION_VIEW);
                market.setData(Uri.parse("market://details?id=jp.rsn.meganecase.den"));
                try {
                    market.setPackage("com.android.vending");
                    startActivity(market);
                }
                catch (ActivityNotFoundException e2) {
                    market.setPackage(null);
                    startActivity(market);
                }
            }
            return true;
        case R.id.menuSecret:
            trackEvent("menu-secret");
            showDialog(DialogManager.DIALOG_SECRET);
            return true;
        case R.id.menuExtream:
            trackEvent("menu-extream");
            extream = !extream;
            changeExtreme(extream);
            return true;
        case R.id.menuSetting:
            trackEvent("menu-setting");
            intent = new Intent(this, SettingActivity.class);
            startActivityForResult(intent, REQUEST_SETTING_ACTIVITY);
            return true;
        case R.id.menuRequest:
            trackEvent("menu-request");
            intent = new Intent(this, RequestActivity.class);
            startActivity(intent);
            return true;
        case R.id.menuTz:
            trackEvent("menu-timezone");
            String currentTz = TimeZone.getDefault().getID();
            final String[] tzs = TimeZone.getAvailableIDs();
            int currentId = 0;
            for (int i = 0; i < tzs.length; i++) {
                if (tzs[i].equals(currentTz)) {
                    currentId = i;
                    break;
                }
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setSingleChoiceItems(tzs, currentId, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                    am.setTimeZone(tzs[which]);
                    dialog.dismiss();
                }
            });
            builder.show();
            return true;
        case R.id.menuFavstar:
            trackEvent("menu-favstar");
            if (twitter != null) {
                String name = twitter.getScreenName();
                if (!Util.isEmpty(name)) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://favstar.fm/users/" + name + "/recent"));
                    startActivity(intent);
                }
            }
            return true;
        case R.id.menuRice:
            trackEvent("menu-rice");
            intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://www.amazon.co.jp/s/?_encoding=UTF8&camp=247&creative=7399&field-keywords=%E7%82%8A%E9%A3%AF%E5%99%A8&linkCode=ur2&tag=jollibee123-22&url=search-alias%3Daps"));
            startActivity(intent);
            return true;
        case R.id.menuGiga:
        	trackEvent("menu-giga");
			intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://details?id=hisaki.giganekeesu"));
            startActivity(intent);
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        switch (requestCode) {
        case REQUEST_SETTING_ACTIVITY:
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (twitter != null) {
                twitter.setVibe(prefs.getBoolean("mode_viberation", false));
                twitter.setBeast(prefs.getBoolean("mode_beast", false));
                twitter.setVoice(prefs.getBoolean("mode_voice", false));
            }
            adapter.setLightColor(prefs.getBoolean("mode_lightcolor", false));
            adapter.setSingleLine(prefs.getBoolean("mode_singleline", false));
            adapter.setRakuraku(prefs.getBoolean("mode_rakuraku", false));
            adapter.setInfoView(prefs.getBoolean("mode_infoview", false));
            adapter.setLargeIcon(prefs.getBoolean("mode_largeicon", false));
            int size = Integer.parseInt(prefs.getString("mode_text_size", "1"));
            adapter.setTextSize(size == 0 ? 0.8f : size == 1 ? 1.0f : 1.2f);
            adapter.setMute(prefs.getBoolean("mode_mute", false));
            mpm.release();
            int type = Integer.parseInt(prefs.getString("mode_voice_type", "0"));
            mpm = new MediaPlayerManager(this, type);
            adapter.notifyDataSetChanged();
            sendBroadcast(new Intent(OnseiWidget.ACTION_WIDGET_UPDATE));
            break;
        default:
            break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (extream && keyCode == KeyEvent.KEYCODE_BACK) {
            extream = false;
            changeExtreme(extream);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (extream) {
            String text = postText.getText().toString();
            String len = postLen.getText().toString();
            Long irt = inReplyTo;
            String irtText = inReplyText.getText().toString();
            changeExtreme(extream);
            postText.setText(text);
            postLen.setText(len);
            inReplyTo = irt;
            if (!Util.isEmpty(irtText)) {
                inReplyText.setText(irtText);
                inReplyText.setVisibility(View.VISIBLE);
            }
        }
        else {
            changeExtreme(extream);
        }
    }

    public final MediaPlayerManager getMediaPlayerManager() {
        return mpm;
    }

    private void changeExtreme(boolean extreme) {
        if (listView != null) {
            listView.setAdapter(null);
            listView.setOnItemClickListener(null);
        }
        if (extreme) {
            setContentView(R.layout.main_ex);
            postLen = (TextView) findViewById(R.id.postLen);
            postText = (EditText) findViewById(R.id.postText);
            postText.addTextChangedListener(this);
            inReplyText = (TextView) findViewById(R.id.inReplyText);
        }
        else {
            setContentView(R.layout.main);
            if (postText != null) {
                postText.removeTextChangedListener(this);
                postText.setText(null);
            }
            if (postLen != null) {
                postLen.setText(null);
            }
            if (inReplyText != null) {
                inReplyText.setText(null);
                inReplyText.setVisibility(View.GONE);
            }
            postText = null;
            postLen = null;
            inReplyText = null;
            inReplyTo = null;
        }
        listView = (ListView) findViewById(R.id.listView);
        listView.setFocusable(true);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        displayAd();
    }
    
    private void displayAd() {
        FrameLayout layout = (FrameLayout) findViewById(R.id.adArea);
        layout.removeAllViews();

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        layout.addView(appCCloud.Ad.loadMoveIconView(), params);
    }
}
