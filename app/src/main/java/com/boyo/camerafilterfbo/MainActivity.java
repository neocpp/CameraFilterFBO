package com.boyo.camerafilterfbo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.boyo.camerafilterfbo.widget.CameraGLSurfaceView;

public class MainActivity extends AppCompatActivity {
    CameraGLSurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = (CameraGLSurfaceView) findViewById(R.id.surface);

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
}
