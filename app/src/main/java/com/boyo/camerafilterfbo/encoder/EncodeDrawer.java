package com.boyo.camerafilterfbo.encoder;

import android.content.Context;
import android.opengl.GLES20;

import com.boyo.camerafilterfbo.GlUtil;
import com.boyo.camerafilterfbo.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by wangchengbo on 2016/10/28.
 */

public class EncodeDrawer {

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
    private int mPreviewProgram; // 输出到预览窗口
    protected int mSurfaceWidth, mSurfaceHeight;

    public EncodeDrawer(Context context) {
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

        mPreviewProgram = GlUtil.createProgram(context, R.raw.vertex_shader_base, R.raw.fragment_shader);

    }

    public void setSurfaceSize(int w, int h) {
        mSurfaceWidth = w;
        mSurfaceHeight = h;
    }

    public int getSurfaceWidth() {
        return mSurfaceWidth;
    }

    public int getSurfaceHeight() {
        return mSurfaceHeight;
    }

    final public void draw(int texId) {

        // 将合成的纹理绘制到窗口
        GLES20.glUseProgram(mPreviewProgram);
        onBindGlslValue(mPreviewProgram, GLES20.GL_TEXTURE_2D, texId, VERTEX_BUF, TEXTURE_COORD_BUF);

        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

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


    public void release() {
        GLES20.glDeleteProgram(mPreviewProgram);
        mPreviewProgram = 0;
    }
}
