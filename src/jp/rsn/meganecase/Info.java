package jp.rsn.meganecase;

import java.util.Date;

import twitter4j.Status;
import android.graphics.Bitmap;
import android.text.Spannable;

public class Info {

    private Bitmap icon = null;
    private String iconUrl = null;
    private String text = null;
    private Boolean isMentions = false;
    private Boolean isMine = false;
    private String username = null;
    private Boolean isSelected = false;
    private Long id = null;
    private Boolean isRetweet = false;
    private Bitmap rtIcon = null;
    private String rtIconUrl = null;
    private String rtUsername = null;
    private String[] urlStrings = null;
    private Date created = null;
    private Long inReplyTo = null;
    private Boolean isDirectMessage = false;
    private Boolean isProtected = false;
    private Date rtTime = null;
    private String via = null;
    private Boolean isFav = false;
    private String userTextName = null;
    private String lang = null;

    private Status status = null;
    private Spannable normalSpan = null;
//    private Spannable select = null;

    public Info() {
    }

    public final Bitmap getIcon() {
        return icon;
    }

    public final void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public final String getIconUrl() {
        return iconUrl;
    }

    public final void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public final String getText() {
        return text;
    }

    public final void setText(String text) {
        this.text = text;
    }

    public final Boolean isMentions() {
        return isMentions;
    }

    public final void setMentions(Boolean isMentions) {
        this.isMentions = isMentions;
    }

    public final Boolean isMine() {
        return isMine;
    }

    public final void setMine(Boolean isMine) {
        this.isMine = isMine;
    }

    public final String getUsername() {
        return username;
    }

    public final void setUsername(String username) {
        this.username = username;
    }

    public final Boolean isSelected() {
        return isSelected;
    }

    public final void setSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    public final Long getId() {
        return id;
    }

    public final void setId(Long id) {
        this.id = id;
    }

    public final Boolean isRetweet() {
        return isRetweet;
    }

    public final void setRetweet(Boolean isRetweet) {
        this.isRetweet = isRetweet;
    }

    public final Bitmap getRtIcon() {
        return rtIcon;
    }

    public final void setRtIcon(Bitmap rtIcon) {
        this.rtIcon = rtIcon;
    }

    public final String getRtIconUrl() {
        return rtIconUrl;
    }

    public final void setRtIconUrl(String rtIconUrl) {
        this.rtIconUrl = rtIconUrl;
    }

    public final String getRtUsername() {
        return rtUsername;
    }

    public final void setRtUsername(String rtUsername) {
        this.rtUsername = rtUsername;
    }

    public final String[] getUrlStrings() {
        return urlStrings;
    }

    public final void setUrlStrings(String[] urlStrings) {
        this.urlStrings = urlStrings;
    }

    public final Date getCreated() {
        return created;
    }

    public final void setCreated(Date created) {
        this.created = created;
    }

    public final Long getInReplyTo() {
        return inReplyTo;
    }

    public final void setInReplyTo(Long inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    public final Boolean isDirectMessage() {
        return isDirectMessage;
    }

    public final void setDirectMessage(Boolean isDirectMessage) {
        this.isDirectMessage = isDirectMessage;
    }

    public final Boolean isProtected() {
        return isProtected;
    }

    public final void setProtected(Boolean isProtected) {
        this.isProtected = isProtected;
    }

    public final Date getRtTime() {
        return rtTime;
    }

    public final void setRtTime(Date rtTime) {
        this.rtTime = rtTime;
    }

    public final String getVia() {
        return via;
    }

    public final void setVia(String via) {
        this.via = via;
    }

    public final Boolean isFav() {
        return isFav;
    }

    public final void setFav(Boolean isFav) {
        this.isFav = isFav;
    }

    public final String getUserTextName() {
        return userTextName;
    }

    public final void setUserTextName(String userTextName) {
        this.userTextName = userTextName;
    }

    public final String getLang() {
        return lang;
    }

    public final void setLang(String lang) {
        this.lang = lang;
    }

    public final Status getStatus() {
        return status;
    }

    public final void setStatus(Status status) {
        this.status = status;
    }

    public final Spannable getNormalSpan() {
        return normalSpan;
    }

    public final void setNormalSpan(Spannable normalSpan) {
        this.normalSpan = normalSpan;
    }

//    public final Spannable getSelect() {
//        return select;
//    }

//    public final void setSelect(Spannable select) {
//        this.select = select;
//    }
}
