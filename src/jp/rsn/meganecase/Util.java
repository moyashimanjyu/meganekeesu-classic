package jp.rsn.meganecase;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;

public class Util {

    public static final boolean isEmpty(String str) {
        return str == null ? true : str.length() == 0;
    }

    private static final Pattern URL_MATCH_PATTERN = Pattern.compile(
            "(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.CASE_INSENSITIVE);

    public static final String[] searchUrls(String text) {
        Matcher matcher = URL_MATCH_PATTERN.matcher(text);
        ArrayList<String> results = new ArrayList<String>();
        while (matcher.find()) {
            results.add(matcher.group());
        }
        if (results.size() == 0) {
            return null;
        }
        return results.toArray(new String[results.size()]);
    }

    public static void saveInfoList(FileOutputStream out, List<Info> list) throws IOException {
        out.write(infoList2JsonString(list).getBytes());
    }

    public static String infoList2JsonString(List<Info> list) {
        JSONArray array = new JSONArray();
        Iterator<Info> it = list.iterator();
        while (it.hasNext()) {
            try {
                Info info = it.next();
                if (info.getIconUrl() == null || info.getIconUrl().length() == 0 || info.isDirectMessage()) {
                    continue;
                }
                JSONObject json = new JSONObject();
                json.put("id", info.getId());
                json.put("username", info.getUsername());
                json.put("usertextname", info.getUserTextName());
                json.put("text", info.getText());
                json.put("iconUrl", info.getIconUrl());
                json.put("inReplyTo", info.getInReplyTo());
                json.put("created", info.getCreated().getTime());
                json.put("via", info.getVia());
                json.put("lang", info.getLang());
                json.put("isMentions", info.isMentions());
                json.put("isMine", info.isMine());
                json.put("isProtected", info.isProtected());
                json.put("isRetweet", info.isRetweet());
                if (info.isRetweet()) {
                    json.put("rtUsername", info.getRtUsername());
                    json.put("rtIconUrl", info.getRtIconUrl());
                    json.put("rtTime", info.getRtTime().getTime());
                }
                array.put(json);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array.toString();
    }

    public static List<Info> loadInfoList(FileInputStream in) throws IOException {
        Writer writer = new StringWriter();
        Reader reader = new BufferedReader(new InputStreamReader(in));
        char[] buffer = new char[1024];
        int len;
        while ((len = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, len);
        }
        return jsonString2InfoList(writer.toString());
    }

    public static List<Info> jsonString2InfoList(String jsonString) {
        List<Info> list = Collections.synchronizedList(new LinkedList<Info>());
        try {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = new JSONObject(array.get(i).toString());
                Info info = new Info();
                info.setId(json.getLong("id"));
                info.setUsername(json.getString("username"));
                info.setUserTextName(json.getString("usertextname"));
                info.setText(json.getString("text"));
                info.setIconUrl(json.getString("iconUrl"));
                info.setInReplyTo(json.getLong("inReplyTo"));
                info.setCreated(new Date(json.getLong("created")));
                info.setVia(json.getString("via"));
                info.setLang(json.getString("lang"));
                info.setMentions(json.getBoolean("isMentions"));
                info.setProtected(json.getBoolean("isProtected"));
                info.setMine(json.getBoolean("isMine"));
                info.setRetweet(json.getBoolean("isRetweet"));
                if (info.isRetweet()) {
                    info.setRtUsername(json.getString("rtUsername"));
                    info.setRtIconUrl(json.getString("rtIconUrl"));
                    info.setRtTime(new Date(json.getLong("rtTime")));
                }
                info.setUrlStrings(searchUrls(info.getText()));
                info.setNormalSpan(SpanBuilder.buildNormal(info));
                list.add(info);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
