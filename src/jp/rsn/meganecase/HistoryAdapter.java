package jp.rsn.meganecase;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HistoryAdapter extends BaseAdapter {

    private final Handler handler = new Handler();
    private final LayoutInflater inflater;
    private final IconManager iconManager;
    private final ArrayList<Info> list = new ArrayList<Info>();

    public HistoryAdapter(Context context, MeganeCaseTwitter twitter, Info first) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        iconManager = new IconManager(context);
        list.add(first);
        if (twitter != null) {
            twitter.openHistory(this, first);
        }
    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.item, null);
        }
        TextView textView = (TextView) view.findViewById(R.id.itemText);
        ImageView iconView = (ImageView) view.findViewById(R.id.itemIcon);
        Info info = list.get(position);
        textView.setText(info.getText());
        iconView.setImageBitmap(info.getIcon());
        return view;
    }

    public void add(Info info) {
        if (info == null) {
            return;
        }
        handler.post(new Adder(info));
        new Thread(new IconDownloader(info)).start();
    }

    public class Adder implements Runnable {
        private final Info info;

        public Adder(Info info) {
            this.info = info;
        }

        public void run() {
            list.add(info);
            notifyDataSetChanged();
        }
    }

    public class IconDownloader implements Runnable {
        private final Info info;

        public IconDownloader(Info info) {
            this.info = info;
        }

        public void run() {
            info.setIcon(iconManager.getIcon(info.getUsername(), info.getIconUrl()));
            handler.post(new Runnable() {
                public void run() {
                    notifyDataSetInvalidated();
                }
            });
            if (info.isRetweet()) {
                info.setRtIcon(iconManager.getIcon(info.getRtUsername(), info.getRtIconUrl()));
                handler.post(new Runnable() {
                    public void run() {
                        notifyDataSetInvalidated();
                    }
                });
            }
        }
    }
}
