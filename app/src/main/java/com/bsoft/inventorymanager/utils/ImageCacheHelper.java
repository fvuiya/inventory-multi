package com.bsoft.inventorymanager.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageCacheHelper {
    
    private static final String TAG = "ImageCacheHelper";
    private static LruCache<String, Bitmap> memoryCache;
    private static Context applicationContext;
    
    public static void initialize(Context context) {
        applicationContext = context.getApplicationContext();
        
        // Get max available VM memory, exceeding this amount will throw an OutOfMemory exception.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }
    
    /**
     * Adds a bitmap to both memory and disk cache
     * @param key Unique key for the bitmap
     * @param bitmap The bitmap to cache
     */
    public static void addBitmapToCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
            // Also save to disk cache
            saveBitmapToDiskCache(key, bitmap);
        }
    }
    
    /**
     * Gets bitmap from memory cache
     * @param key The key for the bitmap
     * @return The bitmap if found, null otherwise
     */
    public static Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }
    
    /**
     * Gets bitmap from disk cache
     * @param key The key for the bitmap
     * @return The bitmap if found, null otherwise
     */
    public static Bitmap getBitmapFromDiskCache(String key) {
        try {
            File cacheDir = new File(applicationContext.getCacheDir(), "image_cache");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            
            File imageFile = new File(cacheDir, key + ".jpg");
            if (imageFile.exists()) {
                return BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting bitmap from disk cache", e);
        }
        return null;
    }
    
    /**
     * Saves bitmap to disk cache
     * @param key The key for the bitmap
     * @param bitmap The bitmap to save
     */
    private static void saveBitmapToDiskCache(String key, Bitmap bitmap) {
        try {
            File cacheDir = new File(applicationContext.getCacheDir(), "image_cache");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            
            File imageFile = new File(cacheDir, key + ".jpg");
            FileOutputStream fos = new FileOutputStream(imageFile);
            
            // Compress the bitmap to reduce file size
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap to disk cache", e);
        }
    }
    
    /**
     * Compresses a bitmap to reduce memory usage
     * @param bitmap The original bitmap
     * @param maxWidth Maximum width for the compressed bitmap
     * @param maxHeight Maximum height for the compressed bitmap
     * @return Compressed bitmap
     */
    public static Bitmap compressBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        // Calculate scaling factor
        float scaleWidth = ((float) maxWidth) / width;
        float scaleHeight = ((float) maxHeight) / height;
        float scale = Math.min(scaleWidth, scaleHeight);
        
        // Create matrix for scaling
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.postScale(scale, scale);
        
        // Create the scaled bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
    }
    
    /**
     * Gets a cached bitmap (from memory first, then disk)
     * @param key The key for the bitmap
     * @return The cached bitmap if found, null otherwise
     */
    public static Bitmap getCachedBitmap(String key) {
        Bitmap bitmap = getBitmapFromMemCache(key);
        if (bitmap == null) {
            bitmap = getBitmapFromDiskCache(key);
            if (bitmap != null) {
                // Add to memory cache for faster access next time
                addBitmapToCache(key, bitmap);
            }
        }
        return bitmap;
    }
    
    /**
     * Clears both memory and disk caches
     */
    public static void clearCache() {
        memoryCache.evictAll();
        
        // Clear disk cache
        File cacheDir = new File(applicationContext.getCacheDir(), "image_cache");
        if (cacheDir.exists()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }
}