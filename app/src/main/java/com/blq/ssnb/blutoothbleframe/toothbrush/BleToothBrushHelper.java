package com.blq.ssnb.blutoothbleframe.toothbrush;

import android.app.Activity;
import android.widget.TextView;

import java.util.Calendar;

import blq.ssnb.blutoothblefragment.AbsBleDeviceHelper;
import blq.ssnb.blutoothblefragment.inf.IBleDeviceRuler;
import blq.ssnb.snbutil.SnbLog;
import blq.ssnb.snbutil.SnbTimeUtil;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-26
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      添加描述
 * ================================================
 * </pre>
 */
public class BleToothBrushHelper extends AbsBleDeviceHelper{

    private String logTag = ">>>蓝牙BLE-help-sub";

    public BleToothBrushHelper(Activity activity) {
        super(activity);
    }

    @Override
    protected IBleDeviceRuler initDeviceRuler() {
        return new BleToothBrushRule();
    }

    @Override
    protected void onGattCommandParsing(byte[] datas) {
        dataAvailableResul(datas);
    }

    private void dataAvailableResul(final byte[] command) {

        byte action = command[0];

        if (action == ToothBrushCmdUtil.CommandAction.SET_DEVICE_TIME.getAction()) {
            setDeviceTimeResult(true);
        } else if (action == ToothBrushCmdUtil.CommandAction.SET_DEVICE_TIME.getFailAction()) {
            setDeviceTimeResult(false);
        } else if (action == ToothBrushCmdUtil.CommandAction.GET_DEVICE_TIME.getAction()) {
            final Calendar c = Calendar.getInstance();
            int year = ToothBrushCmdUtil.ConvertBCD2Decimal(command[1]) + 2000;
            int month = ToothBrushCmdUtil.ConvertBCD2Decimal(command[2]) - 1;
            int day = ToothBrushCmdUtil.ConvertBCD2Decimal(command[3]);
            int hour = ToothBrushCmdUtil.ConvertBCD2Decimal(command[4]);
            int minute = ToothBrushCmdUtil.ConvertBCD2Decimal(command[5]);
            int second = ToothBrushCmdUtil.ConvertBCD2Decimal(command[6]);
            c.set(year, month, day, hour, minute, second);
            getDeviceTimeResult(true, c);
        } else if (action == ToothBrushCmdUtil.CommandAction.GET_DEVICE_TIME.getFailAction()) {
            getDeviceTimeResult(false, Calendar.getInstance());
        } else if (action == ToothBrushCmdUtil.CommandAction.SET_BRUSHING_TIME.getAction()) {
            setBrushingTimeResult(true);
        } else if (action == ToothBrushCmdUtil.CommandAction.SET_BRUSHING_TIME.getFailAction()) {
            setBrushingTimeResult(false);
        } else if (action == ToothBrushCmdUtil.CommandAction.GET_BRUSHING_TIME.getAction()) {

            final int time1 = command[1] & 0xFF;
            final int time2 = command[2] & 0xFF;
            final int time3 = command[3] & 0xFF;
            final int time4 = command[4] & 0xFF;
            getBrushingTimeResult(true, time1, time2, time3, time4);
        } else if (action == ToothBrushCmdUtil.CommandAction.GET_BRUSHING_TIME.getFailAction()) {
            getBrushingTimeResult(false, 0, 0, 0, 0);
        } else if (action == ToothBrushCmdUtil.CommandAction.GET_HISTORY_BRUSHING_DATA.getAction()) {

            // TODO: 2019-09-24 这里刷牙数据返回，需要重点关注
            final int id = command[1] & 0xFF;

            int year, month, day, hours, minutes, seconds;

            year = ToothBrushCmdUtil.ConvertBCD2Decimal(command[2]) + 2000;
            month = ToothBrushCmdUtil.ConvertBCD2Decimal(command[3]) - 1;
            day = ToothBrushCmdUtil.ConvertBCD2Decimal(command[4]);

            if (command[5] >= 0x24) {
                //减去0x48
                hours = ToothBrushCmdUtil.ConvertBCD2Decimal((byte) (command[5] - 48));
            } else {
                hours = ToothBrushCmdUtil.ConvertBCD2Decimal(command[5]);
            }

            minutes = ToothBrushCmdUtil.ConvertBCD2Decimal(command[6]);
            seconds = ToothBrushCmdUtil.ConvertBCD2Decimal(command[7]);

            final Calendar c = Calendar.getInstance();
            c.set(year, month, day, hours, minutes, seconds);

            final int totalTime = (command[8] & 0xFF) + ((command[9] << 8) & 0xFF00);
            if (command.length == 16) {
                SnbLog.e(logTag, "获取刷牙历史数据:结束");
                return;
            }
            SnbLog.e(logTag, "获取刷牙历史数据:" + "id:" + id + ";时间:" + SnbTimeUtil.date2String("yyyy-MM-dd mm:HH:ss", c.getTime()) + ";time:" + totalTime);

//            if (getHistoryBrushingDataCallback != null) {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        getHistoryBrushingDataCallback.onCallback(c, id, isGuidingProgressData, totalTime
//                                , command[20] & 0xFF, command[21] & 0xFF, command[22] & 0xFF
//                                , command[23] & 0xFF, command[24] & 0xFF, command[25] & 0xFF
//                                , command[26] & 0xFF, command[27] & 0xFF, command[28] & 0xFF
//                                , command[29] & 0xFF, command[30] & 0xFF, command[31] & 0xFF
//                                , command[32] & 0xFF, command[33] & 0xFF, command[34] & 0xFF
//                                , command[35] & 0xFF
//                        );
//                    }
//                });
//            }


        } else if (action == ToothBrushCmdUtil.CommandAction.GET_HISTORY_BRUSHING_DATA.getFailAction()) {

        } else if (action == ToothBrushCmdUtil.CommandAction.GET_VERSION.getAction()) {
            final String version = (char) command[1] + "." + (char) command[2] + "."
                    + (char) command[3] + "." + (char) command[3];
            getVersionResult(true, version);
        } else if (action == ToothBrushCmdUtil.CommandAction.GET_VERSION.getFailAction()) {
            getVersionResult(false, "0.0.0.0");
        } else if (action == ToothBrushCmdUtil.CommandAction.MCU_RESET.getAction()) {
            mcuResetResult(true);
        } else if (action == ToothBrushCmdUtil.CommandAction.MCU_RESET.getFailAction()) {
            mcuResetResult(false);
        } else if (action == ToothBrushCmdUtil.CommandAction.OTA.getAction()) {
            otaResult(true);
        } else if (action == ToothBrushCmdUtil.CommandAction.OTA.getFailAction()) {
            otaResult(false);
        } else if (action == ToothBrushCmdUtil.CommandAction.FACTORY_RESET.getAction()) {
            factoryResetResult(true);
        } else if (action == ToothBrushCmdUtil.CommandAction.FACTORY_RESET.getFailAction()) {
            factoryResetResult(true);
        } else if (action == ToothBrushCmdUtil.CommandAction.GET_BATTERY_PERCENT.getAction()) {

            final int batteryPercent = command[1] & 0xFF;
            getBatteryPercentResult(true, batteryPercent);
        } else if (action == ToothBrushCmdUtil.CommandAction.GET_BATTERY_PERCENT.getFailAction()) {
            getBatteryPercentResult(false, 0);
        } else if (action == ToothBrushCmdUtil.CommandAction.BRUSHING_GUIDING_PROGRESS.getAction()) {
            brushingGuidingResult(true);
        } else if (action == ToothBrushCmdUtil.CommandAction.BRUSHING_GUIDING_PROGRESS.getFailAction()) {
            brushingGuidingResult(false);
        } else if (action == ToothBrushCmdUtil.CommandAction.GET_TOOTH_BRUSH_STATUS.getAction()) {
            getToothBrushStatusResult(true, command[1] & 0xFF);
        } else if (action == ToothBrushCmdUtil.CommandAction.GET_TOOTH_BRUSH_STATUS.getFailAction()) {
            getToothBrushStatusResult(false, -1);
        } else if (action == ToothBrushCmdUtil.CommandAction.SAVE_HISTORY_DATA.getAction()) {
            addHistoryResult(true);
        } else if (action == ToothBrushCmdUtil.CommandAction.SAVE_HISTORY_DATA.getFailAction()) {
            addHistoryResult(false);
        } else if (action == (byte) 0x28) {
            final int area = command[1] & 0xFF;
            //是否需要移动到下一个区域
            final boolean isNeedToMove = (command[2] == 1);
            //是否角度正确
            final boolean isCorrect = (command[3] == 1);
            onBrushResult(area, isNeedToMove, isCorrect);
        }
    }

