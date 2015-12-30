package jp.rsn.meganecase;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class TimelineData {

    private static final int MAX_BUFFER = 500;

    private final TimelineHandler handler;
    private final IconManager iconManager;

    private List<Info> list = Collections.synchronizedList(new LinkedList<Info>());
    private Info selectedInfo = null;

    public TimelineData(Context context) {
        handler = new TimelineHandler(context.getMainLooper());
        iconManager = new IconManager(context);
    }

    public final List<Info> getList() {
        return list;
    }

    public final void setList(List<Info> list) {
        this.list = list;
    }

    public final Info getSelectedInfo() {
        return selectedInfo;
    }

    public final void setSelectedInfo(Info selectedInfo) {
        if (this.selectedInfo != null) {
            this.selectedInfo.setSelected(false);
        }
        this.selectedInfo = selectedInfo;
        if (selectedInfo != null) {
            selectedInfo.setSelected(true);
        }
    }

    public final void addInfo(Info info) {
        if (info == null) {
            return;
        }
        info.setNormalSpan(SpanBuilder.buildNormal(info));
        Message msg = handler.obtainMessage(TimelineHandler.HANDLE_ADD_DATA, info);
        handler.sendMessage(msg);
        if (info.getIconUrl() != null && info.getIconUrl().length() != 0) {
            BackgroundWorker.getInstance().invoke(new IconDownloader(info));
        }
    }

    private final void addListItem(Info info) {
        if (info.getCreated() == null && list.size() > 0) {
            info.setCreated(list.get(0).getCreated());
        }
        boolean added = false;
        int eqPos = -1;
        for (int i = 0; i < list.size(); i++) {
            if (info.getCreated().equals(list.get(i).getCreated())) {
                if (eqPos == -1) {
                    eqPos = i;
                }
                if (info.getId().equals(list.get(i).getId())) {
                    added = true;
                    eqPos = -1;
                    break;
                }
            }
            else {
                if (eqPos != -1) {
                    list.add(eqPos, info);
                    added = true;
                    eqPos = -1;
                    break;
                }
            }
            if (info.getCreated().after(list.get(i).getCreated())) {
                list.add(i, info);
                added = true;
                break;
            }
        }
        if (!added) {
            list.add(info);
        }
        while (list.size() > MAX_BUFFER) {
            int pos = list.size() - 1;
            final Info i = list.get(pos);
            list.remove(pos);
            recycleBitmap(i);
        }
    }

    public final void cancel() {
        handler.removeMessages(TimelineHandler.HANDLE_ADD_DATA);
        handler.removeMessages(TimelineHandler.HANDLE_ICON_DOWNLOAD);
    }

    public final void reloadIcons() {
        Iterator<Info> it = list.iterator();
        while (it.hasNext()) {
            Info info = it.next();
            if (info.getIcon() == null && info.getIconUrl() != null
                    && info.getIconUrl().length() != 0) {
                BackgroundWorker.getInstance().invoke(new IconDownloader(info));
            }
        }
    }

    public final void recycleBitmap(Info info) {
        Bitmap b;
        b = info.getIcon();
        if (b != null && !b.isRecycled()) {
            b.recycle();
        }
        b = info.getRtIcon();
        if (b != null && !b.isRecycled()) {
            b.recycle();
        }
    }

    public final void recycleBitmaps() {
        Iterator<Info> it = list.iterator();
        while (it.hasNext()) {
            Info info = it.next();
            recycleBitmap(info);
        }
    }

    public final class IconDownloader implements Runnable {

        private final Info info;

        public IconDownloader(Info info) {
            this.info = info;
        }

        public void run() {
            info.setIcon(iconManager.getIcon(info.getUsername(), info.getIconUrl()));
            if (!handler.hasMessages(TimelineHandler.HANDLE_ICON_DOWNLOAD)) {
                handler.sendMessage(handler.obtainMessage(TimelineHandler.HANDLE_ICON_DOWNLOAD));
            }
            if (info.isRetweet()) {
                info.setRtIcon(iconManager.getIcon(info.getRtUsername(), info.getRtIconUrl()));
                if (!handler.hasMessages(TimelineHandler.HANDLE_ICON_DOWNLOAD)) {
                    handler.sendMessage(handler.obtainMessage(TimelineHandler.HANDLE_ICON_DOWNLOAD));
                }
            }
        }
    }

    private final Set<DataChangeObserver> observers = new CopyOnWriteArraySet<DataChangeObserver>();

    public final void addObserver(DataChangeObserver observer) {
        observers.add(observer);
    }

    public final void deleteObserver(DataChangeObserver observer) {
        observers.remove(observer);
    }

    private final void notifyAdd() {
        for (DataChangeObserver observer : observers) {
            observer.onAdd();
        }
    }

    private final void notifyIconDownload() {
        for (DataChangeObserver observer : observers) {
            observer.onIconDownload();
        }
    }

    public static interface DataChangeObserver {
        public void onAdd();

        public void onIconDownload();
    }

    @SuppressLint("HandlerLeak")
    private final class TimelineHandler extends Handler {

        public static final int HANDLE_ADD_DATA = 101;
        public static final int HANDLE_ICON_DOWNLOAD = 102;

        public TimelineHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HANDLE_ADD_DATA) {
                Info info = (Info) msg.obj;
                synchronized (list) {
                    addListItem(info);
                    notifyAdd();
                }
            }
            else if (msg.what == HANDLE_ICON_DOWNLOAD) {
                notifyIconDownload();
            }
            else {
                super.handleMessage(msg);
            }
        }
    }
}
