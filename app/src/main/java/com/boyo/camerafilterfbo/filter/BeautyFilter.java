package com.boyo.camerafilterfbo.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.boyo.camerafilterfbo.R;

/**
 * Created by wangchengbo on 2016/10/31.
 */

public class BeautyFilter extends BaseFilter {

    public BeautyFilter(Context context) {
        super(context);
    }

    @Override
    protected int getFragmentShaderResId() {
        return R.raw.fragment_shader_beauty;
    }

    @Override
    protected int getVertexShaderResId() {
        return super.getVertexShaderResId();
    }

    @Override
    protected void onBindFilterGlslValue() {
        super.onBindFilterGlslValue();
        int singleStepOffsetLocation = GLES20.glGetUniformLocation(mFilterProgram, "singleStepOffset");
        GLES20.glUniform2fv(singleStepOffsetLocation, 1, new float[]{2.0f / mPreviewWidth, 2.0f / mPreviewHeight}, 0);
    }
}
