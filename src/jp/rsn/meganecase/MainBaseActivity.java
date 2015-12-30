package jp.rsn.meganecase;

import jp.rsn.meganecase.TimelineData.DataChangeObserver;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainBaseActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    protected static final int REQUEST_POST_ACTIVITY = 1;
    protected static final int REQUEST_OAUTH_ACTIVITY = 2;

    protected final DataObserver observer = new DataObserver();

    protected ListView listView = null;
    protected TimelineAdapter adapter = null;
    protected TimelineData data = null;
    protected MeganeCaseTwitter twitter = null;
    protected DialogManager dialogManager = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        data = new TimelineData(this);
        if (savedInstanceState != null) {
            data.setList(Util.jsonString2InfoList(savedInstanceState.getString("SAVED_TIMELINE")));
            data.reloadIcons();
        }
        twitter = null;

        adapter = new TimelineAdapter(this, data);

        data.addObserver(observer);
        dialogManager = new DialogManager(this);

        setContentView(R.layout.main);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
        if (twitter != null) {
            twitter.closeStreaming();
        }
        if (listView != null) {
            listView.setAdapter(null);
            listView.setOnItemClickListener(null);
        }
        if (data != null) {
            data.recycleBitmaps();
            data.deleteObserver(observer);
        }
        new IconManager(this).clean();
        BackgroundWorker.getInstance().shutdown();

        super.onDestroy();
        twitter = null;
        listView = null;
        data = null;
        adapter = null;
        dialogManager = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        App.Token token = App.getToken(this);
        if (token == null) {
            Intent intent = new Intent(this, OAuthActivity.class);
            startActivityForResult(intent, REQUEST_OAUTH_ACTIVITY);
        }
        else {
            new StartTask().execute();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("SAVED_TIMELINE", Util.infoList2JsonString(data.getList()));
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
        case KeyEvent.KEYCODE_P:
            trackEvent("key-p");
            post();
            return true;
        case KeyEvent.KEYCODE_X:
            if (data.getSelectedInfo() != null) {
                trackEvent("key-x");
                clearSelectedInfo();
                return true;
            }
            break;
        case KeyEvent.KEYCODE_V:
            trackEvent("key-v");
            if (twitter != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                boolean vibe = prefs.getBoolean("mode_viberation", false);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("mode_viberation", !vibe);
                editor.commit();
                if (prefs.getBoolean("mode_viberation", false)) {
                    twitter.setVibe(true);
                    Toast.makeText(this, R.string.vibe_on, Toast.LENGTH_SHORT).show();
                }
                else {
                    twitter.setVibe(false);
                    Toast.makeText(this, R.string.vibe_off, Toast.LENGTH_SHORT).show();
                }
            }
            break;
        case KeyEvent.KEYCODE_T:
            trackEvent("key-t");
            listView.setSelection(0);
            break;
        default:
            break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        switch (requestCode) {
        case REQUEST_POST_ACTIVITY:
            if (resultCode == RESULT_OK) {
                clearSelectedInfo();
            }
            break;
        case REQUEST_OAUTH_ACTIVITY:
            if (App.getToken(this) == null) {
                finish();
                break;
            }
            if (resultCode == RESULT_OK) {
                new AuthedTask().execute();
            }
            break;
        default:
            break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return dialogManager.createDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        if (id == DialogManager.DIALOG_HISTORY) {
            dialogManager.prepareDialogForHistory(twitter, data.getSelectedInfo(), id, dialog);
        }
        else {
            dialogManager.prepareDialog(data.getSelectedInfo(), id, dialog);
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Info info = (Info) adapter.getItem(position);
        if (info.getId() == -1) {
            data.setSelectedInfo(null);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"
                    + info.getUsername())));
        }
        else {
            data.setSelectedInfo(info);
            adapter.notifyDataSetChanged();
        }
    }

    public final void post() {
        Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra("POST_STRING", "");
        startActivityForResult(intent, MainActivity.REQUEST_POST_ACTIVITY);
    }

    public final Info getSelectedInfo() {
        return data.getSelectedInfo();
    }

    public final void clearSelectedInfo() {
        data.setSelectedInfo(null);
        adapter.notifyDataSetChanged();
    }

    private final class DataObserver implements DataChangeObserver {
        public final void onAdd() {
            synchronized (listView) {
                int pos = listView.getFirstVisiblePosition();
                int selpos = listView.getSelectedItemPosition();
                adapter.notifyDataSetChanged();
                if (pos != 0) {
                    if (selpos == ListView.INVALID_POSITION) {
                        listView.setSelection(pos + 1);
                    }
                }
                else if (data.getSelectedInfo() != null) {
                    int first = listView.getFirstVisiblePosition();
                    int last = listView.getLastVisiblePosition();
                    for (int i = first; i <= last; i++) {
                        Info info = (Info) listView.getAdapter().getItem(i);
                        if (data.getSelectedInfo() == info) {
                            if (selpos == ListView.INVALID_POSITION) {
                                listView.setSelection(pos + 1);
                            }
                        }
                    }
                }
            }
        }

        public final void onIconDownload() {
            // なぜかnotifyDataSetInvalidatedだとトラックボールの動きがおかしくなるので
            adapter.notifyDataSetChanged();
        }
    }

    private abstract class WaitTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog = null;

        abstract protected Void doInBackground(Void... params);

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MainBaseActivity.this);
            dialog.setOwnerActivity(MainBaseActivity.this);
            dialog.setMessage(getString(R.string.wait));
            dialog.setCancelable(true);
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            });
        }

        @Override
        protected void onPostExecute(Void result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            dialog = null;
        }
    }

    private final class StartTask extends WaitTask {
        protected Void doInBackground(Void... params) {
            if (twitter == null) {
                if (getIntent().getData() == null) {
                    twitter = new MeganeCaseTwitter(MainBaseActivity.this, data);
                }
                else {
                    String q = getIntent().getData().getQueryParameter("q");
                    twitter = new MeganeCaseTwitterSecret(MainBaseActivity.this, data, q);
                }
            }
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(MainBaseActivity.this);
            twitter.setVibe(prefs.getBoolean("mode_viberation", false));
            twitter.setBeast(prefs.getBoolean("mode_beast", false));
            twitter.setVoice(prefs.getBoolean("mode_voice", false));
            App.Token token = App.getToken(MainBaseActivity.this);
            twitter.startStreaming(token);
            return null;
        }
    }

    private final class AuthedTask extends WaitTask {
        protected Void doInBackground(Void... params) {
            if (twitter != null) {
                twitter.closeStreaming();
            }
            App.Token token = App.getToken(MainBaseActivity.this);
            twitter = new MeganeCaseTwitter(MainBaseActivity.this, data);
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(MainBaseActivity.this);
            twitter.setVibe(prefs.getBoolean("mode_viberation", false));
            twitter.setBeast(prefs.getBoolean("mode_beast", false));
            twitter.setVoice(prefs.getBoolean("mode_voice", false));
            twitter.startStreaming(token);
            return null;
        }
    }
}
