package com.blq.ssnb.blutoothbleframe.toothbrush;

import java.util.Calendar;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-23
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      添加描述
 * ================================================
 * </pre>
 */
public class ToothBrushCmdUtil {

    enum CommandAction {
        SET_DEVICE_TIME((byte) 0x1, (byte) 0x81),
        GET_DEVICE_TIME((byte) 0x41, (byte) 0xC1),
        SET_BRUSHING_TIME((byte) 0x06, (byte) 0x86),
        GET_BRUSHING_TIME((byte) 0x7, (byte) 0x87),
        GET_HISTORY_BRUSHING_DATA((byte) 0x15,(byte)0x85 ),
        //// TODO: 2019-09-24 这里的值和文档中的值不一致
        GET_VERSION((byte) 0x27,(byte)0xA7),
        MCU_RESET((byte) 0x2E,(byte)0xAE),
        OTA((byte) 0x47,(byte)0XB4),
        FACTORY_RESET((byte) 0x12,(byte)0x92),
        GET_BATTERY_PERCENT((byte) 0x13,(byte)0x93),
        BRUSHING_GUIDING_PROGRESS((byte) 0x19,(byte)0x89 ),
        GET_TOOTH_BRUSH_STATUS((byte) 0x21,(byte)0x81),
        SAVE_HISTORY_DATA((byte) 0x23,(byte)0x83),
        ;
        byte action;
        byte failAction;

        CommandAction(byte action, byte failAction) {
            this.action = action;
            this.failAction = failAction;
        }

        public byte getAction() {
            return action;
        }

        public byte getFailAction() {
            return failAction;
        }
    }

    /**
     * 设置设备时间
     *
     * @param calendar 日历信息
     * @return 返回参数byte形式
     */
    public static byte[] setDeviceTime(Calendar calendar) {
        byte year, month, day, hour, minute, second;
        year = ConvertDecimal2BCD((byte) (calendar.get(Calendar.YEAR) - 2000));
        month = ConvertDecimal2BCD((byte) (calendar.get(Calendar.MONTH) + 1));
        day = ConvertDecimal2BCD((byte) (calendar.get(Calendar.DAY_OF_MONTH)));
        hour = ConvertDecimal2BCD((byte) (calendar.get(Calendar.HOUR_OF_DAY)));
        minute = ConvertDecimal2BCD((byte) (calendar.get(Calendar.MINUTE)));
        second = ConvertDecimal2BCD((byte) (calendar.get(Calendar.SECOND)));

        byte[] command = new byte[16];
        command[0] = CommandAction.SET_DEVICE_TIME.getAction();
        command[1] = year;
        command[2] = month;
        command[3] = day;
        command[4] = hour;
        command[5] = minute;
        command[6] = second;

        command[15] = crc(command);
        return command;
    }

    /**
     * 获取设备时间
     */
    public static byte[] getDeviceTime() {
        byte[] command = new byte[16];
        command[0] = CommandAction.GET_DEVICE_TIME.getAction();
        command[15] = crc(command);
        return command;
    }

    /**
     * 获取电量百分比
     */
    public static byte[] getBatteryPercent() {
        byte[] command = new byte[16];
        command[0] = CommandAction.GET_BATTERY_PERCENT.getAction();
        command[15] = crc(command);
        return command;
    }

    /**
     * 获取固件版本
     */
    public static byte[] getVersion() {
        byte[] command = new byte[16];
        command[0] = CommandAction.GET_VERSION.getAction();
        command[15] = crc(command);
        return command;
    }

    /**
     * 单片机复位
     */
    public static byte[] mcuReset() {
        byte[] command = new byte[16];
        command[0] = CommandAction.MCU_RESET.getAction();
        command[15] = crc(command);
        return command;
    }

    /**
     * 设置刷牙时间
     *
     * @param time1
     * @param time2
     * @param time3
     * @param time4
     */
    public static byte[] setBrushingTime(int time1, int time2, int time3, int time4) {

        byte[] command = new byte[16];
        command[0] = CommandAction.SET_BRUSHING_TIME.getAction();
        command[1] = (byte) time1;
        command[2] = (byte) time2;
        command[3] = (byte) time3;
        command[4] = (byte) time4;
        command[15] = crc(command);
        return command;

    }

    /**
     * 设置刷牙时长
     *
     * @param totalTime
     */
    public static byte[] setBrushinTime(int totalTime) {
        if (totalTime < 90) {
            totalTime = 90;
        }
        if (180 < totalTime) {
            totalTime = 180;
        }
        int time = totalTime >> 2;
        return setBrushingTime(time, time, time, time);
    }

    /**
     * 获取刷牙时长
     */
    public static byte[] getBrushingTime() {

        byte[] command = new byte[16];
        command[0] = CommandAction.GET_BRUSHING_TIME.getAction();
        command[15] = crc(command);
        return command;
    }

