package jp.rsn.meganecase;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;

public class SpanBuilder {

    private static final Pattern PATTERN_SCREENNAME = Pattern.compile("@[a-zA-z0-9_]+");
    private static final Pattern PATTERN_HASHTAG = Pattern
            .compile("[#\\uFF03][a-zA-z0-9_\\p{InHiragana}\\p{InKatakana}\\p{InCJKUnifiedIdeographs}]+");
    private static final StyleSpan STYLE_SPAN_BOLD = new StyleSpan(Typeface.BOLD);

    public static Spannable buildNormal(Info info) {
        int indent = 0;
        if (info.isDirectMessage()) {
            indent = 2;
        }
        String text = info.getText();
        if (info.isFav()) {
            int pos = text.indexOf("\n") + 1;
            text = text.substring(0, pos) + text.substring(pos).replace("\n", " ");
        }
        else {
            text = text.replace("\n", " ");
        }

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(text);
        spannable.setSpan(STYLE_SPAN_BOLD, indent, info.getUsername().length() + indent,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public static Spannable buildSelect(Info info) {
        String text = info.getText();
        int indent = 0;
        if (info.isDirectMessage()) {
            indent = 2;
        }
        else if (info.getId() != -1) {
            StringBuilder name = new StringBuilder(50);
            name.append(info.getUsername());
            if (!Util.isEmpty(info.getUserTextName())) {
                name.append(" (").append(info.getUserTextName()).append(")");
            }
            name.append("\n");
            int len = info.getUsername().length() + 1;
            text = name.append(text.substring(len)).toString();
        }

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(text);
        URLSpan usernameSpan = new URLSpan("https://twitter.com/" + info.getUsername());
        spannable.setSpan(usernameSpan, indent, info.getUsername().length() + indent,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(STYLE_SPAN_BOLD, indent, info.getUsername().length() + indent,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        Matcher matcher;
        matcher = PATTERN_SCREENNAME.matcher(text);
        while (matcher.find()) {
            URLSpan nameSpan = new URLSpan("https://twitter.com/" + matcher.group().substring(1));
            spannable.setSpan(nameSpan, matcher.start() + 1, matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        matcher = PATTERN_HASHTAG.matcher(text);
        while (matcher.find()) {
            String q = matcher.group().substring(1);
            try {
                q = URLEncoder.encode(q, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
            }
            URLSpan hashSpan = new URLSpan("https://twitter.com/search?q=%23" + q);
            spannable.setSpan(hashSpan, matcher.start(), matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (info.getUrlStrings() != null) {
            for (int i = 0; i < info.getUrlStrings().length; i++) {
                String urlString = info.getUrlStrings()[i];
                if (urlString == null) {
                    continue;
                }
                int start = text.indexOf(urlString);
                int end = start + urlString.length();
                URLSpan span = new URLSpan(urlString);
                spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannable;
    }

    public static Spannable buildInReplyText(Info info) {
        Spannable spannable = Spannable.Factory.getInstance().newSpannable(info.getText());
        spannable.setSpan(STYLE_SPAN_BOLD, 0, info.getUsername().length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }
}
