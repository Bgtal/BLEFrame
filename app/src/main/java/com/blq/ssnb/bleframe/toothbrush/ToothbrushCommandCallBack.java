package com.blq.ssnb.bleframe.toothbrush;

import java.util.Calendar;

import blq.ssnb.bleframe.listener.OnCommandCallBack;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-28
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      添加描述
 * ================================================
 * </pre>
 */
public interface ToothbrushCommandCallBack extends OnCommandCallBack {
    void onSetDeviceTimeResult(boolean isSuccess);
    void onGetDeviceTimeResult(boolean isSuccess, Calendar calendar);
    void onSetBrushingTimeResult(boolean isSuccess);
    void onGetBrushingTimeResult(boolean isSuccess, int time1, int time2, int time3, int time4);
    void onGetVersionResult(boolean isSuccess, String version);
    void onMCUResetResult(boolean isSuccess);
    void onOTAResult(boolean isSuccess);
    void onFactoryResetResult(boolean isSuccess);
    void onGetBatteryPercentResult(boolean isSuccess, int batteryPercent);
    void onBrushingGuidingResult(boolean isSuccess);
    void onGetToothBrushStatusResult(boolean isSuccess, int status);
    void onAddHistoryResult(boolean isSuccess);
    void onBrushResult(int area, boolean isNeedToMove, boolean isCorrect);
    void onGetBrushHistoryResult(String msg);
}
