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


package com.donellesandersjr.walmartbuddy;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class App extends Application {
    private static App _instance;
    public static Context getInstance() {
        return _instance;
    }

    public final static String APP_THUMBNAILS_DIR = "thumbnails";
    public final static String APP_SNAPSHOTS_DIR = "snapshots";
    public final static String APP_TEMP_DIR = "tmp";

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        //
        // Initialize settings
        //
        AppPreferences.initialize(this);
    }

    public static File getAppDir (String folderName) {
        String path =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable() ?
                        getInstance().getFilesDir().getPath() : getInstance().getCacheDir().getPath();
        return new File(path + File.separator + folderName);
    }

    public static File createFile (String folderName, String fileName) throws UnsupportedOperationException{
        File mediaStorageDir = getAppDir(folderName);
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                throw new UnsupportedOperationException("Unable to create folder: " + folderName);
            }
        }
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    public static File createSnapshotFile ()  throws UnsupportedOperationException {
        File mediaStorageDir = getAppDir(App.APP_SNAPSHOTS_DIR);
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                throw new UnsupportedOperationException(getInstance().getString(
                        R.string.error_create_photos_directory));
            }
        }
        String timeStamp = new SimpleDateFormat("'IMG_'yyyyMMdd_HHmmss_SSS'.jpg'").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + timeStamp);
    }

    public static File createThumbnailFile () throws UnsupportedOperationException {
        File mediaStorageDir = getAppDir(App.APP_THUMBNAILS_DIR);
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                throw new UnsupportedOperationException(getInstance().getString(
                        R.string.error_create_photos_directory));
            }
        }

        String timeStamp = new SimpleDateFormat("'IMG_'yyyyMMdd_HHmmss_SSS'.jpg'").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + timeStamp);
    }

    public static File createTempFile () throws UnsupportedOperationException {
        File mediaStorageDir = App.getAppDir(App.APP_TEMP_DIR);
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                throw new UnsupportedOperationException(getInstance().getString(
                        R.string.error_create_photos_directory));
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS'.jpg'").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + timeStamp);
    }
}