    public static byte[] getHistoryBrushingData() {
        byte[] command = new byte[16];
        command[0] = CommandAction.GET_HISTORY_BRUSHING_DATA.getAction();
        command[1] = 0x0;
        command[15] = crc(command);
        return command;
    }

//    public static byte[] deleteAllHistoryBrushData(){
//        byte[] command = new byte[16];
//        command[0] = CommandAction.GET_HISTORY_BRUSHING_DATA.getAction();
//        command[1] = (byte) 0xFE;
//        command[15] = crc(command);
//        command(command);
//    }

    /**
     * 固件升级
     */
    public static byte[] OTA() {
        byte[] command = new byte[16];
        command[0] = CommandAction.OTA.getAction();
        command[15] = crc(command);
        return command;
    }

    /**
     * 恢复出厂设置
     */
    public static byte[] factoryReset() {
        byte[] command = new byte[16];
        command[0] = CommandAction.FACTORY_RESET.getAction();
        command[16] = crc(command);
        return command;
    }

    /**
     * 刷牙指导
     * @param command 0 结束 1 开始 2 换区域
     * @param region
     * @return
     */
    public static byte[] BrushingGuidingProgress(int command, int region) {
        byte[] commands = new byte[16];

        commands[0] = CommandAction.BRUSHING_GUIDING_PROGRESS.getAction();
        commands[1] = (byte) command;
        commands[2] = (byte) region;
        commands[15] = crc(commands);
        return commands;
    }

    /**
     * 获得刷牙状态
     * @return
     */
    public static byte[] getToothBrushStatus() {
        byte[] command = new byte[16];
        command[0] = CommandAction.GET_TOOTH_BRUSH_STATUS.getAction();
        command[15] = crc(command);
        return command;
    }

    /**
     * 设置刷牙状态
     * @param gearPosition
     * @return
     */
    public static byte[] setToothBrushStatus(int gearPosition) {
        byte[] command = new byte[16];
        command[0] = CommandAction.GET_TOOTH_BRUSH_STATUS.getAction();
        command[1] = 0x1;
        command[2] = (byte) gearPosition;
        command[15] = crc(command);
        return command;
    }

    /**
     * 保存历史记录
     * @param datetime
     * @param totalBrushTime
     * @param region1
     * @param region2
     * @param region3
     * @param region4
     * @param region5
     * @param region6
     * @param region7
     * @param region8
     * @param region9
     * @param region10
     * @param region11
     * @param region12
     * @param region13
     * @param region14
     * @param region15
     * @param region16
     * @return
     */
    public static byte[] saveHistoryData(Calendar datetime, int totalBrushTime,
                                       int region1, int region2, int region3,
                                       int region4, int region5, int region6,
                                       int region7, int region8, int region9,
                                       int region10, int region11, int region12,
                                       int region13, int region14, int region15,
                                       int region16) {
        byte[] commands = new byte[35];

        commands[0] = CommandAction.SAVE_HISTORY_DATA.getAction();

        int year = datetime.get(Calendar.YEAR) - 2000;
        int month = datetime.get(Calendar.MONTH) + 1;
        int day = datetime.get(Calendar.DAY_OF_MONTH);
        int hour = datetime.get(Calendar.HOUR_OF_DAY);
        int minute = datetime.get(Calendar.MINUTE);
        int second = datetime.get(Calendar.SECOND);

        commands[1] = ConvertDecimal2BCD((byte) year);
        commands[2] = ConvertDecimal2BCD((byte) month);
        commands[3] = ConvertDecimal2BCD((byte) day);
        commands[4] = (byte) (ConvertDecimal2BCD((byte) hour) + 48);
        commands[5] = ConvertDecimal2BCD((byte) minute);
        commands[6] = ConvertDecimal2BCD((byte) second);

        commands[7] = (byte) (totalBrushTime & 0xFF);
        commands[8] = (byte) ((totalBrushTime >> 0x8) & 0xFF);

        commands[19] = (byte) region1;
        commands[20] = (byte) region2;
        commands[21] = (byte) region3;
        commands[22] = (byte) region4;
        commands[23] = (byte) region5;
        commands[24] = (byte) region6;
        commands[25] = (byte) region7;
        commands[26] = (byte) region8;
        commands[27] = (byte) region9;
        commands[28] = (byte) region10;
        commands[29] = (byte) region11;
        commands[30] = (byte) region12;
        commands[31] = (byte) region13;
        commands[32] = (byte) region14;
        commands[33] = (byte) region15;
        commands[34] = (byte) region16;
        return commands;

    }


    private static byte crc(byte[] cmd) {
        byte val = 0;
        for (int i = 0; i < cmd.length - 1; i++) {
            val += cmd[i];
        }
        return val;
    }


    public static int ConvertBCD2Decimal(byte bcd_code) {

        int result = 0;
        result += (bcd_code & 0xF);
        result += (bcd_code >>> 0x4 & 0xF) * 10;
        return result;

    }

    // the decimal range only from 0x0 to 0x63
    private static byte ConvertDecimal2BCD(byte decimal) {

        byte result = 0;
        result += (decimal % 10);
        result += (decimal / 10 << 0x4);
        return result;
    }
}
