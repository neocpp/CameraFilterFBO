package com.boyo.camerafilterfbo.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.boyo.camerafilterfbo.camera.CameraController;
import com.boyo.camerafilterfbo.renderer.GLSurfaceViewRender;

/**
 * Created by wangchengbo on 2016/10/28.
 */

public class CameraGLSurfaceView extends GLSurfaceView implements SurfaceTexture.OnFrameAvailableListener {

    private GLSurfaceViewRender mRenderer;

    public CameraGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        mRenderer = new GLSurfaceViewRender(getContext(), this);
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        CameraController.getInstance().release();
        queueEvent(new Runnable() {
            @Override
            public void run() {
                // 跨进程 清空 Renderer数据
                mRenderer.notifyPausing();
            }
        });

        super.onPause();
    }

}
