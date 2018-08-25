package com.id.connect.diaspora.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Alcatel-Dev on 25/10/2017.
 */

public class AvatarStream implements Runnable {
    private final String uri;
    private final Callback callback;

    public AvatarStream(String uri, Callback callback) {
        this.uri = uri;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            URL url = new URL(uri);
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            callback.onFinish(bmp);
        } catch (IOException e) {
            callback.onError(e);
        }
    }

    public interface Callback{
        void onFinish(Bitmap bitmap);
        void onError(Throwable t);
    }
}