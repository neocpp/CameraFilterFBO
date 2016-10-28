package com.boyo.camerafilterfbo.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.boyo.camerafilterfbo.GlUtil;
import com.boyo.camerafilterfbo.R;
import com.boyo.camerafilterfbo.gles.RenderBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by wangchengbo on 2016/10/28.
 */

public class BaseFilter {

    static final float SQUARE_COORDS[] = {
            1.0f, -1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, 1.0f,
    };
    static final float TEXTURE_COORDS[] = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
    };

    FloatBuffer VERTEX_BUF, TEXTURE_COORD_BUF;
    private static int mCameraProgram = 0;
    protected int mDrawProgram;
    private int mSurfaceWidth, mSurfaceHeight;
    private int mPreviewWidth, mPreviewHeight;

    private RenderBuffer mCameraFBO;

    public BaseFilter(Context context) {
        if (VERTEX_BUF == null) {
            VERTEX_BUF = ByteBuffer.allocateDirect(SQUARE_COORDS.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            VERTEX_BUF.put(SQUARE_COORDS);
            VERTEX_BUF.position(0);
        }

        if (TEXTURE_COORD_BUF == null) {
            TEXTURE_COORD_BUF = ByteBuffer.allocateDirect(TEXTURE_COORDS.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            TEXTURE_COORD_BUF.put(TEXTURE_COORDS);
            TEXTURE_COORD_BUF.position(0);
        }

        if (mCameraProgram == 0) {
            mCameraProgram = GlUtil.createProgram(context, R.raw.vertex_shader_base, R.raw.fragment_shader_ext);
        }

        mDrawProgram = GlUtil.createProgram(context, R.raw.vertex_shader_texmat, R.raw.fragment_shader);
    }

    public void setPreviewSize(int w, int h) {
        mPreviewWidth = w;
        mPreviewHeight = h;
    }

    public void setSurfaceSize(int w, int h) {
        mSurfaceWidth = w;
        mSurfaceHeight = h;
    }

    final public void draw(int cameraTexId, float[] textMat) {

        if (mCameraFBO == null || mCameraFBO.getWidth() != mPreviewWidth || mCameraFBO.getHeight() != mPreviewHeight) {
            mCameraFBO = new RenderBuffer(mPreviewWidth, mPreviewHeight, GLES20.GL_TEXTURE0);
        }

        GLES20.glUseProgram(mCameraProgram);

        int uTextureLocation = GLES20.glGetUniformLocation(mCameraProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexId);
        GLES20.glUniform1i(uTextureLocation, 0);

        int aPositionLocation = GLES20.glGetAttribLocation(mCameraProgram, "aPosition");
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glVertexAttribPointer(aPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, VERTEX_BUF);

        int aTexCoordLocation = GLES20.glGetAttribLocation(mCameraProgram, "aTextureCoord");
        GLES20.glEnableVertexAttribArray(aTexCoordLocation);
        GLES20.glVertexAttribPointer(aTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, TEXTURE_COORD_BUF);


        // Render to texture
        mCameraFBO.bind();
        GLES20.glViewport(0, 0, mPreviewWidth, mPreviewHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        mCameraFBO.unbind();

        GLES20.glUseProgram(mDrawProgram);
        uTextureLocation = GLES20.glGetUniformLocation(mDrawProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mCameraFBO.getTexId());
        GLES20.glUniform1i(uTextureLocation, 0);

        aPositionLocation = GLES20.glGetAttribLocation(mDrawProgram, "aPosition");
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glVertexAttribPointer(aPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, VERTEX_BUF);

        aTexCoordLocation = GLES20.glGetAttribLocation(mDrawProgram, "aTextureCoord");
        GLES20.glEnableVertexAttribArray(aTexCoordLocation);
        GLES20.glVertexAttribPointer(aTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, TEXTURE_COORD_BUF);

        int uTexMatrixLoc = GLES20.glGetUniformLocation(mDrawProgram, "uTexMatrix");
        GLES20.glUniformMatrix4fv(uTexMatrixLoc, 1, false, textMat, 0);

        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

    }

    public void release() {
        GLES20.glDeleteProgram(mCameraProgram);
        GLES20.glDeleteProgram(mDrawProgram);
        mCameraProgram = 0;
        mDrawProgram = 0;
    }
}
