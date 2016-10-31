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

    FloatBuffer VERTEX_BUF, TEXTURE_COORD_BUF, TEXTURE_COORD_CROP;
    private static int mCameraProgram = 0; // 摄像头预览数据-->纹理
    protected int mFilterProgram; // 滤镜处理
    private int mPreviewProgram; // 输出到预览窗口
    protected int mSurfaceWidth, mSurfaceHeight;
    protected int mPreviewWidth, mPreviewHeight;

    private RenderBuffer mCameraFBO, mFilterFBO;

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

        if (TEXTURE_COORD_CROP == null) {
            TEXTURE_COORD_CROP = ByteBuffer.allocateDirect(TEXTURE_COORDS.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            TEXTURE_COORD_CROP.put(TEXTURE_COORDS);
            TEXTURE_COORD_CROP.position(0);
        }

        if (mCameraProgram == 0) {
            mCameraProgram = GlUtil.createProgram(context, R.raw.vertex_shader_texmat, R.raw.fragment_shader_ext);
        }

        mPreviewProgram = GlUtil.createProgram(context, R.raw.vertex_shader_base, R.raw.fragment_shader);

        mFilterProgram = GlUtil.createProgram(context, getVertexShaderResId(), getFragmentShaderResId());
    }

    protected int getVertexShaderResId() {
        return R.raw.vertex_shader_base;
    }

    protected int getFragmentShaderResId() {
        return R.raw.fragment_shader_ext;
    }

    public void setPreviewSize(int w, int h) {
        mPreviewWidth = w;
        mPreviewHeight = h;
    }

    public void onSurfaceChanged(int w, int h) {
        mSurfaceWidth = w;
        mSurfaceHeight = h;

        // 对纹理坐标进行裁剪，使预览分辨率适应surface的宽高
        if (mPreviewHeight != 0 && mPreviewWidth != 0) {
            float ratioW = mSurfaceWidth * 1.0f / mPreviewWidth;
            float ratioH = mSurfaceHeight * 1.0f / mPreviewHeight;
            float ratio = Math.max(ratioH, ratioW);

            float offsetx = (mPreviewWidth * ratio - mSurfaceWidth) / (mSurfaceWidth * 2.0f);
            float offsety = (mPreviewHeight * ratio - mSurfaceHeight) / (mSurfaceHeight * 2.0f);

            float tcoords[] = {
                    1.0f - offsetx, 0.0f + offsety,
                    0.0f + offsetx, 0.0f + offsety,
                    1.0f - offsetx, 1.0f - offsety,
                    0.0f + offsetx, 1.0f - offsety,
            };

            TEXTURE_COORD_CROP.put(tcoords);
            TEXTURE_COORD_CROP.position(0);
        }
    }

    final public void draw(int cameraTexId, float[] textMat) {

        // 把摄像机的数据绘制到mCameraFBO, 制作普通纹理
        if (mCameraFBO == null || mCameraFBO.getWidth() != mPreviewWidth || mCameraFBO.getHeight() != mPreviewHeight) {
            mCameraFBO = new RenderBuffer(mPreviewWidth, mPreviewHeight, GLES20.GL_TEXTURE0);
        }

        GLES20.glUseProgram(mCameraProgram);

        onBindGlslValue(mCameraProgram, GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexId, VERTEX_BUF, TEXTURE_COORD_BUF);
        int uTexMatrixLoc = GLES20.glGetUniformLocation(mCameraProgram, "uTexMatrix");
        GLES20.glUniformMatrix4fv(uTexMatrixLoc, 1, false, textMat, 0);

        mCameraFBO.bind();
        GLES20.glViewport(0, 0, mPreviewWidth, mPreviewHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        mCameraFBO.unbind();

        // filter 对摄像机的数据进行滤波处理
        if (mFilterFBO == null || mFilterFBO.getWidth() != mPreviewWidth || mFilterFBO.getHeight() != mPreviewHeight) {
            mFilterFBO = new RenderBuffer(mPreviewWidth, mPreviewHeight, GLES20.GL_TEXTURE0);
        }

        GLES20.glUseProgram(mFilterProgram);

        onBindFilterGlslValue();

        mFilterFBO.bind();
        GLES20.glViewport(0, 0, mPreviewWidth, mPreviewHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        mFilterFBO.unbind();

        // 将合成的纹理绘制到窗口
        GLES20.glUseProgram(mPreviewProgram);
        onBindGlslValue(mPreviewProgram, GLES20.GL_TEXTURE_2D, mFilterFBO.getTexId(), VERTEX_BUF, TEXTURE_COORD_CROP);


        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

    }

    protected void onBindFilterGlslValue() {
        onBindGlslValue(mFilterProgram, GLES20.GL_TEXTURE_2D, mCameraFBO.getTexId(), VERTEX_BUF, TEXTURE_COORD_BUF);
    }

    private void onBindGlslValue(int program, int texTarget, int textId, FloatBuffer vertexBuffer, FloatBuffer textrueBuffer) {
        int uTextureLocation = GLES20.glGetUniformLocation(program, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(texTarget, textId);
        GLES20.glUniform1i(uTextureLocation, 0);

        int aPositionLocation = GLES20.glGetAttribLocation(program, "aPosition");
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glVertexAttribPointer(aPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, vertexBuffer);

        int aTexCoordLocation = GLES20.glGetAttribLocation(program, "aTextureCoord");
        GLES20.glEnableVertexAttribArray(aTexCoordLocation);
        GLES20.glVertexAttribPointer(aTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, textrueBuffer);
    }

    public int getTextureId() {
        if (mFilterFBO != null) {
            return mFilterFBO.getTexId();
        }

        return 0;
    }

    public void release() {
        GLES20.glDeleteProgram(mCameraProgram);
        GLES20.glDeleteProgram(mPreviewProgram);
        mCameraProgram = 0;
        mPreviewProgram = 0;
    }
}
