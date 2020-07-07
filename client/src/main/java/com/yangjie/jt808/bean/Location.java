package com.yangjie.jt808.bean;


import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.bitoperator.enums.DataFormat;
import com.yangjie.bitoperator.enums.LengthUnit;

public class Location {

    @BitsProperty(length = 4, desc = "报警标志", dataFormat = DataFormat.BEAN)
    private AlarmMark alarmMark = new AlarmMark();
    @BitsProperty(length = 4, desc = "状态", dataFormat = DataFormat.BEAN)
    private Status status = new Status();
    @BitsProperty(length = 4, desc = "纬度")
    private int latitude = 0x00EC7B43;
    @BitsProperty(length = 4, desc = "经度")
    private int longitude = 0x01CB998E;
    @BitsProperty(length = 2, desc = "高程")
    private int height = 802;
    @BitsProperty(length = 2, desc = "速度")
    private int speed = 670;
    @BitsProperty(length = 2, desc = "方向")
    private int orientation = 30;
    @BitsProperty(length = 6, desc = "时间")
    private long time = 0x200703134410L;

    private static class AlarmMark {
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "紧急报瞥触动报警开关后触发")
        private byte emergency = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "超速报警")
        private byte speeding = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "疲劳驾驶")
        private byte fatigued = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "预警")
        private byte early = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "GNSS 模块发生故障")
        private byte gnssFailure = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "GNSS 天线未接或被剪断")
        private byte gnssDisconnectOrCut = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "GNSS 天线短路")
        private byte gnssShortCircuit = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "终端主电源欠压")
        private byte lowVoltage = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "终端主电源掉电")
        private byte leakVoltage = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "终端 LCD 或显示器故障")
        private byte displayerFailure = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "TTS 模块故障")
        private byte ttsFailure = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "摄像头故障")
        private byte cameraFailure = 1;
        @BitsProperty(index = 18, length = 1, unit = LengthUnit.BIT, desc = "当天累计驾驶超时")
        private byte driveTimeout = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "超时停车")
        private byte parkingTimeout = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "进出区域")
        private byte inAndOutArea = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "进出路线")
        private byte inAndOutRoute = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "路段行驶时间不足 /过长")
        private byte driveTimeLessOrLong = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "路线偏离报警")
        private byte deviateParking = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "车辆 VSS 故障")
        private byte vssFailure = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "车辆油量异常")
        private byte oilQuantityError = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "车辆被盗 (通过车辆防盗器 )")
        private byte stolen = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "车辆非法点火")
        private byte IllegalIgnition = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "车辆非法位移 ")
        private byte IllegalDisplacement = 1;

    }

    private static class Status {
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "0: ACC 关;1:ACC 开 ")
        private byte aac = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "0:未定位 ;1:定位  ")
        private byte position = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "0:北纬 :1:南纬  ")
        private byte latitudeOrientation = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "0:东经 ;1:西经  ")
        private byte longitudeOrientation = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "0:运营状态 :1:停运状态 ")
        private byte run = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "0:经纬度未经保密插件加密 ;l:经纬度已经保密插件加密  ")
        private byte locationEncryption = 1;
        @BitsProperty(index = 10, length = 1, unit = LengthUnit.BIT, desc = "0:车辆油路正常 :1:车辆油路断开 ")
        private byte oil = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "0:车辆电路正常 :1:车辆电路断开  ")
        private byte voltage = 1;
        @BitsProperty(length = 1, unit = LengthUnit.BIT, desc = "0:车门解锁； 1：车门加锁 ")
        private byte doorLock = 1;
    }
}
