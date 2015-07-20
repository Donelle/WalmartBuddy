/*
 * Copyright (C) 2015 Donelle Sanders Jr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.donellesandersjr.walmartbuddy.api;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public final class WBImageUtils {
    private static final String TAG = "com.donellesandersjr.walmartbuddy.utils.WBImageUtils";
    private static final int DEFAULT_JPEG_QUALITY = 100;

    public static Bitmap bitmapFromURL (URL urlPath) throws IOException {
        HttpURLConnection connection = null;
        Bitmap bitmap = null;
        try {
            connection = (HttpURLConnection) urlPath.openConnection();
            InputStream inputStream = new BufferedInputStream(connection.getInputStream());
            BufferedInputStream buffIn = new BufferedInputStream(inputStream);
            bitmap = BitmapFactory.decodeStream(buffIn);
        } catch (IOException ex) {
            //
            // We should load a default image when one can't be fetched
            // but I'm being lazy so.. bite me :-P
            //
            WBLogger.Error(TAG, ex);
            throw ex;
        } finally {
            if (connection != null)
                connection.disconnect();
        }
        return bitmap;
    }

    public static Bitmap bitmapFromUri (URI filePath) throws IOException {
        FileInputStream inputStream = null;
        Bitmap bitmap = null;
        try {
            inputStream = new FileInputStream(filePath.getPath());
            final BufferedInputStream buffIn = new BufferedInputStream(inputStream);
            bitmap = BitmapFactory.decodeStream(buffIn);
        } catch (IOException ex) {
            WBLogger.Error(TAG, ex);
            throw ex;
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (Exception ex) {
                }
        }
        return bitmap;
    }

    public static Bitmap bitmapFromUri (URI filepath, int width, int height) {
        String contentUrl = filepath.getPath();

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(contentUrl, options);

        options.inSampleSize = WBImageUtils.calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(contentUrl, options);
    }

    public static boolean compressToFile (Bitmap bitmap, URI fileUri) {
        FileOutputStream stream = null;
        boolean didSave = false;
        try {
            stream = new FileOutputStream(fileUri.getPath());
            stream.write(compressToBytes(bitmap));
            didSave = true;
        } catch (Exception ex) {
            WBLogger.Error(TAG, ex);
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (Exception ex) {
                    WBLogger.Error(TAG, ex);
                }
        }
        return didSave;
    }

    public static byte[] compressToBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(65536);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap resizeBitmapByScale(
            Bitmap bitmap, float scale, boolean recycle) {
        int width = Math.round(bitmap.getWidth() * scale);
        int height = Math.round(bitmap.getHeight() * scale);
        if (width == bitmap.getWidth()
                && height == bitmap.getHeight()) return bitmap;
        Bitmap target = Bitmap.createBitmap(width, height, _getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return target;
    }

    public static Bitmap resizeDownBySideLength(
            Bitmap bitmap, int maxLength, boolean recycle) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        float scale = Math.min(
                (float) maxLength / srcWidth, (float) maxLength / srcHeight);
        if (scale >= 1.0f) return bitmap;
        return resizeBitmapByScale(bitmap, scale, recycle);
    }

    public static Bitmap resizeAndCropCenter(Bitmap bitmap, int size, boolean recycle) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == size && h == size) return bitmap;

        // scale the image so that the shorter side equals to the target;
        // the longer side will be center-cropped.
        float scale = (float) size / Math.min(w,  h);

        Bitmap target = Bitmap.createBitmap(size, size, _getConfig(bitmap));
        int width = Math.round(scale * bitmap.getWidth());
        int height = Math.round(scale * bitmap.getHeight());
        Canvas canvas = new Canvas(target);
        canvas.translate((size - width) / 2f, (size - height) / 2f);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return target;
    }


    public static void recycleSilently(Bitmap bitmap) {
        if (bitmap == null) return;
        try {
            bitmap.recycle();
        } catch (Throwable t) {
            WBLogger.Debug(TAG, "unable recycle bitmap");
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable, int defaultWidth, int defaultHeight) {
        Bitmap bitmap = null;
        int width = Math.max(drawable.getIntrinsicWidth(), defaultWidth);
        int height = Math.max(drawable.getIntrinsicHeight(), defaultHeight);
        try {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } catch (Exception ex) {
            WBLogger.Error(TAG, ex);
            recycleSilently(bitmap);
            bitmap = null;
        }
        return bitmap;
    }

    private static Bitmap.Config _getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }
}
