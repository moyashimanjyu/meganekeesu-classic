package jp.rsn.meganecase;

import java.util.ArrayList;
import java.util.List;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.IDs;
import twitter4j.TwitterAdapter;
import twitter4j.User;
import twitter4j.conf.Configuration;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class RequestActivity extends BaseActivity {

    private IconManager iconManager = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request);

        iconManager = new IconManager(this);

        ListView listView = (ListView) findViewById(R.id.listView);
        final ExtendAdapter adapter = new ExtendAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(adapter);

        Configuration conf = MeganeCaseTwitter.getConfiguration(App.getToken(this));
        final AsyncTwitter twitter = new AsyncTwitterFactory(conf).getInstance();
        twitter.addListener(new TwitterAdapter() {
            public void gotIncomingFriendships(IDs ids) {
                super.gotIncomingFriendships(ids);
                if (ids.getIDs().length == 0) {
                    Log.v(App.TAG, "request empty");
                    adapter.setEmpty();
                    return;
                }
                Log.v(App.TAG, "has request");
                for (int i = 0; i < ids.getIDs().length; i++) {
                    twitter.showUser(ids.getIDs()[i]);
                }
            }

            public void gotUserDetail(User user) {
                super.gotUserDetail(user);
                adapter.addUser(new UserEx(user));
            }
        });
        twitter.getIncomingFriendships(-1);
    }

    private class ExtendAdapter extends BaseAdapter implements OnItemClickListener {

        private final class ViewHolder {
            ImageView icon;
            TextView name;
            TextView description;
        }

        private List<UserEx> data = new ArrayList<UserEx>();
        private boolean empty = false;

        public int getCount() {
            if (empty) {
                return 1;
            }
            return data.size();
        }

        public Object getItem(int position) {
            if (empty) {
                return null;
            }
            return data.get(position);
        }

        public long getItemId(int position) {
            if (empty) {
                return 0;
            }
            return data.get(position).getUser().getId();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (empty) {
                TextView v = new TextView(parent.getContext());
                v.setText("No requests.");
                v.setPadding(10, 10, 10, 10);
                return v;
            }
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.request_item, null);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.description = (TextView) convertView.findViewById(R.id.description);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            UserEx user = data.get(position);
            holder.name.setText(user.getName());
            holder.description.setText(user.getDescription());
            holder.icon.setImageBitmap(user.getProfileImageBitmap());
            return convertView;
        }

        public void addUser(final UserEx user) {
            getHandler().post(new Runnable() {
                public void run() {
                    data.add(user);
                    notifyDataSetChanged();
                }
            });
            new Thread() {
                public void run() {
                    user.setProfileImageBitmap(iconManager.getIcon(user.getUser().getScreenName(),
                            user.getUser().getProfileImageURL().toString()));
                    getHandler().post(new Runnable() {
                        public void run() {
                            notifyDataSetInvalidated();
                        }
                    });
                }
            }.start();
        }

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (empty) {
                return;
            }
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://twitter.com/"
                    + data.get(position).getUser().getScreenName()));
            startActivity(intent);
        }

        public void setEmpty() {
            empty = true;
            getHandler().post(new Runnable() {
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
    };

    private final class UserEx {
        private final User user;
        private Bitmap image;

        public UserEx(User user) {
            this.user = user;
        }

        public final User getUser() {
            return user;
        }

        public final String getName() {
            return user.getName() + "(" + user.getScreenName() + ")";
        }

        public final String getDescription() {
            return user.getDescription();
        }

        public final Bitmap getProfileImageBitmap() {
            return image;
        }

        public final void setProfileImageBitmap(Bitmap image) {
            this.image = image;
        }
    }
}
