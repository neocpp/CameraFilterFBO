package com.boyo.camerafilterfbo.filter;

import android.content.Context;

import com.boyo.camerafilterfbo.R;

/**
 * Created by wangchengbo on 2016/10/31.
 */

public class ContrastFilter extends BaseFilter {

    public ContrastFilter(Context context) {
        super(context);
    }

    @Override
    protected int getFragmentShaderResId() {
        return R.raw.fragment_shader_contrast;
    }
}
