package com.boyo.camerafilterfbo.renderer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;

import com.boyo.camerafilterfbo.GlUtil;
import com.boyo.camerafilterfbo.camera.CameraController;
import com.boyo.camerafilterfbo.filter.BaseFilter;
import com.boyo.camerafilterfbo.filter.BeautyFilter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wangchengbo on 2016/10/28.
 */

public class GLSurfaceViewRender implements GLSurfaceView.Renderer {

    private int mCameraTextureId;
    private SurfaceTexture mCameraSufaceTexture;

    private int mSurfaceWidth, mSurfaceHeight;
    private int mPreviewWidth, mPreviewHeight;

    private Context mContext;
    private SurfaceTexture.OnFrameAvailableListener mFrameAvailableListener;

    private BaseFilter mFilter;
    private final float[] mSTMatrix = new float[16];

    public GLSurfaceViewRender(Context context, SurfaceTexture.OnFrameAvailableListener l) {
        mContext = context.getApplicationContext();
        mFrameAvailableListener = l;
        mPreviewHeight = mPreviewWidth = 0;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        mCameraTextureId = GlUtil.createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        mCameraSufaceTexture = new SurfaceTexture(mCameraTextureId);
        mCameraSufaceTexture.setOnFrameAvailableListener(mFrameAvailableListener);

        mFilter = new BeautyFilter(mContext);
        if (mPreviewHeight == 0 || mPreviewWidth == 0) {
            mPreviewHeight = 640;
            mPreviewWidth = 480;
        }
        mFilter.setPreviewSize(mPreviewWidth, mPreviewHeight);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;

        CameraController.getInstance().setupCamera(mCameraSufaceTexture, mContext, width);
        CameraController.getInstance().configureCameraParameters(mPreviewWidth, mPreviewHeight);
        mFilter.onSurfaceChanged(mSurfaceWidth, mSurfaceHeight);
        CameraController.getInstance().startCameraPreview();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mCameraSufaceTexture.updateTexImage();
        mCameraSufaceTexture.getTransformMatrix(mSTMatrix);
        mFilter.draw(mCameraTextureId, mSTMatrix);

        mFilter.getTextureId();
        // recording

    }

    public void notifyPausing() {
        mFilter.release();
    }
}