    protected void brushingGuidingResult(boolean isSuccess) {
        setText("刷牙指导返回:"+isSuccess);
    }

    protected void onBrushResult(int area, boolean isNeedToMove, boolean isCorrect) {
        SnbLog.e(logTag, "刷牙数据返回:区域:" + area + ";是否需要移动:" + isNeedToMove + ";角度是否对:" + isCorrect);
        setText("刷牙数据返回:区域:" + area + ";是否需要移动:" + isNeedToMove + ";角度是否对:" + isCorrect);
    }

    protected void addHistoryResult(boolean isSuccess) {
        SnbLog.e(logTag, "设置历史数据:" + isSuccess);
        setText("设置历史数据:" + isSuccess);
    }

    protected void getToothBrushStatusResult(boolean isSuccess, int status) {
        SnbLog.e(logTag, "获取牙刷状态返回:" + isSuccess + "；状态:" + status);
        setText("获取牙刷状态返回:" + isSuccess + "；状态:" + status);

    }

    protected void getBatteryPercentResult(boolean isSuccess, int batteryPercent) {
        SnbLog.e(logTag, "获取电量返回:" + isSuccess + "；电量:" + batteryPercent);
        setText("获取电量返回:" + isSuccess + "；电量:" + batteryPercent);
    }

    protected void factoryResetResult(boolean isSuccess) {

    }

