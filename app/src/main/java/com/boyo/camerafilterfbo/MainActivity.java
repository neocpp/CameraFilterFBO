package com.boyo.camerafilterfbo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.boyo.camerafilterfbo.encoder.EncoderConfig;
import com.boyo.camerafilterfbo.widget.CameraGLSurfaceView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    CameraGLSurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        mSurfaceView = (CameraGLSurfaceView) findViewById(R.id.surface);
        mSurfaceView.getRender().setPreviewSize(480, 640);
        mSurfaceView.getRender().setEncoderConfig(new EncoderConfig(new File(
                FileUtil.getCacheDirectory(this, true),
                "video-" + System.currentTimeMillis() + ".mp4"), 480, 640,
                1024 * 1024 /* 1 Mb/s */));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();

        mSurfaceView.getRender().stopRecording();
    }
}
