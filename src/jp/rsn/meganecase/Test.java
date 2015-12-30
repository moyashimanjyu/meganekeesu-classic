package jp.rsn.meganecase;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Relationship;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.conf.Configuration;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Test extends BaseActivity {

    private static final int DIALOG_CONNECTING = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new View(this));

        Uri uri = getIntent().getData();
        String path = uri.getPath();
        Intent intent = null;
        if (path.equals("/") || path.equals("/home") || path.equals("/share")
                || path.equals("/intent/tweet")) {
            trackEvent("post");
            intent = new Intent(getIntent().getAction(), uri, this, PostActivity.class);
            startActivity(intent);
            finish();
        }
        else if (path.equals("/search")) {
            trackEvent("search");
            intent = new Intent(getIntent().getAction(), uri, this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else if (path.equals("/status")) {
            trackEvent("status");
            intent = new Intent(getIntent().getAction(), uri);
            startActivity(intent);
            finish();
        }
        else {
            trackEvent("user");
            String username = path.substring(1);
            user(username);
        }
    }

    private String followItem;
    private String blockItem;
    private String r4sItem;
    private String followMode;
    private String blockMode;
    private User user;

    private final void user(final String username) {
        Configuration conf = MeganeCaseTwitter.getConfiguration(App.getToken(this));
        final AsyncTwitter twitter = new AsyncTwitterFactory(conf).getInstance();
        final SelectAdapter adapter = new SelectAdapter();

        twitter.addListener(new TwitterAdapter() {
            public void gotUserDetail(User user) {
                super.gotUserDetail(user);
                Test.this.user = user;
                try {
                    twitter.showFriendship(twitter.getId(), user.getId());
                }
                catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                catch (TwitterException e) {
                    e.printStackTrace();
                }
            }

            public void gotShowFriendship(Relationship relationship) {
                super.gotShowFriendship(relationship);
                if (relationship.isSourceFollowingTarget()) {
                    // リムる
                    followItem = getString(R.string.select_remove);
                    followMode = "remove";
                }
                else {
                    if (user.isProtected()) {
                        // 鍵っ子にリクエスト
                        followItem = getString(R.string.select_follow_request);
                    }
                    else {
                        // ヒョローする
                        followItem = getString(R.string.select_follow);
                    }
                    followMode = "follow";
                }
                if (relationship.isSourceBlockingTarget()) {
                    // ブロック解除する
                    blockItem = getString(R.string.select_unblock);
                    blockMode = "unblock";
                }
                else {
                    // ブロックする
                    blockItem = getString(R.string.select_block);
                    blockMode = "block";
                }
                r4sItem = getString(R.string.select_r4s);
                getHandler().post(new Runnable() {
                    public void run() {
                        adapter.notifyDataSetChanged();
                        dismissDialog(DIALOG_CONNECTING);
                    }
                });
            }

            public void createdFriendship(User user) {
                super.createdFriendship(user);
                dismissDialog(DIALOG_CONNECTING);
                finish();
            }

            public void destroyedFriendship(User user) {
                super.destroyedFriendship(user);
                dismissDialog(DIALOG_CONNECTING);
                finish();
            }

            public void createdBlock(User user) {
                super.createdBlock(user);
                dismissDialog(DIALOG_CONNECTING);
                finish();
            }

            public void destroyedBlock(User user) {
                super.destroyedBlock(user);
                dismissDialog(DIALOG_CONNECTING);
                finish();
            }

            public void reportedSpam(User reportedSpammer) {
                super.reportedSpam(reportedSpammer);
                dismissDialog(DIALOG_CONNECTING);
                finish();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(Test.this);
        ListView listView = new ListView(this);
        listView.setAdapter(adapter);
        OnItemClickListener listener = new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                case 0:
                    trackEvent(followMode);
                    if (followMode.equals("follow")) {
                        twitter.createFriendship(user.getId());
                        showDialog(DIALOG_CONNECTING);
                    }
                    else if (followMode.equals("remove")) {
                        twitter.destroyFriendship(user.getId());
                        showDialog(DIALOG_CONNECTING);
                    }
                    break;
                case 1:
                    trackEvent(blockMode);
                    if (blockMode.equals("block")) {
                        twitter.createBlock(user.getId());
                        showDialog(DIALOG_CONNECTING);
                    }
                    else if (blockMode.equals("unblock")) {
                        twitter.destroyBlock(user.getId());
                        showDialog(DIALOG_CONNECTING);
                    }
                    break;
                case 2:
                    trackEvent("r4s");
                    twitter.reportSpam(user.getId());
                    showDialog(DIALOG_CONNECTING);
                    break;
                }
            }
        };
        listView.setOnItemClickListener(listener);
        String title = "@" + username;
        builder.setTitle(title);
        builder.setView(listView);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                dismissDialog(DIALOG_CONNECTING);
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        twitter.showUser(username);
        showDialog(DIALOG_CONNECTING);
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (id == DIALOG_CONNECTING) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("通信中");
            return dialog;
        }
        else {
            return super.onCreateDialog(id);
        }
    }

    private final class SelectAdapter extends BaseAdapter {
        public int getCount() {
            return 3;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(getContext());
            textView.setTextSize(24);
            textView.setTextColor(Color.WHITE);
            textView.setPadding(5, 5, 5, 5);
            switch (position) {
            case 0:
                textView.setText(followItem);
                break;
            case 1:
                textView.setText(blockItem);
                break;
            case 2:
                textView.setText(r4sItem);
                break;
            }
            return textView;
        }
    }
}