    protected void otaResult(boolean isSuccess) {

    }

    protected void mcuResetResult(boolean isSuccess) {

    }

    protected void getVersionResult(boolean isSuccess, String version) {
        SnbLog.e(logTag, "获取版本返回:" + isSuccess + "；version:" + version);
        setText("获取版本返回:" + isSuccess + "；version:" + version);

    }

    protected void getBrushingTimeResult(boolean isSuccess, int time1, int time2, int time3, int time4) {
        SnbLog.e(logTag, "获得刷牙时间返回:" + isSuccess + " time1:" + time1 + ";time2:" + time2 + ";time3:" + time3 + ";time4:" + time4);
        setText( "获得刷牙时间返回:" + isSuccess + " time1:" + time1 + ";time2:" + time2 + ";time3:" + time3 + ";time4:" + time4);

    }

    protected void setDeviceTimeResult(boolean isSuccess) {
        SnbLog.e(logTag, "设置蓝牙时间返回:" + isSuccess);
        setText( "设置蓝牙时间返回:" + isSuccess);

    }

    protected void getDeviceTimeResult(boolean isSuccess, Calendar time) {
        String timeStr = "";
        if (isSuccess) {
            timeStr = SnbTimeUtil.date2String("yyyy-MM-dd mm:HH:ss", time.getTime());
        }
        SnbLog.e(logTag, "获取蓝牙时间返回:" + isSuccess + " : " + timeStr);
        setText("获取蓝牙时间返回:" + isSuccess + " : " + timeStr);
    }

    protected void setBrushingTimeResult(boolean isSuccess) {
        SnbLog.e(logTag, "设置蓝牙时间返回:" + isSuccess);
        setText( "设置蓝牙时间返回:" + isSuccess);
    }

    private TextView mTextView;

    /**
     * 当然这里不应该这么传，应该以callback
     * 的方式回调到外面让外面进行相关业务逻辑的操作
     * 我这里纯是为了图方便
     * @param tv
     */
    public void setTextView(TextView tv) {
        this.mTextView = tv;
    }

    private void setText(String msg) {
        if (mTextView != null) {
            mTextView.append(msg + "\n");
        }
    }

}
