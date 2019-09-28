package com.blq.ssnb.blutoothbleframe;

import android.app.Application;

import blq.ssnb.blutoothblefragment.Constant;
import blq.ssnb.blutoothblefragment.SnbBluetoothManager;
import blq.ssnb.snbutil.SnbLog;

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
public class MApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SnbBluetoothManager.init(this);
        SnbLog.getGlobalBuilder().isOpen(true).isShowLocation(true);
        Constant.isLogOpen(true).isShowLocation(true);
    }
}
