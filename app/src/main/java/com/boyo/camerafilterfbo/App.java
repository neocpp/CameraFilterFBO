package com.boyo.camerafilterfbo;

import android.app.Application;

import com.boyo.camerafilterfbo.encoder.TextureMovieEncoder;

/**
 * Created by wangchengbo on 2016/10/31.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        TextureMovieEncoder.initialize(this);
    }
}
