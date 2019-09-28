package com.blq.ssnb.bleframe;

import android.view.View;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-27
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      添加描述
 * ================================================
 * </pre>
 */
public class ActionBean {
    private String title;
    private View.OnClickListener mOnClickListener;

    public String getTitle() {
        return title;
    }

    public ActionBean setTitle(String title) {
        this.title = title;
        return this;
    }

    public View.OnClickListener getOnClickListener() {
        return mOnClickListener;
    }

    public ActionBean setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
        return this;
    }
}
