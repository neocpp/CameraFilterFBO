package com.boyo.camerafilterfbo.renderer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;

import com.boyo.camerafilterfbo.GlUtil;
import com.boyo.camerafilterfbo.camera.CameraController;
import com.boyo.camerafilterfbo.encoder.EncoderConfig;
import com.boyo.camerafilterfbo.encoder.TextureMovieEncoder;
import com.boyo.camerafilterfbo.filter.BaseFilter;
import com.boyo.camerafilterfbo.filter.BeautyFilter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wangchengbo on 2016/10/28.
 */

public class GLSurfaceViewRender implements GLSurfaceView.Renderer {

    private static final int RECORDING_OFF = 0;
    private static final int RECORDING_ON = 1;
    private static final int RECORDING_RESUMED = 2;

    private int mCameraTextureId;
    private SurfaceTexture mCameraSufaceTexture;

    private int mSurfaceWidth, mSurfaceHeight;
    private int mPreviewWidth, mPreviewHeight;

    private Context mContext;
    private SurfaceTexture.OnFrameAvailableListener mFrameAvailableListener;

    private BaseFilter mFilter;
    private final float[] mSTMatrix = new float[16];

    private TextureMovieEncoder mVideoEncoder;
    private boolean mRecordingEnabled;
    private int mRecordingStatus;
    private EncoderConfig mEncoderConfig;


    public GLSurfaceViewRender(Context context, SurfaceTexture.OnFrameAvailableListener l) {
        mContext = context.getApplicationContext();
        mFrameAvailableListener = l;
        mPreviewHeight = mPreviewWidth = 0;

        mVideoEncoder = TextureMovieEncoder.getInstance();
    }

    public void setEncoderConfig(EncoderConfig encoderConfig) {
        mEncoderConfig = encoderConfig;
    }

    public void setRecordingEnabled(boolean recordingEnabled) {
        mRecordingEnabled = recordingEnabled;
    }

    public void setPreviewSize(int w, int h) {
        mSurfaceWidth = w;
        mSurfaceHeight = h;
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

        mRecordingEnabled = mVideoEncoder.isRecording();
        if (mRecordingEnabled) {
            mRecordingStatus = RECORDING_RESUMED;
        } else {
            mRecordingStatus = RECORDING_OFF;
        }

        mRecordingEnabled = true;
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

        // recording
        videoOnDrawFrame(mFilter.getTextureId(), mCameraSufaceTexture.getTimestamp());
    }

    private void videoOnDrawFrame(int textureId, long timestamp) {
        if (mRecordingEnabled && mEncoderConfig != null) {
            switch (mRecordingStatus) {
                case RECORDING_OFF:
                    mEncoderConfig.updateEglContext(EGL14.eglGetCurrentContext());
                    mVideoEncoder.startRecording(mEncoderConfig);
                    mVideoEncoder.setTextureId(textureId);
                    mRecordingStatus = RECORDING_ON;

                    break;
                case RECORDING_RESUMED:
                    mVideoEncoder.updateSharedContext(EGL14.eglGetCurrentContext());
                    mVideoEncoder.setTextureId(textureId);
                    mRecordingStatus = RECORDING_ON;
                    break;
                case RECORDING_ON:
                    // yay
                    break;
                default:
                    throw new RuntimeException("unknown status " + mRecordingStatus);
            }
        } else {
            switch (mRecordingStatus) {
                case RECORDING_ON:
                case RECORDING_RESUMED:
                    mVideoEncoder.stopRecording();
                    mRecordingStatus = RECORDING_OFF;
                    break;
                case RECORDING_OFF:
                    // yay
                    break;
                default:
                    throw new RuntimeException("unknown status " + mRecordingStatus);
            }
        }

        mVideoEncoder.frameAvailable(null, timestamp);
    }


    public void notifyPausing() {
        if (mFilter != null) {
            mFilter.release();
        }

        //mRecordingEnabled = false;
        //mVideoEncoder.stopRecording();
    }

    public void stopRecording(){
        //mRecordingEnabled = false;
        mVideoEncoder.stopRecording();
    }
}
