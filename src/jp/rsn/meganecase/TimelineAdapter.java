package jp.rsn.meganecase;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimelineAdapter extends BaseAdapter implements View.OnClickListener,
        View.OnLongClickListener {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",
            Locale.JAPAN);
    private static final LinearLayout.LayoutParams PARAMS = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    private static final class ViewHolder {
        ImageView icon;
        TextView text;
        LinearLayout layout;
        TextView infoView;
        RtViewHolder rtHolder;
        View action;
    }

    private static final class RtViewHolder {
        View view;
        ImageView icon;
        TextView text;
        TextView time;
    }

    private final Context context;
    private final TimelineData data;
    private final LayoutInflater inflater;
    private final float density;

    private MainActionListener listener = null;
    private boolean openExtendActions = false;
    private boolean singleLine = false;
    private boolean rakuraku = false;
    private boolean infoViewMode = false;
    private boolean largeIcon = false;
    private float textSize = 1.0f;
    private boolean mute = false;

    private int mineColor;
    private int mentionColor;
    private int favColor;
    private int retweetColor;
    private int mainColor;
    private int subColor;
    private int selectColor;

    public TimelineAdapter(Context context, TimelineData data) {
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        density = context.getResources().getDisplayMetrics().density;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        singleLine = prefs.getBoolean("mode_singleline", false);
        rakuraku = prefs.getBoolean("mode_rakuraku", false);
        infoViewMode = prefs.getBoolean("mode_infoview", false);
        largeIcon = prefs.getBoolean("mode_largeicon", false);
        int size = Integer.parseInt(prefs.getString("mode_text_size", "1"));
        textSize = (size == 0 ? 0.8f : size == 1 ? 1.0f : 1.2f);
        mute = prefs.getBoolean("mode_mute", false);
        setLightColor(prefs.getBoolean("mode_lightcolor", false));
        selectColor = context.getResources().getColor(android.R.color.primary_text_dark);
    }

    public final void setListener(MainActionListener listener) {
        this.listener = listener;
    }

    public final int getCount() {
        return data.getList().size();
    }

    public final Info getItem(int position) {
        return data.getList().get(position);
    }

    public final long getItemId(int position) {
        return position;
    }

    public View getView(int position, View view, ViewGroup parent) {
        Info info = getItem(position);

        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.item, null);
            holder = new ViewHolder();
            holder.icon = (ImageView) view.findViewById(R.id.itemIcon);
            holder.text = (TextView) view.findViewById(R.id.itemText);
            holder.layout = (LinearLayout) view.findViewById(R.id.layoutTweet);
            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        holder.icon.setImageBitmap(info.getIcon());
        int iconSize = (int) ((largeIcon ? 40 : 20) * density);
        holder.icon.getLayoutParams().width = iconSize;
        holder.icon.getLayoutParams().height = iconSize;

        TextView textView = holder.text;
        if (info.isSelected()) {
            textView.setText(SpanBuilder.buildSelect(info));
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setSingleLine(false);
        }
        else {
            textView.setText(mute ? null : info.getNormalSpan());
            textView.setMovementMethod(null);
            textView.setSingleLine(singleLine);
        }

        if (info.isMine()) {
            textView.setTextColor(mineColor);
        }
        else if (info.isMentions()) {
            textView.setTextColor(mentionColor);
        }
        else {
            textView.setTextColor(mainColor);
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, (rakuraku ? 30 : 14) * textSize);

        LinearLayout layout = holder.layout;
        layout.removeAllViewsInLayout();
        layout.addView(textView, PARAMS);

        if (info.isFav()) {
            textView.setTextColor(favColor);
            return view;
        }

        if (info.getId() == -1) {
            textView.setTextColor(subColor);
            return view;
        }

        if (infoViewMode || info.isSelected()) {
            TextView infoView;
            if (holder.infoView == null) {
                infoView = new TextView(context);
                infoView.setSingleLine();
                holder.infoView = infoView;
            }
            else {
                infoView = holder.infoView;
            }
            StringBuffer infoText = new StringBuffer(40);
            if (info.isRetweet()) {
                infoText.append(SDF.format(info.getRtTime()));
            }
            else {
                infoText.append(SDF.format(info.getCreated()));
            }
            if (!Util.isEmpty(info.getVia())) {
                infoText.append(" via ").append(info.getVia());
            }
            infoView.setText(infoText.toString());
            infoView.setTextColor(subColor);
            infoView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f * textSize);
            layout.addView(infoView, PARAMS);
        }
        else if (holder.infoView != null) {
            holder.infoView = null;
        }

        if (info.isDirectMessage()) {
            textView.setTextColor(Color.RED);
            return view;
        }

        if (info.isRetweet()) {
            textView.setTextColor(retweetColor);
            RtViewHolder rtHolder;
            View rt;
            TextView rtText;
            ImageView rtIcon;
            if (holder.rtHolder == null) {
                rtHolder = new RtViewHolder();
                holder.rtHolder = rtHolder;
                rt = inflater.inflate(R.layout.rt, layout, false);
                rtHolder.view = rt;
                rtText = (TextView) rt.findViewById(R.id.itemRtText);
                rtHolder.text = rtText;
                rtIcon = (ImageView) rt.findViewById(R.id.itemRtIcon);
                rtHolder.icon = rtIcon;
            }
            else {
                rtHolder = holder.rtHolder;
                rt = rtHolder.view;
                rtText = rtHolder.text;
                rtIcon = rtHolder.icon;
            }
            if (infoViewMode || info.isSelected()) {
                TextView rtTime;
                if (rtHolder.time == null) {
                    rtTime = (TextView) rt.findViewById(R.id.itemRtTime);
                    rtHolder.time = rtTime;
                }
                else {
                    rtTime = rtHolder.time;
                }
                rtTime.setText(SDF.format(info.getCreated()));
                rtTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f * textSize);
            }
            else if (rtHolder.time != null) {
                rtHolder.time.setText(null);
                rtHolder.time = null;
            }
            if (info.isSelected()) {
                Spannable spannable = Spannable.Factory.getInstance().newSpannable(
                        info.getRtUsername());
                URLSpan nameSpan = new URLSpan("https://twitter.com/" + info.getRtUsername());
                spannable.setSpan(nameSpan, 0, info.getRtUsername().length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                rtText.setText(spannable);
                rtText.setMovementMethod(LinkMovementMethod.getInstance());
            }
            else {
                rtText.setText(mute ? null : info.getRtUsername());
                rtText.setMovementMethod(null);
            }
            rtText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f * textSize);
            rtIcon.setImageBitmap(info.getRtIcon());
            layout.addView(rt);
        }
        else if (holder.rtHolder != null) {
            holder.rtHolder = null;
        }

        if (info.isSelected()) {
            View action;
            if (holder.action == null) {
                action = inflater.inflate(R.layout.action, layout, false);
                holder.action = action;
            }
            else {
                action = holder.action;
            }
            setActionViews(info, action);
            layout.addView(action, PARAMS);
            textView.setTextColor(selectColor);
            if (openExtendActions) {
                action.findViewById(R.id.extendActions).setVisibility(View.VISIBLE);
            }
            else {
                action.findViewById(R.id.extendActions).setVisibility(View.GONE);
            }
        }
        else if (holder.action != null) {
            holder.action = null;
        }
        return view;
    }

    private final void setActionViews(final Info info, final View view) {
        Button replyButton = (Button) view.findViewById(R.id.replyButton);
        replyButton.setOnClickListener(this);
        replyButton.setOnLongClickListener(this);
        Button favoriteButton = (Button) view.findViewById(R.id.favoriteButton);
        favoriteButton.setOnClickListener(this);
        favoriteButton.setOnLongClickListener(this);
        Button qtButton = (Button) view.findViewById(R.id.qtButton);
        qtButton.setOnClickListener(this);
        qtButton.setOnLongClickListener(this);
        Button retweetButton = (Button) view.findViewById(R.id.retweetButton);
        if (info.isProtected() || info.isMine()) {
            retweetButton.setEnabled(false);
        }
        else {
            retweetButton.setEnabled(true);
            retweetButton.setOnClickListener(this);
        }
        Button historyButton = (Button) view.findViewById(R.id.historyButton);
        if (info.isSelected() != null && info.getInReplyTo() != null && info.getInReplyTo() != -1) {
            historyButton.setEnabled(true);
            historyButton.setOnClickListener(this);
        }
        else {
            historyButton.setEnabled(false);
        }
        Button leadButton = (Button) view.findViewById(R.id.leadButton);
        leadButton.setOnClickListener(this);
        Button aaaButton = (Button) view.findViewById(R.id.aaaButton);
        aaaButton.setOnClickListener(this);
        Button pluginButton = (Button) view.findViewById(R.id.pluginButton);
        pluginButton.setOnClickListener(this);
        Button translateButton = (Button) view.findViewById(R.id.translateButton);
        translateButton.setOnClickListener(this);
        Button deleteButton = (Button) view.findViewById(R.id.deleteButton);
        if (info.isMine()) {
            deleteButton.setEnabled(true);
            deleteButton.setOnClickListener(this);
        }
        else {
            deleteButton.setEnabled(false);
        }
        view.findViewById(R.id.closeButton).setOnClickListener(this);
        view.findViewById(R.id.extendButton).setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.closeButton) {
            data.setSelectedInfo(null);
            notifyDataSetChanged();
            return;
        }
        else if (v.getId() == R.id.extendButton) {
            openExtendActions = !openExtendActions;
            notifyDataSetChanged();
            return;
        }
        if (listener == null) {
            return;
        }
        switch (v.getId()) {
        case R.id.replyButton:
            listener.onReply(data.getSelectedInfo());
            break;
        case R.id.favoriteButton:
            listener.onFavorite(data.getSelectedInfo());
            break;
        case R.id.retweetButton:
            listener.onRetweet(data.getSelectedInfo());
            break;
        case R.id.qtButton:
            listener.onQt(data.getSelectedInfo());
            break;
        case R.id.historyButton:
            listener.onHistory(data.getSelectedInfo());
            break;
        case R.id.leadButton:
            listener.onLead(data.getSelectedInfo());
            break;
        case R.id.aaaButton:
            listener.onAaa(data.getSelectedInfo());
            break;
        case R.id.pluginButton:
            listener.onPlugin(data.getSelectedInfo());
            break;
        case R.id.translateButton:
            listener.onTranslate(data.getSelectedInfo());
            break;
        case R.id.deleteButton:
            listener.onDelete(data.getSelectedInfo());
            break;
        default:
            break;
        }
    }

    public boolean onLongClick(View v) {
        if (listener == null) {
            return true;
        }
        switch (v.getId()) {
        case R.id.replyButton:
            listener.onBlank(data.getSelectedInfo());
            break;
        case R.id.favoriteButton:
            listener.onFavrt(data.getSelectedInfo());
            break;
        case R.id.qtButton:
            listener.onRt(data.getSelectedInfo());
            break;
        default:
            break;
        }
        return true;
    }

    public final void setLightColor(boolean lightColor) {
        Resources res = context.getResources();
        mineColor = res.getColor(lightColor ? R.color.mine_light : R.color.mine);
        mentionColor = res.getColor(lightColor ? R.color.mention_light : R.color.mention);
        mainColor = res.getColor(lightColor ? android.R.color.primary_text_dark
                : android.R.color.secondary_text_dark);
        favColor = res.getColor(lightColor ? R.color.fav_light : R.color.fav);
        subColor = res.getColor(lightColor ? android.R.color.secondary_text_dark
                : android.R.color.tertiary_text_dark);
        retweetColor = res.getColor(lightColor ? R.color.retweet_light : R.color.retweet);
    }

    public final void setSingleLine(boolean singleLine) {
        this.singleLine = singleLine;
    }

    public final void setRakuraku(boolean rakuraku) {
        this.rakuraku = rakuraku;
    }

    public final void setInfoView(boolean infoView) {
        this.infoViewMode = infoView;
    }

    public final void setLargeIcon(boolean largeIcon) {
        this.largeIcon = largeIcon;
    }

    public final void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public final void setMute(boolean mute) {
        this.mute = mute;
    }
}