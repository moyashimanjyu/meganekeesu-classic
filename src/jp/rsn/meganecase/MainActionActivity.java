package jp.rsn.meganecase;

import java.net.URLEncoder;

import twitter4j.Status;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.AdapterView;

public class MainActionActivity extends MainBaseActivity implements MainActionListener,
        DialogManager.DialogAction, AdapterView.OnItemClickListener, TaskErrorListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter.setListener(this);
        dialogManager.setDialogAction(this);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if ((event.getMetaState() & KeyEvent.META_ALT_ON) == 0) {
            return super.dispatchKeyEvent(event);
        }
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return super.dispatchKeyEvent(event);
        }
        if ((event.getMetaState() & KeyEvent.META_SHIFT_ON) != 0) {
            switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_F:
                trackEvent("key-shift+f");
                onFavrt(data.getSelectedInfo());
                return true;
            case KeyEvent.KEYCODE_R:
                trackEvent("key-shift+r");
                onRetweet(data.getSelectedInfo());
                return true;
            default:
                break;
            }
            return super.dispatchKeyEvent(event);
        }
        switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_R:
            if (data.getSelectedInfo() != null) {
                trackEvent("key-r");
                onReply(data.getSelectedInfo());
                return true;
            }
            break;
        case KeyEvent.KEYCODE_F:
            if (data.getSelectedInfo() != null) {
                trackEvent("key-f");
                onFavorite(data.getSelectedInfo());
                return true;
            }
            break;
        case KeyEvent.KEYCODE_Q:
            if (data.getSelectedInfo() != null) {
                trackEvent("key-q");
                onQt(data.getSelectedInfo());
                return true;
            }
            break;
        default:
            break;
        }
        return super.dispatchKeyEvent(event);
    }

    public void onReply(Info info) {
        trackEvent("button-reply");
        String postString = "@" + info.getUsername() + " ";
        Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra("POST_STRING", postString);
        intent.putExtra("POST_INREPRYTO", info.getId());
        intent.putExtra("POST_REPLY", true);
        intent.putExtra("POST_INREPRYTEXT", info.getText());
        startActivityForResult(intent, MainActionActivity.REQUEST_POST_ACTIVITY);
    }

    public void onBlank(Info info) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean nodialog = prefs.getBoolean("mode_nodialog", false);
        if (nodialog) {
            blankAction(info);
        }
        else {
            showDialog(DialogManager.DIALOG_BLANK);
        }
    }

    public void onFavorite(Info info) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean nodialog = prefs.getBoolean("mode_nodialog", false);
        if (nodialog) {
            favoriteAction(info);
        }
        else {
            showDialog(DialogManager.DIALOG_FAVORITE);
        }
    }

    public void onFavrt(Info info) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean nodialog = prefs.getBoolean("mode_nodialog", false);
        if (nodialog) {
            favrtAction(info);
        }
        else {
            showDialog(DialogManager.DIALOG_FAVRT);
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
        Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra("POST_STRING", postString);
        intent.putExtra("POST_INREPRYTO", info.getId());
        intent.putExtra("POST_INREPRYTEXT", info.getText());
        startActivityForResult(intent, MainActionActivity.REQUEST_POST_ACTIVITY);
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
        Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra("POST_STRING", postString);
        startActivityForResult(intent, MainActionActivity.REQUEST_POST_ACTIVITY);
    }

    public void onLead(Info info) {
        trackEvent("button-lead");
        String postString = null;
        if (info.isProtected()) {
            postString = "Åc RT " + info.getText().substring(info.getUsername().length() + 1);
        }
        else {
            postString = "Åc RT @" + info.getText();
        }
        Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra("POST_STRING", postString);
        startActivityForResult(intent, MainActionActivity.REQUEST_POST_ACTIVITY);
    }

    public void onAaa(Info info) {
        trackEvent("button-aaa");
        String postString = null;
        if (info.isProtected()) {
            postString = "±±±Ø RT " + info.getText().substring(info.getUsername().length() + 1);
        }
        else {
            postString = "±±±Ø RT @" + info.getText();
        }
        Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra("POST_STRING", postString);
        startActivityForResult(intent, MainActionActivity.REQUEST_POST_ACTIVITY);
    }

    public void onPlugin(Info info) {
        trackEvent("button-plugin");
        Intent intent = new Intent();
        intent.setAction("jp.r246.twicca.ACTION_SHOW_TWEET");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        Status status = info.getStatus();
        intent.putExtra(Intent.EXTRA_TEXT, status.getText());
        intent.putExtra("user_screen_name", status.getUser().getScreenName());
        intent.putExtra("user_name", status.getUser().getName());
        intent.putExtra("user_profile_image_url", status.getUser().getProfileImageURL().toString());
        intent.putExtra("user_profile_image_url_mini", status.getUser().getProfileImageURL()
                .toString());
        intent.putExtra("user_profile_image_url_normal", status.getUser().getProfileImageURL()
                .toString());
        intent.putExtra("user_profile_image_url_bigger", status.getUser().getProfileImageURL()
                .toString());
        intent.putExtra("id", Long.toString(status.getId()));
        if (status.getGeoLocation() != null) {
            intent.putExtra("latitude", Double.toString(status.getGeoLocation().getLatitude()));
            intent.putExtra("longitude", Double.toString(status.getGeoLocation().getLongitude()));
        }
        intent.putExtra("created_at", Long.toString(status.getCreatedAt().getTime()));
        intent.putExtra("source", status.getSource().replaceAll("<.+?>", ""));
        if (status.getInReplyToStatusId() > 0) {
            intent.putExtra("in_reply_to_status_id", Long.toString(status.getInReplyToStatusId()));
        }
        startActivity(Intent.createChooser(intent, null));
    }

    public void onTranslate(Info info) {
        trackEvent("button-translate");
        String text = info.getText().substring(info.getUsername().length() + 2);
        String lang = info.getLang();
        String url = "http://translate.google.co.jp/?text=" + URLEncoder.encode(text);
        if (lang != null) {
            if (lang.equals("ja")) {
                url = url + "&sl=" + lang + "&tl=en";
            }
            else {
                url = url + "&sl=" + lang + "&tl=ja";
            }
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    public void onRetweet(Info info) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean nodialog = prefs.getBoolean("mode_nodialog", false);
        if (nodialog) {
            retweetAction(info);
        }
        else {
            showDialog(DialogManager.DIALOG_RETWEET);
        }
    }

    public void onDelete(Info info) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean nodialog = prefs.getBoolean("mode_nodialog", false);
        if (nodialog) {
            deleteAction(info);
        }
        else {
            showDialog(DialogManager.DIALOG_DELETE);
        }
    }

    public void onHistory(Info info) {
        trackEvent("button-history");
        if (info.getInReplyTo() != null && info.getInReplyTo() != -1) {
            showDialog(DialogManager.DIALOG_HISTORY);
        }
    }

    public void retweetAction(Info info) {
        trackEvent("button-retweet");
        RetweetTask task = new RetweetTask(this);
        task.setOnErrorListener(this);
        task.execute(info);
        clearSelectedInfo();
    }

    public void favoriteAction(Info info) {
        trackEvent("button-favorite");
        FavoriteTask task = new FavoriteTask(this);
        task.setOnErrorListener(this);
        task.execute(info);
        clearSelectedInfo();
    }

    public void blankAction(Info info) {
        trackEvent("button-blank");
        BlankTask task = new BlankTask(this);
        task.setOnErrorListener(this);
        task.execute(info);
        clearSelectedInfo();
    }

    public void favrtAction(Info info) {
        trackEvent("button-favrt");
        FavrtTask task = new FavrtTask(this);
        task.setOnErrorListener(this);
        task.execute(info);
        clearSelectedInfo();
    }

    public void deleteAction(Info info) {
        trackEvent("button-delete");
        DeleteTask task = new DeleteTask(this);
        task.setOnErrorListener(this);
        task.execute(info);
        clearSelectedInfo();
    }

    public void secretAction(String text) {
        if (twitter != null) {
            twitter.closeStreaming();
        }
        data.cancel();
        data.getList().clear();
        clearSelectedInfo();
        App.Token token = App.getToken(this);
        if (text == null || text.length() == 0) {
            twitter = new MeganeCaseTwitter(this, data);
        }
        else {
            twitter = new MeganeCaseTwitterSecret(this, data, text);
        }
        twitter.startStreaming(token);
    }

    public void onError(Exception e) {
        showError();
    }
}
