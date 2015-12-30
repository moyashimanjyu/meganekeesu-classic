package jp.rsn.meganecase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class IconManager {

    private static final BitmapFactory.Options OPTS = new BitmapFactory.Options();
    static {
        OPTS.inPurgeable = true;
    }

    private final File cacheDir;

    private FileLRUCache fileCache = new FileLRUCache(500);

    public IconManager(Context context) {
        cacheDir = new File(context.getCacheDir().getPath(), "icon");
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
        for (String filename : cacheDir.list()) {
            fileCache.put(filename, new File(cacheDir, filename));
        }
    }

    public Bitmap getIcon(final String screenName, String iconUrl) {
        Bitmap bitmap = null;
        if (fileCache.containsKey(screenName)) {
            final File iconFile = fileCache.get(screenName);
            final byte[] buffer = requestIcon(iconUrl, iconFile.lastModified());
            if (buffer != null) {
                bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length, OPTS);
                BackgroundWorker.getInstance().invoke(new Runnable() {
                    @Override
                    public void run() {
                        saveCache(screenName, iconFile, buffer);
                    }
                });
            }
            else {
                bitmap = BitmapFactory.decodeFile(iconFile.getPath(), OPTS);
            }
        }
        else {
            final byte[] buffer = requestIcon(iconUrl);
            if (buffer == null) {
                return null;
            }
            bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length, OPTS);
            BackgroundWorker.getInstance().invoke(new Runnable() {
                @Override
                public void run() {
                    saveCache(screenName, new File(cacheDir, screenName), buffer);
                }
            });
        }
        return bitmap;
    }

    private void saveCache(String screenName, File iconFile, byte[] buffer) {
        try {
            FileOutputStream out = new FileOutputStream(iconFile);
            out.write(buffer);
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if (screenName != null && iconFile != null) {
            fileCache.put(screenName, iconFile);
        }
    }

    public void clean() {
        for (String filename : cacheDir.list()) {
            if (!fileCache.containsKey(filename)) {
                new File(cacheDir, filename).delete();
            }
        }
    }

    public static byte[] requestIcon(String url) {
        return requestIcon(url, 0);
    }

    public static byte[] requestIcon(String url, long modified) {
        byte[] result = null;
        HttpURLConnection con = null;
        InputStream in = null;
        ByteArrayOutputStream out = null;
        int size = 0;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            if (modified != 0) {
                con.setIfModifiedSince(modified);
            }
            con.connect();

            if (con.getResponseCode() == 304) {
                return null;
            }

            in = con.getInputStream();
            out = new ByteArrayOutputStream(8192);
            byte[] buffer = new byte[8192];
            while ((size = in.read(buffer)) != -1) {
                out.write(buffer, 0, size);
            }
            result = out.toByteArray();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (con != null)
                    con.disconnect();
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            }
            catch (Exception e) {
            }
        }
        return result;
    }

    private static class FileLRUCache extends LinkedHashMap<String, File> {

        private static final long serialVersionUID = 3772462152375078300L;

        private final int maxEntries;

        public FileLRUCache(int maxEntries) {
            super(maxEntries, 0.75f, true);
            this.maxEntries = maxEntries;
        }

        @Override
        protected boolean removeEldestEntry(Entry<String, File> eldest) {
            if (size() > maxEntries) {
                eldest.getValue().delete();
                return true;
            }
            else {
                return false;
            }
        }
    }
}
