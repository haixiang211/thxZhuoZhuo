package com.example.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import java.io.File;
import java.io.FileOutputStream;

public class ImageCache {
    private static final ImageCache INSTANCE = new ImageCache();
    private LruCache<String, Bitmap> memoryCache;
    private File cacheDir;

    private ImageCache() {}

    public static ImageCache getInstance() {
        return INSTANCE;
    }

    public void init(Context context) {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
        cacheDir = context.getCacheDir();
    }

    public void put(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
        // Save to file cache
        File file = new File(cacheDir, String.valueOf(key.hashCode()));
        if (!file.exists()) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    public Bitmap getBitmapFromFileCache(String key) {
        File file = new File(cacheDir, String.valueOf(key.hashCode()));
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap != null) {
                memoryCache.put(key, bitmap);
            }
            return bitmap;
        }
        return null;
    }
}
